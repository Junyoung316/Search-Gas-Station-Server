package com.spring.searchGasStation.dto.auth.response;

import com.spring.searchGasStation.core.dto.TokenResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDto {
    private TokenResponse token;    // JWT 토큰
    private String nickname; // 사용자 닉네임 (프론트 표시용)
    private String email;    // 사용자 이메일
}
