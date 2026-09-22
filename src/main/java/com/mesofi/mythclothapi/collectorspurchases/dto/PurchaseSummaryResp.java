package com.mesofi.mythclothapi.collectorspurchases.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a summary of collector purchases, including the total amount and
 * currency.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record PurchaseSummaryResp(
        /*
         * The currency of the collector purchase summary.
         */
        String currency,
        /*
         * The total amount of the collector purchase summary. The total cost of all
         * purchases in the summary, calculated from their quantities and unit prices.
         */
        BigDecimal totalAmount) {
}
