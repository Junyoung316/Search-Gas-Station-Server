package com.spring.searchGasStation.domain.entity;

import com.spring.searchGasStation.core.auditing.BaseTimeEntity;
import com.spring.searchGasStation.domain.entity.enums.MemberStatus;
import com.spring.searchGasStation.domain.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
@AllArgsConstructor
@Builder
@SQLRestriction("status <> 'DELETED'")
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // 암호화된 비밀번호 저장

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private Role role; // USER, ADMIN

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void withdraw() {
        this.status = MemberStatus.DELETED; // 탈퇴 상태로 변경
    }
}