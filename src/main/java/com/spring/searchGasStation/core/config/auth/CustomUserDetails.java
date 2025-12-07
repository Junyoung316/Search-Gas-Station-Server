package com.spring.searchGasStation.core.config.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {

    private final String nickname; // 우리가 추가하고 싶은 필드

    // 생성자
    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, String nickname) {
        super(username, password, authorities); // 부모(User) 초기화
        this.nickname = nickname; // 내 필드 초기화
    }

}
