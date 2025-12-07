package com.spring.searchGasStation.presentation.opinet;


import com.spring.searchGasStation.application.opinet.OpinetService;
import com.spring.searchGasStation.dto.opinet.OpinetDetailResponseDto;
import com.spring.searchGasStation.dto.opinet.OpinetResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OpinetController {

    private final OpinetService opinetService;

    @GetMapping("/api/gas-stations")
    public ResponseEntity<OpinetResponseDto> getGasStations(
            @RequestParam double x, @RequestParam double y,
            @RequestParam(defaultValue = "3000") int radius,
            @RequestParam(defaultValue = "B027") String prodcd,
            @RequestParam(defaultValue = "1") int sort
    ) {
        return ResponseEntity.ok(opinetService.getNearbyStations(x, y, radius, prodcd, sort));
    }

    @GetMapping("/api/station-detail")
    public ResponseEntity<OpinetDetailResponseDto> getStationDetail(
            @RequestParam String uniId // 주유소 고유 ID
    ) {
        // TODO: GasStationService.getStationDetail 호출 및 DTO 반환
        return ResponseEntity.ok(opinetService.getStationDetail(uniId));
    }

    @GetMapping("/api/search-stations")
    public ResponseEntity<OpinetResponseDto> searchStationsByName(@RequestParam String keyword) {

        // 검색 키워드 유효성 검사 (공백/null 방지)
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Service 호출 (전국 검색)
        OpinetResponseDto response = opinetService.searchStationsNationwide(keyword.trim());
        return ResponseEntity.ok(response);
    }
}
