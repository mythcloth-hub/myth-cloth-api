package com.mesofi.mythclothapi.figurines.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FigurineSummaryResp(long id, String displayableName, CatalogResp lineUp, String officialImageUrl) {
}
