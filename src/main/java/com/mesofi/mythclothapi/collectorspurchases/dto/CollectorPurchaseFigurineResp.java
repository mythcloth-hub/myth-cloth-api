package com.mesofi.mythclothapi.collectorspurchases.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.collectorspurchases.model.PurchaseType;

/**
 * Response DTO for purchasing a collector figurine. This class represents the
 * data returned after a successful purchase of a collector figurine, including
 * the collection figurine ID, quantity, price paid, and purchase type.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorPurchaseFigurineResp(
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
