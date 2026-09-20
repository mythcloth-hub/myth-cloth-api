package com.mesofi.mythclothapi.collectorspurchases.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.collectorspurchases.model.PurchaseType;

/**
 * Request DTO for purchasing a collector figurine. This class represents the
 * data required to make a purchase of a collector figurine, including the
 * collection figurine ID, quantity, price paid, and purchase type.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorPurchaseFigurineReq(
        /*
         * The ID of the collection figurine associated with the purchase. This field is
         * mandatory and must be a positive value.
         */
        @Positive long collectionFigurineId,
        /*
         * The quantity of the figurine purchased. This field is mandatory and must be a
         * positive value.
         */
        @Positive int quantity,
        /*
         * The price paid for the figurine. This field is mandatory and must be a
         * positive value.
         */
        @Positive BigDecimal pricePaid,
        /*
         * The purchase type of the figurine. This field is mandatory and must be a
         * valid value from the PurchaseType enum.
         */
        @NotNull PurchaseType purchaseType) {
}
