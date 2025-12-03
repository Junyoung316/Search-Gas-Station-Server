package com.spring.searchGasStation.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email; // 아이디로 사용

    @Column(length = 200, nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    private String role; // "ROLE_USER", "ROLE_ADMIN"

//    @Builder
//    public Member(String email, String password, String role) {
//        this.email = email;
//        this.password = password;
//        this.role = role;
//    }

}
