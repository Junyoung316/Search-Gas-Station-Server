package com.spring.searchGasStation.application.redis;

import com.spring.searchGasStation.dto.GasStationCacheDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 글로벌 캐시 레이어 - 모든 사용자가 공유하는 주유소 데이터
 * Redis GEO를 활용한 지리 공간 인덱싱 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalGasStationCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    // 글로벌 캐시 키
    private static final String GLOBAL_GEO_KEY = "global:gas_stations:geo";
    private static final String GLOBAL_DATA_PREFIX = "global:gas_stations:data:";
    private static final String GRID_CACHE_PREFIX = "global:gas_stations:grid:";
    
    // 캐시 만료 시간 (10분 - 주유소 가격은 자주 변동)
    private static final long CACHE_EXPIRATION = 10;
    
    // 그리드 크기 (약 1km 단위)
    private static final double GRID_SIZE = 0.01; // 위도/경도 약 1km

    /**
     * 주유소 데이터를 글로벌 캐시에 저장 (GEO 인덱싱 포함)
     */
    public void cacheGasStations(List<GasStationCacheDto> stations) {
        if (stations == null || stations.isEmpty()) {
            return;
        }
        
        try {
            GeoOperations<String, Object> geoOps = redisTemplate.opsForGeo();
            
            for (GasStationCacheDto station : stations) {
                if (station.getLongitude() != null && station.getLatitude() != null) {
                    // 1. GEO 인덱스에 위치 정보 저장
                    Point location = new Point(station.getLongitude(), station.getLatitude());
                    geoOps.add(GLOBAL_GEO_KEY, location, station.getId());
                    
                    // 2. 상세 데이터 저장
                    String dataKey = GLOBAL_DATA_PREFIX + station.getId();
                    redisTemplate.opsForValue().set(dataKey, station, CACHE_EXPIRATION, TimeUnit.MINUTES);
                    
                    log.debug("Cached station: {} at ({}, {})", 
                            station.getName(), station.getLongitude(), station.getLatitude());
                }
            }
            
            // GEO 키에도 만료 시간 설정
            redisTemplate.expire(GLOBAL_GEO_KEY, CACHE_EXPIRATION, TimeUnit.MINUTES);
            
            log.info("Global cache updated with {} stations", stations.size());
            
        } catch (Exception e) {
            log.error("Failed to cache gas stations to global cache: ", e);
        }
    }

    /**
     * 특정 위치 주변의 주유소를 GEO 쿼리로 검색
     * @param longitude 경도
     * @param latitude 위도
     * @param radiusKm 반경(km)
     * @return 주유소 목록
     */
    public List<GasStationCacheDto> findNearbyStations(double longitude, double latitude, double radiusKm) {
        try {
            GeoOperations<String, Object> geoOps = redisTemplate.opsForGeo();
            
            // GEO RADIUS 쿼리 실행
            Circle circle = new Circle(new Point(longitude, latitude), new Distance(radiusKm, RedisGeoCommands.DistanceUnit.KILOMETERS));
            GeoResults<RedisGeoCommands.GeoLocation<Object>> results = geoOps.radius(GLOBAL_GEO_KEY, circle);
            
            if (results == null) {
                log.debug("No stations found in global cache for location ({}, {}) within {}km", 
                        longitude, latitude, radiusKm);
                return Collections.emptyList();
            }
            
            // 결과에서 주유소 ID 추출 및 상세 데이터 조회
            List<GasStationCacheDto> stations = new ArrayList<>();
            
            results.getContent().forEach(result -> {
                String stationId = (String) result.getContent().getName();
                String dataKey = GLOBAL_DATA_PREFIX + stationId;
                
                GasStationCacheDto station = (GasStationCacheDto) redisTemplate.opsForValue().get(dataKey);
                if (station != null) {
                    // 거리 정보 업데이트
                    double distanceKm = result.getDistance().getValue();
                    station.setDistance(String.format("%.2f", distanceKm * 1000)); // 미터로 변환
                    stations.add(station);
                }
            });
            
            log.info("Found {} stations in global cache for location ({}, {}) within {}km", 
                    stations.size(), longitude, latitude, radiusKm);
            
            return stations;
            
        } catch (Exception e) {
            log.error("Failed to find nearby stations from global cache: ", e);
            return Collections.emptyList();
        }
    }

    /**
     * 특정 주유소 ID로 조회
     */
    public GasStationCacheDto getStationById(String stationId) {
        try {
            String dataKey = GLOBAL_DATA_PREFIX + stationId;
            return (GasStationCacheDto) redisTemplate.opsForValue().get(dataKey);
        } catch (Exception e) {
            log.error("Failed to get station by id from global cache: ", e);
            return null;
        }
    }

    /**
     * 주유소 이름으로 검색 (글로벌 캐시)
     * @param searchTerm 검색어
     * @return 검색 결과 목록
     */
    public List<GasStationCacheDto> searchByName(String searchTerm) {
        List<GasStationCacheDto> results = new ArrayList<>();
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return results;
        }
        
        try {
            // 모든 주유소 데이터 키 조회
            Set<String> dataKeys = redisTemplate.keys(GLOBAL_DATA_PREFIX + "*");
            
            if (dataKeys == null || dataKeys.isEmpty()) {
                log.debug("No stations in global cache for search");
                return results;
            }
            
            String searchLower = searchTerm.toLowerCase();
            
            // 각 주유소를 확인하며 이름 매칭
            for (String key : dataKeys) {
                try {
                    GasStationCacheDto station = (GasStationCacheDto) redisTemplate.opsForValue().get(key);
                    
                    if (station != null && station.getName() != null) {
                        if (station.getName().toLowerCase().contains(searchLower)) {
                            results.add(station);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse station data for key: {}", key);
                }
            }
            
            log.info("Global cache search '{}' found {} stations", searchTerm, results.size());
            
        } catch (Exception e) {
            log.error("Failed to search stations in global cache: ", e);
        }
        
        return results;
    }

    /**
     * 그리드 기반 캐시 확인 - 같은 지역을 검색했는지 확인
     * @return 캐시된 데이터가 있으면 true
     */
    public boolean hasGridCache(double longitude, double latitude, int radiusMeters, String prodcd) {
        String gridKey = generateGridKey(longitude, latitude, radiusMeters, prodcd);
        try {
            // Set의 크기를 확인
            Long size = redisTemplate.opsForSet().size(gridKey);
            return size != null && size > 0;
        } catch (Exception e) {
            log.error("Failed to check grid cache: ", e);
            return false;
        }
    }

    /**
     * 그리드 기반 캐시 저장
     */
    public void saveGridCache(double longitude, double latitude, int radiusMeters, String prodcd, List<String> stationIds) {
        String gridKey = generateGridKey(longitude, latitude, radiusMeters, prodcd);
        try {
            // Redis Set으로 저장하여 직렬화 문제 방지
            redisTemplate.delete(gridKey);
            if (stationIds != null && !stationIds.isEmpty()) {
                redisTemplate.opsForSet().add(gridKey, stationIds.toArray());
                redisTemplate.expire(gridKey, CACHE_EXPIRATION, TimeUnit.MINUTES);
            }
            log.debug("Saved grid cache: {} with {} stations", gridKey, stationIds.size());
        } catch (Exception e) {
            log.error("Failed to save grid cache: ", e);
        }
    }

    /**
     * 그리드 기반 캐시 조회
     */
    public List<GasStationCacheDto> getGridCache(double longitude, double latitude, int radiusMeters, String prodcd) {
        String gridKey = generateGridKey(longitude, latitude, radiusMeters, prodcd);
        try {
            // Set에서 모든 멤버 조회
            Set<Object> stationIdSet = redisTemplate.opsForSet().members(gridKey);
            
            if (stationIdSet == null || stationIdSet.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 주유소 ID로 상세 데이터 조회
            List<GasStationCacheDto> results = new ArrayList<>();
            for (Object obj : stationIdSet) {
                if (obj instanceof String) {
                    String stationId = (String) obj;
                    GasStationCacheDto station = getStationById(stationId);
                    if (station != null) {
                        results.add(station);
                    }
                }
            }
            
            log.debug("Retrieved {} stations from grid cache: {}", results.size(), gridKey);
            return results;
                    
        } catch (Exception e) {
            log.error("Failed to get grid cache: ", e);
            return Collections.emptyList();
        }
    }

    /**
     * 그리드 키 생성 - 같은 지역을 검색하면 같은 키가 나옴
     */
    private String generateGridKey(double longitude, double latitude, int radiusMeters, String prodcd) {
        // 좌표를 그리드로 정규화 (약 1km 단위)
        long gridX = Math.round(longitude / GRID_SIZE);
        long gridY = Math.round(latitude / GRID_SIZE);
        
        return String.format("%s%d:%d:%d:%s", GRID_CACHE_PREFIX, gridX, gridY, radiusMeters, prodcd);
    }

    /**
     * 특정 주유소의 캐시 데이터 갱신
     */
    public void updateStation(GasStationCacheDto station) {
        if (station == null || station.getId() == null) {
            return;
        }
        
        try {
            String dataKey = GLOBAL_DATA_PREFIX + station.getId();
            station.setCachedAt(System.currentTimeMillis());
            redisTemplate.opsForValue().set(dataKey, station, CACHE_EXPIRATION, TimeUnit.MINUTES);
            
            log.debug("Updated station in global cache: {}", station.getName());
        } catch (Exception e) {
            log.error("Failed to update station in global cache: ", e);
        }
    }

    /**
     * 전체 글로벌 캐시 삭제 (관리 목적)
     */
    public void clearAllCache() {
        try {
            Set<String> dataKeys = redisTemplate.keys(GLOBAL_DATA_PREFIX + "*");
            Set<String> gridKeys = redisTemplate.keys(GRID_CACHE_PREFIX + "*");
            
            if (dataKeys != null && !dataKeys.isEmpty()) {
                redisTemplate.delete(dataKeys);
                log.info("Deleted {} data keys", dataKeys.size());
            }
            if (gridKeys != null && !gridKeys.isEmpty()) {
                redisTemplate.delete(gridKeys);
                log.info("Deleted {} grid keys", gridKeys.size());
            }
            
            redisTemplate.delete(GLOBAL_GEO_KEY);
            
            log.info("Cleared all global cache");
        } catch (Exception e) {
            log.error("Failed to clear global cache: ", e);
        }
    }

    /**
     * 손상된 그리드 캐시 정리 (직렬화 오류 해결용)
     */
    public void cleanupCorruptedGridCache() {
        try {
            Set<String> gridKeys = redisTemplate.keys(GRID_CACHE_PREFIX + "*");
            
            if (gridKeys != null && !gridKeys.isEmpty()) {
                int cleaned = 0;
                for (String key : gridKeys) {
                    try {
                        // Set으로 읽을 수 있는지 확인
                        redisTemplate.opsForSet().members(key);
                    } catch (Exception e) {
                        // 읽기 실패하면 손상된 데이터로 판단하여 삭제
                        redisTemplate.delete(key);
                        cleaned++;
                        log.debug("Cleaned corrupted grid cache: {}", key);
                    }
                }
                
                log.info("Cleaned {} corrupted grid cache keys", cleaned);
            }
        } catch (Exception e) {
            log.error("Failed to cleanup corrupted grid cache: ", e);
        }
    }

    /**
     * 캐시 통계 조회
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // GEO 인덱스의 항목 수 (GEO는 내부적으로 Sorted Set이므로 ZCARD 사용)
            Long geoCount = redisTemplate.opsForZSet().size(GLOBAL_GEO_KEY);
            stats.put("geoIndexSize", geoCount != null ? geoCount : 0);
            
            // 데이터 키 수
            Set<String> dataKeys = redisTemplate.keys(GLOBAL_DATA_PREFIX + "*");
            stats.put("cachedStations", dataKeys != null ? dataKeys.size() : 0);
            
            // 그리드 캐시 수
            Set<String> gridKeys = redisTemplate.keys(GRID_CACHE_PREFIX + "*");
            stats.put("gridCacheCount", gridKeys != null ? gridKeys.size() : 0);
            
            // TTL 정보 추가
            Long ttl = redisTemplate.getExpire(GLOBAL_GEO_KEY, TimeUnit.SECONDS);
            stats.put("geoIndexTTL", ttl != null ? ttl : -1);
            
        } catch (Exception e) {
            log.error("Failed to get cache stats: ", e);
        }
        
        return stats;
    }
}
