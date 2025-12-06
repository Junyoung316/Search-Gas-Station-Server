package com.spring.searchGasStation.application.auth;

import com.spring.searchGasStation.core.dto.TokenResponse;
import com.spring.searchGasStation.core.util.jwt.JwtService;
import com.spring.searchGasStation.domain.entity.Member;
import com.spring.searchGasStation.domain.repository.MemberRepository;
import com.spring.searchGasStation.dto.auth.request.LoginRequestDto;
import com.spring.searchGasStation.dto.auth.request.SignupRequestDto;
import com.spring.searchGasStation.dto.auth.response.LoginResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public void signup(SignupRequestDto requestDto) {
        // 1. 비밀번호 일치 확인 (서버측 2차 검증)
        if (!requestDto.getPassword().equals(requestDto.getCheckPassword())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        // 2. 이메일 중복 검사
        if (memberRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 3. 저장
        Member member = requestDto.toEntity(passwordEncoder);
        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto requestDto) {
        // 1. 이메일 존재 확인
        Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // 2. 비밀번호 일치 확인
        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 토큰 생성
        TokenResponse token = jwtService.generateTokens(requestDto.getEmail());

        // 4. 응답 반환
        return new LoginResponseDto(token, member.getNickname(), member.getEmail());
    }
}