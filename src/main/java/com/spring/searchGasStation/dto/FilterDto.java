package com.spring.searchGasStation.dto;

import lombok.Data;

@Data
public class FilterDto {
    private int radius;
    private String fuelType;
    private int sortType;
}
