package com.spring.searchGasStation.application.redis;

import com.spring.searchGasStation.dto.GasStationCacheDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 사용자별 세션 캐시 서비스
 * 사용자가 최근 검색한 주유소 목록을 세션별로 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GasStationCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CACHE_PREFIX = "user:gas_station:";
    private static final long CACHE_EXPIRATION = 30; // 30분

    /**
     * 주유소 데이터를 사용자 세션 캐시에 저장
     */
    public void cacheGasStations(String userKey, List<GasStationCacheDto> stations) {
        String key = CACHE_PREFIX + userKey;
        
        try {
            // 기존 데이터 삭제
            redisTemplate.delete(key);
            
            // 새 데이터 저장
            for (GasStationCacheDto station : stations) {
                redisTemplate.opsForHash().put(key, station.getId(), station);
            }
            
            // 만료 시간 설정 (30분)
            redisTemplate.expire(key, CACHE_EXPIRATION, TimeUnit.MINUTES);
            
            log.info("Cached {} gas stations for user session: {}", stations.size(), userKey);
        } catch (Exception e) {
            log.error("Failed to cache gas stations for user: ", e);
        }
    }

    /**
     * 주유소 이름으로 검색
     */
    public List<GasStationCacheDto> searchByName(String userKey, String searchTerm) {
        String key = CACHE_PREFIX + userKey;
        List<GasStationCacheDto> results = new ArrayList<>();
        
        try {
            Set<Object> keys = redisTemplate.opsForHash().keys(key);
            
            for (Object hashKey : keys) {
                GasStationCacheDto station = (GasStationCacheDto) redisTemplate.opsForHash().get(key, hashKey);
                if (station != null && station.getName() != null) {
                    if (station.getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                        results.add(station);
                    }
                }
            }
            
            log.info("Found {} stations matching '{}' for user: {}", results.size(), searchTerm, userKey);
        } catch (Exception e) {
            log.error("Failed to search gas stations: ", e);
        }
        
        return results;
    }

    /**
     * 특정 주유소 ID로 조회 (사용자 세션에서)
     */
    public GasStationCacheDto getStationById(String userKey, String stationId) {
        String key = CACHE_PREFIX + userKey;
        
        try {
            return (GasStationCacheDto) redisTemplate.opsForHash().get(key, stationId);
        } catch (Exception e) {
            log.error("Failed to get gas station by id: ", e);
            return null;
        }
    }

    /**
     * 모든 캐시된 주유소 조회 (사용자 세션)
     */
    public List<GasStationCacheDto> getAllStations(String userKey) {
        String key = CACHE_PREFIX + userKey;
        List<GasStationCacheDto> results = new ArrayList<>();
        
        try {
            List<Object> values = redisTemplate.opsForHash().values(key);
            for (Object value : values) {
                if (value instanceof GasStationCacheDto) {
                    results.add((GasStationCacheDto) value);
                }
            }
        } catch (Exception e) {
            log.error("Failed to get all gas stations: ", e);
        }
        
        return results;
    }

    /**
     * 사용자 캐시 삭제
     */
    public void clearCache(String userKey) {
        String key = CACHE_PREFIX + userKey;
        redisTemplate.delete(key);
        log.info("Cleared cache for user: {}", userKey);
    }
}
