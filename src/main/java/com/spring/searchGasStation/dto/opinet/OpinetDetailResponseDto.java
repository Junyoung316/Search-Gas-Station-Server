package com.spring.searchGasStation.dto.opinet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpinetDetailResponseDto {
    @JsonProperty("RESULT")
    private Result result;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        // 상세 조회여도 OIL 배열 안에 객체가 하나 들어옵니다.
        @JsonProperty("OIL")
        private List<StationDetailDto> stationDetails;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StationDetailDto {

        // --- 1. 기본 정보 ---
        @JsonProperty("UNI_ID")
        private String uniId;
        @JsonProperty("OS_NM")
        private String name;

        @JsonProperty("POLL_DIV_CO") // 주유소 상표 코드
        private String brandCode;
        @JsonProperty("GIS_X_COOR")
        private double katecX;
        @JsonProperty("GIS_Y_COOR")
        private double katecY;

        // --- 2. 주소 및 연락처 ---
        @JsonProperty("VAN_ADR")
        private String vanAddress; // 구 주소
        @JsonProperty("NEW_ADR")
        private String newAddress; // 새 주소
        @JsonProperty("TEL")
        private String tel;

        // --- 3. 부가 서비스 Flags (Y/N) ---
        @JsonProperty("WASH_YN")
        private String washYn; // 세차장 유무
        @JsonProperty("CONV_YN")
        private String convYn; // 편의점 유무
        @JsonProperty("MAINT_YN")
        private String maintYn; // 경정비 유무
        @JsonProperty("LPG_YN")
        private String lpgYn; // LPG 충전소 병설 유무

        // --- 4. ★ 유종별 가격 리스트 (핵심) ---
        @JsonProperty("OIL_PRICE")
        private List<OilPriceDto> oilPrices;
    }
}
