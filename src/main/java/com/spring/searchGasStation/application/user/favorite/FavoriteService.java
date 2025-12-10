package com.spring.searchGasStation.application.user.favorite;

import com.spring.searchGasStation.application.user.UserCheck;
import com.spring.searchGasStation.core.exception.CustomJwtException;
import com.spring.searchGasStation.domain.entity.GasStationFavorite;
import com.spring.searchGasStation.domain.entity.Member;
import com.spring.searchGasStation.domain.repository.GasStationFavoriteRepository;
import com.spring.searchGasStation.domain.repository.MemberRepository;
import com.spring.searchGasStation.dto.user.request.FavoriteRequestDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteService {

    private final UserCheck userCheck;
    private final GasStationFavoriteRepository favoriteRepository;

    public boolean toggleFavorite(FavoriteRequestDto dto) {

        if(userCheck.getUserCheckIsFavorite()) {

            Member member = userCheck.getUserCheck();

            // 이미 찜한 상태라면 -> 삭제
            if (favoriteRepository.existsByMemberAndStationCode(member, dto.getStationCode())) {
                favoriteRepository.deleteByMemberAndStationCode(member, dto.getStationCode());
                return false;
            }
            // 찜하지 않은 상태라면 -> 저장
            else {
                GasStationFavorite favorite = GasStationFavorite.builder()
                        .member(member)
                        .stationCode(dto.getStationCode())
                        .name(dto.getName())
                        .brand(dto.getBrand())
                        .address(dto.getAddress())
                        .build();
                favoriteRepository.save(favorite);
                return true; // 찜 등록됨 (ON)
            }
        }
        throw new CustomJwtException("인증이 필요합니다.");
    }
}
