package com.spring.searchGasStation.presentation.opinet;

import com.spring.searchGasStation.application.opinet.OpinetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OpinetController {

    private final OpinetService opinetService;

    @GetMapping("/api/gas-stations")
    public String getGasStations(
            @RequestParam double x,
            @RequestParam double y,
            @RequestParam int radius,
            @RequestParam String prodcd,
            @RequestParam int sort) {
        String gasStationsList = opinetService.getAroundStationList(x, y, radius, prodcd, sort);
        log.info("gasStationsList: {}", gasStationsList);
        return gasStationsList;
    }

    @GetMapping("/api/station-detail")
    public String getStationDetail(@RequestParam String id) {
        // ID (UNI_ID)를 받아 서비스 호출
        return opinetService.getStationDetail(id);
    }

}
