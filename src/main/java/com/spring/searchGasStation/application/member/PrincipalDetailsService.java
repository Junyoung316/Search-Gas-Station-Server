package com.spring.searchGasStation.application.member;

import com.spring.searchGasStation.core.config.auth.CustomUserDetails;
import com.spring.searchGasStation.domain.entity.Member;
import com.spring.searchGasStation.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("======================================");
        System.out.println("✅ [1] 로그인 시도 이메일: " + email);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("❌ [2] DB에서 회원을 못 찾음 (이메일 불일치)");
                    return new UsernameNotFoundException("해당 이메일의 회원을 찾을 수 없습니다: " + email);
                });

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(member.getRole()));

        System.out.println("✅ [3] DB 회원 찾음! 비밀번호: [" + member.getPassword() + "]");
        System.out.println("✅ [4] 부여할 권한: " + member.getRole());
        System.out.println("======================================");

        return new CustomUserDetails(
                member.getEmail(),      // username
                member.getPassword(),   // password
                authorities,            // authorities
                member.getNickname()    // ★ nickname 전달
        );
    }
}
