package com.spring.searchGasStation.domain.repository;

import com.spring.searchGasStation.domain.entity.GasStationFavorite;
import com.spring.searchGasStation.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GasStationFavoriteRepository extends JpaRepository<GasStationFavorite, Long> {
    // 해당 회원이 특정 코드를 가진 주유소를 즐겨찾기 했는지 확인
    boolean existsByMemberAndStationCode(Member member, String stationCode);
    // 즐겨찾기 취소
    void deleteByMemberAndStationCode(Member member, String stationCode);
    // 해당 회원이 즐겨찾기한 주유소의 리스트
    List<GasStationFavorite> findByMember(Member member);

}