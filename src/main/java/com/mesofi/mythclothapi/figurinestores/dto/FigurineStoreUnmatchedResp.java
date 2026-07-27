package com.mesofi.mythclothapi.figurinestores.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FigurineStoreUnmatchedResp(long id, Long storeId, String storeWebsite, String storeLogo,
        String originalName, String imageUrl, String productUrl) {
}
