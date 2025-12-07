package com.spring.searchGasStation.presentation.auth;

import com.spring.searchGasStation.application.auth.AuthService;
import com.spring.searchGasStation.core.dto.MainResponse;
import com.spring.searchGasStation.core.dto.TokenResponse;
import com.spring.searchGasStation.dto.auth.request.LoginRequestDto;
import com.spring.searchGasStation.dto.auth.request.PasswordUpdateDto;
import com.spring.searchGasStation.dto.auth.request.SignupRequestDto;
import com.spring.searchGasStation.dto.auth.response.LoginResponseDto;
import com.sun.tools.javac.Main;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
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

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<MainResponse<TokenResponse>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        return ResponseEntity.ok(MainResponse.success(authService.refreshToken(refreshToken)));
    }

    @PutMapping("/api/member/password")
    public ResponseEntity<MainResponse<String>> updatePassword(@RequestBody PasswordUpdateDto passwordUpdateDto) {
        authService.updatePassword(passwordUpdateDto);
        return ResponseEntity.ok(MainResponse.success());
    }

    @DeleteMapping("/api/member/withdraw")
    public ResponseEntity<MainResponse<String>> withdraw() {
        authService.withdraw();
        return ResponseEntity.ok(MainResponse.success());
    }
}
