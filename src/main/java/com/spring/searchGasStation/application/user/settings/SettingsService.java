package com.spring.searchGasStation.application.user.settings;

import com.spring.searchGasStation.application.user.UserCheck;
import com.spring.searchGasStation.domain.entity.Member;
import com.spring.searchGasStation.domain.entity.SearchFilter;
import com.spring.searchGasStation.domain.repository.SearchFilterRepository;
import com.spring.searchGasStation.dto.user.request.UserSettingsRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsService {

    private final UserCheck userCheck;

    private final SearchFilterRepository searchFilterRepository;

    public UserSettingsRequestDto getMySettings() {

        Member user = userCheck.getUserCheck();

        log.info("user: {}", user.getId());

        SearchFilter searchFilter = searchFilterRepository.findByMember(user)
                .orElseThrow(() -> new RuntimeException("설정을 찾을 수 없습니다."));

        log.info("searchFilter: {}", searchFilter);

        return UserSettingsRequestDto.builder()
                .fuelType(searchFilter.getFuelType())
                .radius(searchFilter.getRadius())
                .sortType(searchFilter.getSortType())
                .build();
    }

    @Transactional
    public void settings(UserSettingsRequestDto requestDto) {

        Member user = userCheck.getUserCheck();

        SearchFilter filter = searchFilterRepository.findByMember(user)
                .orElseThrow(() -> new RuntimeException("설정을 찾을 수 없습니다."));
        filter.setFuelType(requestDto.getFuelType());
        filter.setRadius(requestDto.getRadius());
        filter.setSortType(requestDto.getSortType());

        searchFilterRepository.save(filter);
    }

}
