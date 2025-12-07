package com.spring.searchGasStation.dto.user.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettingsRequestDto {
    private String fuelType;
    private int radius;
    private int sortType;
}
