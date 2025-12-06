package com.spring.searchGasStation.presentation.auth;

import com.spring.searchGasStation.application.auth.AuthService;
import com.spring.searchGasStation.core.dto.MainResponse;
import com.spring.searchGasStation.dto.auth.request.LoginRequestDto;
import com.spring.searchGasStation.dto.auth.request.SignupRequestDto;
import com.spring.searchGasStation.dto.auth.response.LoginResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 회원가입 API
    @PostMapping("/api/auth/signup")
    public ResponseEntity<MainResponse<String>> signup(@RequestBody SignupRequestDto requestDto) {
        authService.signup(requestDto);
        return ResponseEntity.ok(MainResponse.success("회원가입 성공"));
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<MainResponse<LoginResponseDto>> login(@RequestBody LoginRequestDto requestDto) {
        LoginResponseDto response = authService.login(requestDto);
        return ResponseEntity.ok(MainResponse.success(response));
    }
}
