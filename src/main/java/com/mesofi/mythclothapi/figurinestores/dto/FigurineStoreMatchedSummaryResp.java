package com.mesofi.mythclothapi.figurinestores.dto;

import java.util.Currency;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FigurineStoreMatchedSummaryResp(Long storeId, String storeWebsite, Currency currency,
        int matchedFigurineCount) {
}
