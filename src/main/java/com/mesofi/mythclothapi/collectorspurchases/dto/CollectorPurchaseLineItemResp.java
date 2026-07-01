package com.mesofi.mythclothapi.collectorspurchases.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.collectorspurchases.model.PurchaseType;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorPurchaseLineItemResp(
    long lineItemId,
    long figurineId,
    Integer quantity,
    BigDecimal pricePaid,
    PurchaseType purchaseType) {}
