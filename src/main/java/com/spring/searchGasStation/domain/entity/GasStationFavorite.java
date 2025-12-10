package com.spring.searchGasStation.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        name = "gas_station_favorite",
        uniqueConstraints = {
                // 한 회원이 같은 주유소 ID(오피넷 코드)를 중복 저장 방지
                @UniqueConstraint(
                        name = "uk_member_station_code",
                        columnNames = {"member_id", "station_code"}
                )
        }
)
@Builder
public class GasStationFavorite {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 오피넷 API에서 주는 주유소 고유 코드 (예: A0012345)
    @Column(nullable = false, name = "station_code")
    private String stationCode;

//    @Builder // 빌더 패턴 사용 권장
//    public GasStationFavorite(Member member, String stationCode, String name, String brand, String address) {
//        this.member = member;
//        this.stationCode = stationCode;
//        this.name = name;
//        this.brand = brand;
//        this.address = address;
//    }
}