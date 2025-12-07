package com.spring.searchGasStation.application.auth;

import com.spring.searchGasStation.domain.entity.Member;
import com.spring.searchGasStation.domain.entity.enums.Role;
import com.spring.searchGasStation.dto.auth.request.SignupRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    void signup() {

        SignupRequestDto  requestDto = SignupRequestDto.builder()
                .email("test123@test.com")
                .password("1234")
                .checkPassword("1234")
                .nickname("test")
                .build();

        authService.signup(requestDto);
    }


}