package com.spring.searchGasStation.dto.auth.request;

import lombok.Data;

@Data
public class PasswordUpdateDto {
    private String currentPassword;
    private String newPassword;
    private String confirmNewPassword;
}
