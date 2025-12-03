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

    /**
     * 오피넷 API를 호출하여 주변 주유소 정보 조회
     * @param x KATEC X 좌표
     * @param y KATEC Y 좌표
     * @param radius 반경(미터)
     * @param prodcd 제품 코드 (B027: 휘발유, D047: 경유 등)
     * @param sort 정렬 기준 (1: 가격순, 2: 거리순)
     * @return JSON 형식의 주유소 정보
     */
    public String getAroundStationList(double x, double y, int radius, String prodcd, int sort) {
        RestTemplate restTemplate = new RestTemplate();

        URI uri = UriComponentsBuilder.fromHttpUrl(aroundAll)
                .queryParam("code", apiKey)
                .queryParam("out", "json")
                .queryParam("x", x)
                .queryParam("y", y)
                .queryParam("radius", radius)
                .queryParam("prodcd", prodcd)
                .queryParam("sort", sort)
                .build()
                .toUri();

        log.info("Opinet API request URI: {}", uri);

        try {
            String response = restTemplate.getForObject(uri, String.class);
            log.debug("Opinet API response received");
            return response;
        } catch (Exception e) {
            log.error("Failed to call Opinet API: ", e);
            throw new RuntimeException("오피넷 API 호출 실패", e);
        }
    }

    /**
     * 주유소 상세 정보 조회
     * @param stationId 주유소 고유 ID (UNI_ID)
     * @return JSON 형식의 상세 정보
     */
    public String getStationDetail(String stationId) {
        RestTemplate restTemplate = new RestTemplate();
        String detailUrl = "http://www.opinet.co.kr/api/detailById.do";

        URI uri = UriComponentsBuilder.fromHttpUrl(detailUrl)
                .queryParam("code", apiKey)
                .queryParam("out", "json")
                .queryParam("id", stationId)
                .build()
                .toUri();

        log.info("Opinet detail API request URI: {}", uri);

        try {
            String response = restTemplate.getForObject(uri, String.class);
            log.debug("Opinet detail API response received for station: {}", stationId);
            return response;
        } catch (Exception e) {
            log.error("Failed to get station detail from Opinet API: ", e);
            throw new RuntimeException("오피넷 상세 정보 API 호출 실패", e);
        }
    }
}
