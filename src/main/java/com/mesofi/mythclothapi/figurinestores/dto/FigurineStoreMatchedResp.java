package com.mesofi.mythclothapi.figurinestores.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FigurineStoreMatchedResp(long id, long figurineId, String figurineDisplayableName, String figurineLineUp,
        String figurineOfficialImageUrl, String figurineTamashiiUrl, long storeId, String storeOriginalName,
        String storeProductImageUrl, String storeProductUrl) {
}
