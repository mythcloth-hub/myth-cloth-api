package com.mesofi.mythclothapi.collectorspurchases.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.collectorspurchases.model.PurchaseChannel;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorPurchaseResp(
        /*
         * The unique identifier of the collector purchase.
         */
        Long purchaseId,
        /*
         * The seller of the collector purchase.
         */
        String seller,
        /*
         * The order number of the collector purchase.
         */
        String orderNumber,
        /*
         * The currency of the collector purchase.
         */
        String currency,
        /*
         * The total amount of the collector purchase. The total cost of the figurines
         * in the purchase, calculated from their quantities and unit prices.
         */
        BigDecimal totalAmount,
        /*
         * The channel through which the purchase was made. This field is mandatory and
         * must be one of the predefined purchase channels.
         */
        PurchaseChannel purchaseChannel) {

}
