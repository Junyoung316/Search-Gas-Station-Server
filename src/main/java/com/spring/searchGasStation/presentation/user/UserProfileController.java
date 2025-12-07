package com.spring.searchGasStation.presentation.user;

import com.spring.searchGasStation.application.user.profile.UserService;
import com.spring.searchGasStation.core.dto.MainResponse;
import com.spring.searchGasStation.dto.user.request.MemberUpdateDto;
import com.spring.searchGasStation.dto.user.response.MemberResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;

    @GetMapping("/api/member/me")
    public ResponseEntity<MainResponse<MemberResponseDto>> getUserProfile() {
        return ResponseEntity.ok(MainResponse.success(userService.getMyInfo()));
    }

    @PostMapping("/api/member/profile-image")
    public ResponseEntity<MainResponse<String>> userProfileImage(@RequestParam("file") MultipartFile image) throws IOException {
        log.info("{}", image.getName());
        userService.updateProfileImage(image);
        return ResponseEntity.ok(MainResponse.success());
    }

    @PutMapping("/api/member/nickname")
    public ResponseEntity<MainResponse<String>> changeNickname(@RequestBody MemberUpdateDto  memberUpdateDto) {
        userService.changeNickname(memberUpdateDto.getNickname());
        return ResponseEntity.ok(MainResponse.success());
    }
}
