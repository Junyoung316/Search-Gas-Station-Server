package com.spring.searchGasStation.application.opinet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Service
public class OpinetService {

    @Value("${opinet.api.key}")
    private String apiKey;

    @Value("${opinet.api.aroundAll}")
    private String aroundAll;

    public String getAroundStationList(double x, double y, int radius, String prodcd, int sort) {
        RestTemplate restTemplate = new RestTemplate();

        URI uri = UriComponentsBuilder.fromHttpUrl(aroundAll)
                .queryParam("code", apiKey)    // API 키
                .queryParam("out", "json")     // JSON 포맷 요청
                .queryParam("x", x)            // KATEC X 좌표
                .queryParam("y", y)            // KATEC Y 좌표
                .queryParam("radius", radius)  // 반경 (m)
                .queryParam("prodcd", prodcd)  // 제품 코드
                .queryParam("sort", sort)         // 1: 가격순, 2: 거리순
                .build()
                .toUri();

        log.info("uri : {}", uri);

        // 요청 보내고 결과(JSON String) 받기
        // (한글 깨짐 방지를 위해 String으로 받아 처리하는 것이 간편함)
        return restTemplate.getForObject(uri, String.class);
    }

    public String getStationDetail(String stationId) {
        RestTemplate restTemplate = new RestTemplate();

        // 오피넷 상세 정보 API URL
        String detailUrl = "http://www.opinet.co.kr/api/detailById.do";

        URI uri = UriComponentsBuilder.fromHttpUrl(detailUrl)
                .queryParam("code", apiKey) // 공통 API 키
                .queryParam("out", "json")
                .queryParam("id", stationId) // 주유소 고유 ID (UNI_ID)
                .build()
                .toUri();

        System.out.println("오피넷 상세 요청 URL: " + uri.toString());

        return restTemplate.getForObject(uri, String.class);
    }

}
