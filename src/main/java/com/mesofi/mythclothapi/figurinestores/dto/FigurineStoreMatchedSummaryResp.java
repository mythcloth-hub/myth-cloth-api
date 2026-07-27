package com.mesofi.mythclothapi.figurinestores.dto;

import java.util.Currency;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FigurineStoreMatchedSummaryResp(Long storeId, String storeName, String storeWebsite, String storeLogo,
        Currency currency, String country, int matchedFigurineCount) {
}
