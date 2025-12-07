package com.spring.searchGasStation.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "profile_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalFileName; // 사용자가 올린 파일명
    private String storedFilePath;   // 서버에 저장된 경로
    private String accessUrl;        // 프론트에서 접근할 URL

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public ProfileImage(String originalFileName, String storedFilePath, String accessUrl, Member member) {
        this.originalFileName = originalFileName;
        this.storedFilePath = storedFilePath;
        this.accessUrl = accessUrl;
        this.member = member;
    }

    public void updateUrl(String originalFileName, String storedFilePath, String accessUrl) {
        this.originalFileName = originalFileName;
        this.storedFilePath = storedFilePath;
        this.accessUrl = accessUrl;
    }
}