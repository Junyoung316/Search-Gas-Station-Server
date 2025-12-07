package com.spring.searchGasStation.domain.repository;

import com.spring.searchGasStation.domain.entity.Member;
import com.spring.searchGasStation.domain.entity.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {
    Optional<ProfileImage> findByMember(Member member);
}
