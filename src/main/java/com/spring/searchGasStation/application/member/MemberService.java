package com.spring.searchGasStation.application.member;

import com.spring.searchGasStation.domain.entity.Member;
import com.spring.searchGasStation.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder; // SecurityConfig에서 등록한 Bean

    public void join(String email, String password, String nickname) {
        // 1. 중복 체크
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }

        // 2. 비밀번호 암호화 (필수!)
        String encodedPassword = passwordEncoder.encode(password.trim());

        System.out.println("✅ DB 비밀번호: [" + password + "]");

        // 3. 저장
        Member member = Member.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .role("ROLE_USER")
                .build();

        memberRepository.save(member);
    }
}