package com.spring.searchGasStation.presentation.user;

import com.spring.searchGasStation.application.user.settings.SettingsService;
import com.spring.searchGasStation.core.dto.MainResponse;
import com.spring.searchGasStation.dto.user.request.UserSettingsRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping("/api/my/settings")
    public ResponseEntity<MainResponse<UserSettingsRequestDto>> mySettings() {
        UserSettingsRequestDto a = settingsService.getMySettings();
        return ResponseEntity.ok(MainResponse.success(a));
    }

    @PostMapping("/api/settings")
    public ResponseEntity<MainResponse<String>> settings(@RequestBody UserSettingsRequestDto requestDto) {
        settingsService.settings(requestDto);
        return ResponseEntity.ok(MainResponse.success());
    }
}

//fuelType: settings.fuelType,
//searchRadius: settings.radius,
//sortType: settings.sortType