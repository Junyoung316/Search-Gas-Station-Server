package com.spring.searchGasStation.dto.opinet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OilPriceDto {
    @JsonProperty("PRODCD")
    private String productCode; // B027 (휘발유), D047 (경유) 등

    @JsonProperty("PRICE")
    private int price; // 해당 유종의 가격

    @JsonProperty("TRADE_DT")
    private String tradeDate; // 가격 업데이트 날짜 (YYYYMMDD)

    @JsonProperty("TRADE_TM")
    private String tradeTime;
}