package com.spring.searchGasStation.dto.opinet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpinetResponseDto {
    @JsonProperty("RESULT")
    private Result result;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        @JsonProperty("OIL")
        private List<GasStationDto> oil;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GasStationDto {
        @JsonProperty("UNI_ID") private String uniId;
        @JsonProperty("OS_NM") private String name;
        @JsonProperty("PRICE") private int price;
        @JsonProperty("DISTANCE") private double distance;
        @JsonProperty("GIS_X_COOR") private double katecX;
        @JsonProperty("GIS_Y_COOR") private double katecY;
        @JsonProperty("POLL_DIV_CD") private String brand;
    }
}