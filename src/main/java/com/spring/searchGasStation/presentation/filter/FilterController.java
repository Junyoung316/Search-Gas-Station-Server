package com.spring.searchGasStation.presentation.filter;

import com.spring.searchGasStation.application.filter.FilterService;
import com.spring.searchGasStation.dto.FilterDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/filter")
@RequiredArgsConstructor
public class FilterController {

    private final FilterService filterService;

    // 1. 내 필터 설정 가져오기 (로그인 후 지도 뜰 때 호출)
    @GetMapping
    public ResponseEntity<FilterDto> getMyFilter(@AuthenticationPrincipal UserDetails user) {
        if (user == null) return ResponseEntity.ok(null); // 비로그인은 null
        return ResponseEntity.ok(filterService.getFilter(user.getUsername()));
    }

    // 2. 필터 설정 저장하기 (옵션 변경할 때마다 호출)
    @PostMapping
    public ResponseEntity<String> saveMyFilter(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody FilterDto dto) {

        if (user == null) return ResponseEntity.status(401).body("로그인 필요");

        filterService.saveFilter(user.getUsername(), dto);
        return ResponseEntity.ok("저장 완료");
    }

}
