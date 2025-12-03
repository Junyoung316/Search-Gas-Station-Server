package com.spring.searchGasStation.presentation.opinet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.searchGasStation.application.opinet.OpinetService;
import com.spring.searchGasStation.application.redis.GasStationCacheService;
import com.spring.searchGasStation.application.redis.GlobalGasStationCacheService;
import com.spring.searchGasStation.dto.GasStationCacheDto;
import com.spring.searchGasStation.util.CoordinateConverter;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OpinetController {

    private final OpinetService opinetService;
    private final GasStationCacheService userCacheService;
    private final GlobalGasStationCacheService globalCacheService;
    private final ObjectMapper objectMapper;
    private final CoordinateConverter coordinateConverter;

    /**
     * 주변 주유소 조회 - 글로벌 캐시 우선, 없으면 API 호출
     */
    @GetMapping("/api/gas-stations")
    public String getGasStations(
            @RequestParam double x,
            @RequestParam double y,
            @RequestParam int radius,
            @RequestParam String prodcd,
            @RequestParam int sort,
            HttpSession session) {
        
        String sessionId = session.getId();
        
        try {
            // KATEC 좌표를 WGS84로 변환
            double[] wgs84 = coordinateConverter.katecToWgs84(x, y);
            double longitude = wgs84[0];
            double latitude = wgs84[1];
            double radiusKm = radius / 1000.0;
            
            log.info("Searching stations - KATEC: ({}, {}), WGS84: ({}, {}), radius: {}m, prodcd: {}", 
                    x, y, longitude, latitude, radius, prodcd);
            
            // 1. 그리드 캐시 확인 (같은 지역을 최근에 조회했는지)
            if (globalCacheService.hasGridCache(longitude, latitude, radius, prodcd)) {
                log.info("Grid cache HIT for location ({}, {}) with radius {}m", longitude, latitude, radius);
                
                List<GasStationCacheDto> cachedStations = globalCacheService.getGridCache(longitude, latitude, radius, prodcd);
                
                if (!cachedStations.isEmpty()) {
                    // 사용자 세션 캐시에도 저장
                    userCacheService.cacheGasStations(sessionId, cachedStations);
                    
                    // JSON 응답 생성
                    String jsonResponse = convertStationsToJson(cachedStations);
                    log.info("Returned {} stations from grid cache", cachedStations.size());
                    return jsonResponse;
                }
            }
            
            // 2. GEO 인덱스 검색 (글로벌 캐시)
            List<GasStationCacheDto> geoResults = globalCacheService.findNearbyStations(longitude, latitude, radiusKm);
            
            if (!geoResults.isEmpty()) {
                log.info("Global GEO cache HIT: Found {} stations", geoResults.size());
                
                // 제품 코드로 필터링 및 정렬
                List<GasStationCacheDto> filtered = filterAndSort(geoResults, prodcd, sort);
                
                if (!filtered.isEmpty()) {
                    // 사용자 세션 캐시에 저장
                    userCacheService.cacheGasStations(sessionId, filtered);
                    
                    // 그리드 캐시에 저장 (다음 검색을 위해)
                    List<String> stationIds = filtered.stream()
                            .map(GasStationCacheDto::getId)
                            .toList();
                    globalCacheService.saveGridCache(longitude, latitude, radius, prodcd, stationIds);
                    
                    String jsonResponse = convertStationsToJson(filtered);
                    log.info("Returned {} stations from global GEO cache", filtered.size());
                    return jsonResponse;
                }
            }
            
            // 3. 캐시 미스 - 오피넷 API 호출
            log.info("Cache MISS - Calling Opinet API");
            String apiResponse = opinetService.getAroundStationList(x, y, radius, prodcd, sort);
            
            // 4. API 응답을 파싱하여 글로벌 캐시에 저장
            cacheApiResponse(apiResponse, sessionId);
            
            return apiResponse;
            
        } catch (Exception e) {
            log.error("Error in getGasStations: ", e);
            
            // 에러 발생 시 API 직접 호출
            try {
                String apiResponse = opinetService.getAroundStationList(x, y, radius, prodcd, sort);
                cacheApiResponse(apiResponse, sessionId);
                return apiResponse;
            } catch (Exception ex) {
                log.error("Failed to call Opinet API as fallback: ", ex);
                return "{\"RESULT\":{\"OIL\":[]}}";
            }
        }
    }

    /**
     * 주유소 상세 정보 조회
     */
    @GetMapping("/api/station-detail")
    public String getStationDetail(@RequestParam String id) {
        log.info("Getting station detail for id: {}", id);
        
        try {
            return opinetService.getStationDetail(id);
        } catch (Exception e) {
            log.error("Failed to get station detail: ", e);
            return "{\"RESULT\":{\"OIL\":[]}}";
        }
    }
    
    /**
     * 주유소 이름으로 검색 (글로벌 캐시 포함)
     */
    @GetMapping("/api/search-stations")
    public Map<String, Object> searchStations(
            @RequestParam String query,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String sessionId = session.getId();
            
            // 1. 먼저 사용자 세션 캐시에서 검색
            List<GasStationCacheDto> userResults = userCacheService.searchByName(sessionId, query);
            
            // 2. 글로벌 캐시에서도 검색
            List<GasStationCacheDto> globalResults = globalCacheService.searchByName(query);
            
            // 3. 결과 병합 (중복 제거)
            Map<String, GasStationCacheDto> mergedMap = new HashMap<>();
            userResults.forEach(station -> mergedMap.put(station.getId(), station));
            globalResults.forEach(station -> mergedMap.put(station.getId(), station));
            
            List<GasStationCacheDto> results = new ArrayList<>(mergedMap.values());
            
            // 4. 이름 유사도로 정렬 (검색어와 가장 유사한 순)
            results.sort((a, b) -> {
                String nameA = a.getName().toLowerCase();
                String nameB = b.getName().toLowerCase();
                String queryLower = query.toLowerCase();
                
                // 정확히 일치하는 것을 우선
                boolean aStarts = nameA.startsWith(queryLower);
                boolean bStarts = nameB.startsWith(queryLower);
                
                if (aStarts && !bStarts) return -1;
                if (!aStarts && bStarts) return 1;
                
                return nameA.compareTo(nameB);
            });
            
            response.put("success", true);
            response.put("count", results.size());
            response.put("results", results);
            response.put("sources", Map.of(
                "userCache", userResults.size(),
                "globalCache", globalResults.size(),
                "total", results.size()
            ));
            
            log.info("Search '{}' returned {} results (user: {}, global: {}, merged: {})", 
                    query, results.size(), userResults.size(), globalResults.size(), results.size());
            
        } catch (Exception e) {
            log.error("Search failed: ", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 캐시 통계 조회 (관리자용)
     */
    @GetMapping("/api/cache-stats")
    public Map<String, Object> getCacheStats() {
        return globalCacheService.getCacheStats();
    }

    /**
     * 손상된 캐시 정리 (관리자용)
     */
    @GetMapping("/api/cache-cleanup")
    public Map<String, Object> cleanupCache() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            globalCacheService.cleanupCorruptedGridCache();
            response.put("success", true);
            response.put("message", "Corrupted cache cleaned up successfully");
            
            log.info("Cache cleanup completed");
        } catch (Exception e) {
            log.error("Cache cleanup failed: ", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    /**
     * 전체 캐시 삭제 (관리자용)
     */
    @GetMapping("/api/cache-clear")
    public Map<String, Object> clearCache() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            globalCacheService.clearAllCache();
            response.put("success", true);
            response.put("message", "All cache cleared successfully");
            
            log.info("All cache cleared");
        } catch (Exception e) {
            log.error("Failed to clear cache: ", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    /**
     * API 응답을 파싱하여 캐시에 저장
     */
    private void cacheApiResponse(String apiResponse, String sessionId) {
        try {
            JsonNode rootNode = objectMapper.readTree(apiResponse);
            JsonNode oilArray = rootNode.path("RESULT").path("OIL");
            
            if (!oilArray.isArray() || oilArray.isEmpty()) {
                log.warn("No stations in API response");
                return;
            }
            
            List<GasStationCacheDto> stations = new ArrayList<>();
            
            for (JsonNode node : oilArray) {
                // KATEC 좌표를 WGS84로 변환
                double katecX = node.path("GIS_X_COOR").asDouble();
                double katecY = node.path("GIS_Y_COOR").asDouble();
                double[] wgs84 = coordinateConverter.katecToWgs84(katecX, katecY);
                
                GasStationCacheDto dto = new GasStationCacheDto();
                dto.setId(node.path("UNI_ID").asText());
                dto.setName(node.path("OS_NM").asText());
                dto.setKatecX(node.path("GIS_X_COOR").asText());
                dto.setKatecY(node.path("GIS_Y_COOR").asText());
                dto.setDistance(node.path("DISTANCE").asText());
                dto.setPrice(node.path("PRICE").asText());
                dto.setLongitude(wgs84[0]);
                dto.setLatitude(wgs84[1]);
                dto.setBrand(node.path("POLL_DIV_CD").asText());
                dto.setCachedAt(System.currentTimeMillis());
                
                stations.add(dto);
            }
            
            // 글로벌 캐시에 저장 (GEO 인덱싱)
            globalCacheService.cacheGasStations(stations);
            
            // 사용자 세션 캐시에 저장
            userCacheService.cacheGasStations(sessionId, stations);
            
            log.info("Cached {} stations from API response", stations.size());
            
        } catch (Exception e) {
            log.error("Failed to cache API response: ", e);
        }
    }

    /**
     * 제품 코드와 정렬 기준으로 필터링 및 정렬
     */
    private List<GasStationCacheDto> filterAndSort(List<GasStationCacheDto> stations, String prodcd, int sort) {
        // 실제로는 제품 코드별 가격 정보를 별도로 저장해야 하지만,
        // 현재 구조에서는 간단히 처리
        List<GasStationCacheDto> result = new ArrayList<>(stations);
        
        // 정렬
        if (sort == 1) {
            // 가격순
            result.sort((a, b) -> {
                try {
                    double priceA = Double.parseDouble(a.getPrice());
                    double priceB = Double.parseDouble(b.getPrice());
                    return Double.compare(priceA, priceB);
                } catch (NumberFormatException e) {
                    return 0;
                }
            });
        } else if (sort == 2) {
            // 거리순
            result.sort((a, b) -> {
                try {
                    double distA = Double.parseDouble(a.getDistance());
                    double distB = Double.parseDouble(b.getDistance());
                    return Double.compare(distA, distB);
                } catch (NumberFormatException e) {
                    return 0;
                }
            });
        }
        
        return result;
    }

    /**
     * 주유소 목록을 JSON 문자열로 변환
     */
    private String convertStationsToJson(List<GasStationCacheDto> stations) {
        try {
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> resultNode = new HashMap<>();
            
            List<Map<String, String>> oilList = new ArrayList<>();
            for (GasStationCacheDto station : stations) {
                Map<String, String> stationMap = new HashMap<>();
                stationMap.put("UNI_ID", station.getId());
                stationMap.put("OS_NM", station.getName());
                stationMap.put("GIS_X_COOR", station.getKatecX());
                stationMap.put("GIS_Y_COOR", station.getKatecY());
                stationMap.put("DISTANCE", station.getDistance());
                stationMap.put("PRICE", station.getPrice());
                stationMap.put("POLL_DIV_CD", station.getBrand() != null ? station.getBrand() : "");
                oilList.add(stationMap);
            }
            
            resultNode.put("OIL", oilList);
            result.put("RESULT", resultNode);
            
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            log.error("Failed to convert stations to JSON: ", e);
            return "{\"RESULT\":{\"OIL\":[]}}";
        }
    }


}
