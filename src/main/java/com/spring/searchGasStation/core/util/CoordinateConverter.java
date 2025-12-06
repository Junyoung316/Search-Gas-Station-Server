package com.spring.searchGasStation.core.util;

import lombok.extern.slf4j.Slf4j;
import org.locationtech.proj4j.*;
import org.springframework.stereotype.Component;

/**
 * 좌표계 변환 유틸리티
 * KATEC (Korea TM) ↔ WGS84 변환
 */
@Slf4j
@Component
public class CoordinateConverter {

    private final CRSFactory crsFactory;
    private final CoordinateTransformFactory ctFactory;
    private final CoordinateTransform katecToWgs84;
    private final CoordinateTransform wgs84ToKatec;

    // KATEC 좌표계 정의 (중부원점 기준)
    private static final String KATEC_PROJ = 
        "+proj=tmerc +lat_0=38 +lon_0=128 +k=0.9999 +x_0=400000 +y_0=600000 " +
        "+ellps=bessel +towgs84=-115.80,474.99,674.11,1.16,-2.31,-1.63,6.43 " +
        "+units=m +no_defs";

    // WGS84 좌표계 정의
    private static final String WGS84_PROJ = "+proj=longlat +datum=WGS84 +no_defs";

    public CoordinateConverter() {
        this.crsFactory = new CRSFactory();
        this.ctFactory = new CoordinateTransformFactory();

        try {
            CoordinateReferenceSystem katecCrs = crsFactory.createFromParameters("KATEC", KATEC_PROJ);
            CoordinateReferenceSystem wgs84Crs = crsFactory.createFromParameters("WGS84", WGS84_PROJ);

            this.katecToWgs84 = ctFactory.createTransform(katecCrs, wgs84Crs);
            this.wgs84ToKatec = ctFactory.createTransform(wgs84Crs, katecCrs);

            log.info("CoordinateConverter initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize CoordinateConverter", e);
            throw new RuntimeException("좌표계 변환 초기화 실패", e);
        }
    }

    /**
     * KATEC 좌표를 WGS84로 변환
     * @param katecX KATEC X 좌표 (미터)
     * @param katecY KATEC Y 좌표 (미터)
     * @return [경도, 위도] (도 단위)
     */
    public double[] katecToWgs84(double katecX, double katecY) {
        try {
            ProjCoordinate srcCoord = new ProjCoordinate(katecX, katecY);
            ProjCoordinate dstCoord = new ProjCoordinate();

            katecToWgs84.transform(srcCoord, dstCoord);

            double longitude = dstCoord.x;
            double latitude = dstCoord.y;

            log.debug("KATEC ({}, {}) -> WGS84 ({}, {})", katecX, katecY, longitude, latitude);

            return new double[]{longitude, latitude};
        } catch (Exception e) {
            log.error("Failed to convert KATEC to WGS84: ({}, {})", katecX, katecY, e);
            
            // 실패 시 간단한 근사식 사용
            return approximateKatecToWgs84(katecX, katecY);
        }
    }

    /**
     * WGS84 좌표를 KATEC으로 변환
     * @param longitude 경도 (도 단위)
     * @param latitude 위도 (도 단위)
     * @return [KATEC X, KATEC Y] (미터 단위)
     */
    public double[] wgs84ToKatec(double longitude, double latitude) {
        try {
            ProjCoordinate srcCoord = new ProjCoordinate(longitude, latitude);
            ProjCoordinate dstCoord = new ProjCoordinate();

            wgs84ToKatec.transform(srcCoord, dstCoord);

            double katecX = dstCoord.x;
            double katecY = dstCoord.y;

            log.debug("WGS84 ({}, {}) -> KATEC ({}, {})", longitude, latitude, katecX, katecY);

            return new double[]{katecX, katecY};
        } catch (Exception e) {
            log.error("Failed to convert WGS84 to KATEC: ({}, {})", longitude, latitude, e);
            
            // 실패 시 간단한 근사식 사용
            return approximateWgs84ToKatec(longitude, latitude);
        }
    }

    /**
     * KATEC -> WGS84 근사 변환 (fallback)
     */
    private double[] approximateKatecToWgs84(double x, double y) {
        double originLon = 128.0;
        double originLat = 38.0;
        
        double deltaLon = (x - 400000) / 88800.0;  // 중부원점 위도에서 경도 1도 ≈ 88.8km
        double deltaLat = (y - 600000) / 110540.0; // 위도 1도 ≈ 110.54km
        
        double longitude = originLon + deltaLon;
        double latitude = originLat + deltaLat;
        
        log.warn("Using approximate conversion for KATEC ({}, {}) -> WGS84 ({}, {})", 
                x, y, longitude, latitude);
        
        return new double[]{longitude, latitude};
    }

    /**
     * WGS84 -> KATEC 근사 변환 (fallback)
     */
    private double[] approximateWgs84ToKatec(double longitude, double latitude) {
        double originLon = 128.0;
        double originLat = 38.0;
        
        double deltaLon = longitude - originLon;
        double deltaLat = latitude - originLat;
        
        double x = 400000 + (deltaLon * 88800.0);
        double y = 600000 + (deltaLat * 110540.0);
        
        log.warn("Using approximate conversion for WGS84 ({}, {}) -> KATEC ({}, {})", 
                longitude, latitude, x, y);
        
        return new double[]{x, y};
    }

    /**
     * 두 WGS84 좌표 간의 거리 계산 (Haversine 공식)
     * @param lon1 경도1
     * @param lat1 위도1
     * @param lon2 경도2
     * @param lat2 위도2
     * @return 거리(미터)
     */
    public double calculateDistance(double lon1, double lat1, double lon2, double lat2) {
        final double R = 6371000; // 지구 반경 (미터)
        
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
}
