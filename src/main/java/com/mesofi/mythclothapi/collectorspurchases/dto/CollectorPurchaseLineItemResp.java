package com.mesofi.mythclothapi.collectorspurchases.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.collectorspurchases.model.PurchaseType;

/**
 * Response DTO representing a figurine line item within a collector purchase.
 *
 * <p>This object contains the persisted information of a purchased figurine, including the purchase
 * line item identifier, figurine reference, quantity, price, and purchase type.
 *
 * <p>This DTO represents historical purchase data and does not reflect the collector's current
 * collection quantities.
 *
 * @param lineItemId unique identifier of the purchase line item
 * @param figurineId identifier of the purchased figurine
 * @param quantity quantity of figurines purchased
 * @param pricePaid price paid per figurine unit
 * @param purchaseType type of purchase transaction
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorPurchaseLineItemResp(
    long lineItemId,
    long figurineId,
    Integer quantity,
    BigDecimal pricePaid,
    PurchaseType purchaseType) {}
