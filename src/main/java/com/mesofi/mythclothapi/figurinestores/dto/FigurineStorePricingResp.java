package com.mesofi.mythclothapi.figurinestores.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

@Deprecated
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FigurineStorePricingResp(BigDecimal realTimePrice) {
}
