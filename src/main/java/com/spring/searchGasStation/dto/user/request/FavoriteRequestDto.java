package com.spring.searchGasStation.dto.user.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteRequestDto {
    private String stationCode; // 오피넷 유니크 ID
    private String name;
    private String brand;
    private String address;
}