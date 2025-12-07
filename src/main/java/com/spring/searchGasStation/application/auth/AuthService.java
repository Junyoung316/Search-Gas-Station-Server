package com.spring.searchGasStation.application.auth;

import com.spring.searchGasStation.application.user.UserCheck;
import com.spring.searchGasStation.core.dto.TokenResponse;
import com.spring.searchGasStation.core.util.jwt.JwtService;
import com.spring.searchGasStation.domain.entity.Member;
import com.spring.searchGasStation.domain.entity.SearchFilter;
import com.spring.searchGasStation.domain.entity.enums.MemberStatus;
import com.spring.searchGasStation.domain.repository.MemberRepository;
import com.spring.searchGasStation.domain.repository.SearchFilterRepository;
import com.spring.searchGasStation.dto.auth.request.LoginRequestDto;
import com.spring.searchGasStation.dto.auth.request.PasswordUpdateDto;
import com.spring.searchGasStation.dto.auth.request.SignupRequestDto;
import com.spring.searchGasStation.dto.auth.response.LoginResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final SearchFilterRepository searchFilterRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserCheck userCheck;
    private final JwtService jwtService;

    @Transactional
    public void signup(SignupRequestDto requestDto) {
        // 1. 비밀번호 일치 확인 (서버측 2차 검증)
        if (!requestDto.getPassword().equals(requestDto.getCheckPassword())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        if(memberRepository.existsByNickname(requestDto.getNickname())) {
            throw new IllegalArgumentException("중복된 닉네임입니다.");
        }

        // 2. 이메일 중복 검사
        if (memberRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 3. 저장
        Member member = requestDto.toEntity(passwordEncoder);
        member.setStatus(MemberStatus.ACTIVE);
        Member saveMember = memberRepository.save(member);

        SearchFilter searchFilter = SearchFilter.builder()
                .member(saveMember)
                .fuelType("B027")
                .radius(3000)
                .sortType(1)
                .build();
        searchFilterRepository.save(searchFilter);
    }

    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto requestDto) {
        // 1. 이메일 존재 확인
        Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        /* * [참고]
         * @SQLRestriction을 적용했으므로, 아래와 같은 상태 체크 코드는
         * 'INACTIVE'(휴면) 상태 등을 체크할 때만 유효합니다.
         * 'DELETED' 상태는 이미 위 1번에서 걸러집니다.
         */
        if (member.getStatus() == MemberStatus.INACTIVE) {
            throw new IllegalArgumentException("휴면 계정입니다. 관리자에게 문의하세요.");
        }

        // 2. 비밀번호 일치 확인
        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 토큰 생성
        TokenResponse token = jwtService.generateTokens(requestDto.getEmail());

        // 4. 응답 반환
        return new LoginResponseDto(token, member.getNickname(), member.getEmail());
    }

    public TokenResponse refreshToken(String refreshToken) {
        if(refreshToken == null) {
            throw new NullPointerException("[JWT] Refresh Token을 확인해 주세요.");
        }
        String tokenType = jwtService.extractFromToken(refreshToken, "tokenType", String.class);
        if(!tokenType.equals("refresh")) {
            throw new IllegalArgumentException("Refresh 토큰이 아닙니다.");
        }
        boolean val = jwtService.validateJwtToken(refreshToken);
        if(val) {
            String email = jwtService.extractEmailFromToken(refreshToken);
            log.info("email: {}", email);
            return jwtService.generateTokens(email);
        }
        return null;
    }

    @Transactional
    public void updatePassword(PasswordUpdateDto passwordUpdateDto) {
        Member member = userCheck.getUserCheck();

        if(passwordUpdateDto.getNewPassword().equals(passwordUpdateDto.getConfirmNewPassword())) {
            if (!passwordEncoder.matches(passwordUpdateDto.getCurrentPassword(), member.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }
            member.updatePassword(passwordEncoder.encode(passwordUpdateDto.getNewPassword()));
            memberRepository.save(member);
        }
    }

    @Transactional
    public void withdraw() {
        Member member = userCheck.getUserCheck();
        member.withdraw(); // 상태를 DELETED로 변경
        memberRepository.save(member);
    }
}