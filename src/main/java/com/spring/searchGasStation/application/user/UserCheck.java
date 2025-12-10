package com.spring.searchGasStation.application.user;

import com.spring.searchGasStation.domain.entity.Member;
import com.spring.searchGasStation.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCheck {

    private final MemberRepository memberRepository;

    public Member getUserCheck() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName();  // 사용자 식별자(ID) 조회
            return memberRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        }
        return null;
    }

    public boolean getUserCheckIsFavorite() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName();  // 사용자 식별자(ID) 조회
            return memberRepository.existsByEmail(userEmail);
        }
        return false;
    }

}
