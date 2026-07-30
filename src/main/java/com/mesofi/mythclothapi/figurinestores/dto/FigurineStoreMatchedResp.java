package com.mesofi.mythclothapi.figurinestores.dto;

import java.util.Currency;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.messaging.pricing.model.ListingStatus;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FigurineStoreMatchedResp(long id, long figurineId, String figurineDisplayableName, String figurineLineUp,
        String figurineOfficialImageUrl, String figurineTamashiiUrl, long storeId, Currency storeCurrency,
        String storeOriginalName, String storeProductImageUrl, String storeProductUrl, ListingStatus storeStatus,
        boolean storePreorder, List<FigurineStorePriceResp> storePrices) {
}
