package com.spring.searchGasStation.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    // 1. RestTemplate 빈 등록 (HTTP 통신용)
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // RestTemplate을 Builder를 통해 등록해야 Spring의 기본 설정이 적용됩니다.
        return builder.build();
    }

    // 2. ObjectMapper 빈 등록 (JSON 수동 변환용)
    @Bean
    public ObjectMapper objectMapper() {
        // OpinetService에서 JSON 문자열을 DTO로 변환하기 위해 필요합니다.
        return new ObjectMapper();
    }
}