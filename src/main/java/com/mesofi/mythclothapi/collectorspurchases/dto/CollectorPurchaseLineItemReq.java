package com.mesofi.mythclothapi.collectorspurchases.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.collectorspurchases.model.PurchaseType;

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
