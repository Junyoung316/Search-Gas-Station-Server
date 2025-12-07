package com.spring.searchGasStation.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 회원의 필터인지 연결 (1:1 관계)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 저장할 옵션들 (프로젝트 상황에 맞게 수정하세요)
    private int radius;        // 반경 (예: 3000, 5000)
    private String fuelType;   // 연료 (예: B027-휘발유, D047-경유)
    private int sortType;   // 정렬 (예: 1-가격순, 2-거리순)

    public void updateFilter(int radius, String fuelType, int sortType) {
        this.radius = radius;
        this.fuelType = fuelType;
        this.sortType = sortType;
    }
}
