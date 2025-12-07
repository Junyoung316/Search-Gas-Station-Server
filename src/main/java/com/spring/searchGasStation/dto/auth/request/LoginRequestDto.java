package com.spring.searchGasStation.dto.auth.request;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String email;
    private String password;
}