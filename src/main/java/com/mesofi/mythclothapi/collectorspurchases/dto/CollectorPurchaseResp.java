package com.mesofi.mythclothapi.collectorspurchases.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.collectorspurchases.model.PurchaseChannel;
import com.mesofi.mythclothapi.collectorspurchases.model.ShippingStatus;

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
        PurchaseChannel purchaseChannel,
        /*
         * The shipping status of the collector purchase.
         */
        ShippingStatus shippingStatus,
        /*
         * The tracking number of the purchase if provided by the seller. This field is
         * optional and has a maximum length of 100 characters.
         */
        String trackingNumber,
        /*
         * The carrier responsible for the shipment. This field is optional and has a
         * maximum length of 100 characters.
         */
        String carrier,
        /*
         * The date when the purchase was shipped.
         */
        LocalDate shippedDate,
        /*
         * The date when the purchase was delivered.
         */
        LocalDate deliveredDate) {

}
