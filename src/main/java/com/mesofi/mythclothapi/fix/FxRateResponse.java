package com.mesofi.mythclothapi.fix;

import java.math.BigDecimal;

/**
 * Response payload for a currency-pair lookup from {@code fxapi.app}.
 *
 * @param base source currency code (for example, {@code CNY})
 * @param target target currency code (for example, {@code JPY})
 * @param rate conversion rate from {@code base} to {@code target}
 * @param timestamp timestamp string returned by the upstream API
 */
public record FxRateResponse(String base, String target, BigDecimal rate, String timestamp) {}
