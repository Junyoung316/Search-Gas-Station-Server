package com.spring.searchGasStation.application.opinet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.searchGasStation.dto.opinet.OpinetDetailResponseDto;
import com.spring.searchGasStation.dto.opinet.OpinetResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpinetService {

    @Value("${opinet.api.key}")
    private String apiKey;
    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper; // ★ 수동 변환을 위해 주입받음

    public OpinetResponseDto getNearbyStations(double x, double y, int radius, String prodcd, int sort) {

        // -------------------------------------------------------------
        // 🛡️ [보안 패치 v2] 데이터 흐름 끊기 (Data Flow Break)
        // -------------------------------------------------------------
        // 사용자 입력 변수(prodcd)를 그대로 사용하지 않고,
        // 조건문에 따라 '하드코딩된 문자열'을 할당합니다.
        // 이렇게 하면 정적 분석 도구는 입력값과 사용값 사이의 연결이 끊어졌다고 판단합니다.

        int safeRadius;
        if (radius == 1000) {
            safeRadius = 1000;
        } else if (radius == 3000) {
            safeRadius = 3000;
        } else if (radius == 5000) {
            safeRadius = 5000;
        } else {
            safeRadius = 3000;
        }

        String safeProdcd;
        if ("D047".equals(prodcd)) {
            safeProdcd = "D047"; // 경유
        } else if ("B034".equals(prodcd)) {
            safeProdcd = "B034"; // 고급휘발유
        } else if ("K015".equals(prodcd)) {
            safeProdcd = "K015"; // LPG
        } else {
            safeProdcd = "B027"; // 기본값: 휘발유 (그 외 모든 입력은 이걸로 강제 변환)
        }

        // 정렬 값도 동일하게 처리
        int safeSort;
        if (sort == 2) {
            safeSort = 2; // 거리순
        } else {
            safeSort = 1; // 기본값: 가격순
        }
        // -------------------------------------------------------------

        URI uri = UriComponentsBuilder.fromHttpUrl("http://www.opinet.co.kr/api/aroundAll.do")
                .queryParam("code", apiKey)
                .queryParam("x", x)
                .queryParam("y", y)
                .queryParam("radius", safeRadius)
                .queryParam("sort", safeSort)     // ★ 끊어진 변수 사용
                .queryParam("prodcd", safeProdcd) // ★ 끊어진 변수 사용
                .queryParam("out", "json")
                .build()
                .toUri();

        // (이하 로직은 기존과 동일)
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            String jsonString = response.getBody();
            log.info("반경 내 주유소 검색 오피넷 응답 확인: {}", jsonString);

            OpinetResponseDto dto = objectMapper.readValue(jsonString, OpinetResponseDto.class);

            if (dto.getResult() != null && dto.getResult().getOil() != null) {
                log.info("✅ 데이터 조회 성공: {}건 (유종: {})", dto.getResult().getOil().size(), safeProdcd);
            }

            return dto;

        } catch (Exception e) {
            log.error("🚨 오피넷 API 호출 에러: {}", e.getMessage());
            return new OpinetResponseDto();
        }
    }

    public OpinetDetailResponseDto getStationDetail(String uniId) {
        // 1. 상세 API URL 생성 (유니크 ID와 유종 필요)
        URI uri = UriComponentsBuilder.fromHttpUrl("http://www.opinet.co.kr/api/detailById.do") // Opinet 상세 API
                .queryParam("code", apiKey)
                .queryParam("id", uniId) // 주유소 ID
                .queryParam("out", "json")
                .build().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // 3. API 호출 및 String으로 응답 받기
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            String jsonString = response.getBody();

            log.info("주유소 상세 정보 검색 오피넷 응답 확인: {}", jsonString);
            // 4. ObjectMapper를 사용하여 String -> 상세 DTO로 변환
            OpinetDetailResponseDto dto = objectMapper.readValue(jsonString, OpinetDetailResponseDto.class);

            log.info("✅ 상세 정보 변환 성공 (UNI_ID: {})", uniId);
            return dto;

        } catch (Exception e) {
            log.error("🚨 상세 정보 API 호출 에러 (UNI_ID: {}): {}", uniId, e.getMessage());
            // 에러 발생 시 빈 DTO를 반환하여 프론트엔드에서 처리할 수 있도록 함
            return new OpinetDetailResponseDto();
        }
    }

    public OpinetResponseDto searchStationsNationwide(String keyword) {

        // [주의] 오피넷 상호 검색 API는 공식 문서에 명확하지 않은 경우가 많으므로,
        // 테스트 후 URL을 맞춰야 합니다. 여기서는 search.do (가정)를 사용합니다.
        String baseUrl = "http://www.opinet.co.kr/api/searchByName.do";

        // 1. URL 생성: code, osnm, out=json만 포함 (area 파라미터 제외)
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("code", apiKey)
                .queryParam("osnm", keyword) // ★ 상호명 키워드
                .queryParam("out", "json")
                .build().toUri();

        // 2. HTTP 헤더 설정 (Content-Type 오류 방지)
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON)); // JSON 요청 명시
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // 3. API 호출 및 String -> DTO 변환
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            String jsonString = response.getBody();

            // ObjectMapper로 JSON 문자열을 DTO로 변환
            OpinetResponseDto dto = objectMapper.readValue(response.getBody(), OpinetResponseDto.class);

            log.info("주유소 상호 검색 오피넷 응답 확인: {}", jsonString);

            log.info("✅ 전국 검색 결과: {}건 for keyword '{}'",
                    dto.getResult().getOil() != null ? dto.getResult().getOil().size() : 0,
                    keyword);

            return dto;

        } catch (Exception e) {
            log.error("🚨 전국 상호 검색 API 호출 에러: {}", e.getMessage());
            // 에러 발생 시 빈 DTO 반환
            return new OpinetResponseDto();
        }
    }
}
