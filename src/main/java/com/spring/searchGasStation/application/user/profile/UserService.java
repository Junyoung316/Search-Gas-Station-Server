package com.spring.searchGasStation.application.user.profile;

import com.spring.searchGasStation.application.user.UserCheck;
import com.spring.searchGasStation.domain.entity.Member;
import com.spring.searchGasStation.domain.entity.ProfileImage;
import com.spring.searchGasStation.domain.repository.MemberRepository;
import com.spring.searchGasStation.domain.repository.ProfileImageRepository;
import com.spring.searchGasStation.dto.user.response.MemberResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${server.port}")
    private String serverPort;

    private final MemberRepository memberRepository;
    private final ProfileImageRepository profileImageRepository;

    private final UserCheck userCheck;

    private final String uploadDir = System.getProperty("user.dir") + "/uploads/";

    private String getCurrentBaseUrl() {
        try {
            // 현재 서버의 IP 주소 가져오기 (예: 192.168.0.x)
            String ip = InetAddress.getLocalHost().getHostAddress();
            return "http://" + ip + ":" + serverPort;
        } catch (UnknownHostException e) {
            // IP를 못 찾으면 기본값 localhost 사용
            return "http://localhost:" + serverPort;
        }
    }

    @Transactional(readOnly = true)
    public boolean checkNicknameDuplicate(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    @Transactional
    public String updateProfileImage(MultipartFile file) throws IOException {
        Member member = userCheck.getUserCheck();

        // 폴더 생성
        File folder = new File(uploadDir);
        if (!folder.exists()) folder.mkdirs();

        // 파일명 생성 (UUID)
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String filePath = uploadDir + fileName;
        String accessUrl = "/images/" + fileName; // 프론트 접근 URL

        // 파일 저장
        file.transferTo(new File(filePath));

        // DB 저장 (기존 이미지가 있으면 업데이트, 없으면 생성)
        ProfileImage profileImage = profileImageRepository.findByMember(member)
                .orElse(ProfileImage.builder().member(member).build());

        profileImage.updateUrl(file.getOriginalFilename(), filePath, accessUrl);
        profileImageRepository.save(profileImage);

        return accessUrl;
    }

    public MemberResponseDto getMyInfo() {

        Member member = userCheck.getUserCheck();
        ProfileImage image = profileImageRepository.findByMember(member).orElse(null);

        String baseUrl = getCurrentBaseUrl();
        String imageUrl = null; // 기본값은 null로 설정

        // ★ [수정] image 객체 자체가 존재하는지 먼저 확인해야 합니다!
        if (image != null && image.getAccessUrl() != null) {
            imageUrl = baseUrl + image.getAccessUrl();
        }

        return MemberResponseDto.builder()
                .email(member.getEmail())
                .nickname(member.getNickname())
                .joinDate(member.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .profileImageUrl(imageUrl)
                .build();
    }

    @Transactional
    public void changeNickname(String newNickname) {
        if(!checkNicknameDuplicate(newNickname)) {
            Member member = userCheck.getUserCheck();
            member.updateNickname(newNickname);
            memberRepository.save(member);
        } else {
            throw new IllegalArgumentException("중복된 닉네임입니다.");
        }
    }
}
