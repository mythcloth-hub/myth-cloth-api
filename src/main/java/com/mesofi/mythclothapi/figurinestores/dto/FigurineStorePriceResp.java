package com.mesofi.mythclothapi.figurinestores.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FigurineStorePriceResp(BigDecimal realTimePrice, BigDecimal discount, BigDecimal discountedPrice,
        Instant lastUpdated, String currency) {

    public FigurineStorePriceResp(BigDecimal realTimePrice) {
        this(realTimePrice, null, null, null, null);
    }

    public FigurineStorePriceResp(BigDecimal realTimePrice, String currency) {
        this(realTimePrice, null, null, null, currency);
    }
}
