package com.spring.searchGasStation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GasStationCacheDto implements Serializable {
    private String id;           // 주유소 ID (UNI_ID)
    private String name;         // 주유소 이름 (OS_NM)
    private String katecX;       // KATEC X 좌표
    private String katecY;       // KATEC Y 좌표
    private String distance;     // 거리
    private String price;        // 가격
    
    // 지리 공간 인덱싱을 위한 WGS84 좌표 추가
    private Double longitude;    // 경도 (WGS84)
    private Double latitude;     // 위도 (WGS84)
    private String brand;        // 브랜드 (POLL_DIV_CD)
    private Long cachedAt;       // 캐시 시간
    
    // 기존 생성자 유지를 위한 생성자
    public GasStationCacheDto(String id, String name, String katecX, String katecY, 
                              String distance, String price) {
        this.id = id;
        this.name = name;
        this.katecX = katecX;
        this.katecY = katecY;
        this.distance = distance;
        this.price = price;
        this.cachedAt = System.currentTimeMillis();
    }
}
