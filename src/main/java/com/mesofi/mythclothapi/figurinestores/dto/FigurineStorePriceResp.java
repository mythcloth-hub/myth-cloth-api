package com.mesofi.mythclothapi.figurinestores.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record FigurineStorePriceResp(BigDecimal realTimePrice, BigDecimal discount, BigDecimal discountedPrice,
        Instant lastUpdated) {
}
