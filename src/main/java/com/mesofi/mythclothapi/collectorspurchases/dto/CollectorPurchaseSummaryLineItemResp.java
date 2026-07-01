package com.mesofi.mythclothapi.collectorspurchases.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.collectorspurchases.model.ShippingStatus;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorPurchaseSummaryLineItemResp(
    long purchaseId,
    LocalDate orderDate,
    String store,
    String orderNumber,
    CurrencyCode currency,
    BigDecimal totalAmount,
    Integer totalFigurines,
    ShippingStatus shippingStatus,
    String trackingNumber,
    String carrier,
    LocalDate shippedDate,
    LocalDate deliveredDate,
    List<CollectorPurchaseLineItemResp> lineItems) {}
