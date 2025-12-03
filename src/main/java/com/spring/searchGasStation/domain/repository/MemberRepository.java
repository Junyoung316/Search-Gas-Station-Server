package com.spring.searchGasStation.domain.repository;

import com.spring.searchGasStation.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email); // 이메일로 회원 찾기 (로그인 시 필요)
    boolean existsByEmail(String email); // 중복 가입 방지용
}
