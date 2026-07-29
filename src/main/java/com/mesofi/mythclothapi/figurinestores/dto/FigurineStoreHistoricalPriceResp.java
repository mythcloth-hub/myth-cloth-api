package com.mesofi.mythclothapi.figurinestores.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FigurineStoreHistoricalPriceResp(String storeName, String storeLogoUrl, String storeProductUrl,
        BigDecimal price, Instant checkedAt) {
}
