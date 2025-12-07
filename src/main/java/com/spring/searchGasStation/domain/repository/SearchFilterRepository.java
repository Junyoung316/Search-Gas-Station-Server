package com.spring.searchGasStation.domain.repository;

import com.spring.searchGasStation.domain.entity.Member;
import com.spring.searchGasStation.domain.entity.SearchFilter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SearchFilterRepository extends JpaRepository<SearchFilter, Long> {
    Optional<SearchFilter> findByMember(Member member);
}
