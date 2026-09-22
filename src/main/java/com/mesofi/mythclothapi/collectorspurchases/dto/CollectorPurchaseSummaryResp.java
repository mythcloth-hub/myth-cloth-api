package com.mesofi.mythclothapi.collectorspurchases.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents the response for a collector's purchase summary, including the
 * summary of purchases and the list of individual purchases.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorPurchaseSummaryResp(
        /*
         * The summary of the collector purchases, including total purchases, total
         * amount spent, and average purchase amount.
         */
        PurchaseSummaryResp summary,
        /*
         * The list of individual collector purchases.
         */
        List<CollectorPurchaseResp> purchases) {
}
