package com.spring.searchGasStation.application.filter;

import com.spring.searchGasStation.domain.entity.Member;
import com.spring.searchGasStation.domain.entity.SearchFilter;
import com.spring.searchGasStation.domain.repository.MemberRepository;
import com.spring.searchGasStation.domain.repository.SearchFilterRepository;
import com.spring.searchGasStation.dto.FilterDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FilterService {
    private final SearchFilterRepository filterRepository;
    private final MemberRepository memberRepository;

    // 1. 필터 설정 저장 또는 업데이트
    public void saveFilter(String email, FilterDto dto) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        SearchFilter filter = filterRepository.findByMemberId(member.getId())
                .orElse(null);

        if (filter == null) {
            // 없으면 새로 생성
            filter = SearchFilter.builder()
                    .member(member)
                    .radius(dto.getRadius())
                    .fuelType(dto.getFuelType())
                    .sortType(dto.getSortType())
                    .build();
            filterRepository.save(filter);
        } else {
            // 있으면 내용만 수정 (Dirty Checking)
            filter.updateFilter(dto.getRadius(), dto.getFuelType(), dto.getSortType());
        }
    }

    // 2. 필터 설정 가져오기 (없으면 기본값 반환)
    @Transactional(readOnly = true)
    public FilterDto getFilter(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        return filterRepository.findByMemberId(member.getId())
                .map(f -> {
                    FilterDto dto = new FilterDto();
                    dto.setRadius(f.getRadius());
                    dto.setFuelType(f.getFuelType());
                    dto.setSortType(f.getSortType());
                    return dto;
                })
                .orElseGet(() -> {
                    // 저장된 설정이 없을 때 반환할 기본값 (Default)
                    FilterDto defaultDto = new FilterDto();
                    defaultDto.setRadius(3000); // 기본 3km
                    defaultDto.setFuelType("B027"); // 기본 휘발유
                    defaultDto.setSortType(1); // 기본 가격순
                    return defaultDto;
                });
    }
}
