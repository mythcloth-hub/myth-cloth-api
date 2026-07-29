package com.mesofi.mythclothapi.figurinestores.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FigurineStoreHistoricalResp(String name, String currency, List<FigurineStoreHistoricalPriceResp> prices) {
}
