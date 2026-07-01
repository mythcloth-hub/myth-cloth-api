package com.mesofi.mythclothapi.collectorspurchases.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.collectorspurchases.model.ShippingStatus;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorPurchaseSummaryLineItemReq(
    @PastOrPresent(message = "orderDate cannot be in the future") LocalDate orderDate,
    @Size(max = 100, message = "store must not exceed 100 characters") String store,
    @Size(max = 50, message = "orderNumber must not exceed 50 characters") String orderNumber,
    @NotNull(message = "currency is required") CurrencyCode currency,
    @NotNull(message = "shippingStatus is required") ShippingStatus shippingStatus,
    @Size(max = 50, message = "trackingNumber must not exceed 50 characters") String trackingNumber,
    @Size(max = 50, message = "carrier must not exceed 50 characters") String carrier,
    @NotNull(message = "lineItems is required")
        @Size(min = 1, message = "lineItems must contain at least one item")
        List<@Valid CollectorPurchaseLineItemReq> lineItems) {}
