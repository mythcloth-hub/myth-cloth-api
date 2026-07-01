package com.mesofi.mythclothapi.collectorspurchases.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.collectorspurchases.model.PurchaseType;

/**
 * Request DTO representing a figurine item included in a collector purchase.
 *
 * <p>This object contains the information required to associate a figurine with a purchase,
 * including the quantity acquired, price paid, and purchase type.
 *
 * <p>Each line item represents a purchased figurine independently of the collector's current
 * collection state. This allows purchase history to preserve transaction details such as quantity
 * and price.
 *
 * @param figurineId identifier of the purchased figurine
 * @param quantity number of units purchased
 * @param pricePaid amount paid per figurine unit
 * @param purchaseType type of purchase transaction
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorPurchaseLineItemReq(
    @NotNull(message = "figurineId is required") @Positive(message = "figurineId must be positive")
        Long figurineId,
    @NotNull(message = "quantity is required") @Positive(message = "quantity must be positive")
        Integer quantity,
    @NotNull(message = "pricePaid is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "pricePaid must be greater than 0")
        BigDecimal pricePaid,
    @NotNull(message = "purchaseType is required") PurchaseType purchaseType) {}
