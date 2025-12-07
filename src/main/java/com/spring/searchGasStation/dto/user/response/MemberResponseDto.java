package com.spring.searchGasStation.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponseDto {
    private String email;
    private String nickname;
    private String joinDate;       // 가입일 (yyyy-MM-dd)
    private String profileImageUrl; // ★ 추가된 필드: 프로필 이미지 경로
}