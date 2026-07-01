package com.mesofi.mythclothapi.collectorspurchases.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.collectorspurchases.model.ShippingStatus;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;

/**
 * Response DTO representing a collector purchase summary with its associated figurine line items.
 *
 * <p>This object contains the persisted purchase information including order details, totals,
 * shipping information, and the list of figurines included in the purchase.
 *
 * <p>The response represents historical purchase data and is independent from the collector's
 * current collection inventory.
 *
 * <p>Calculated fields such as total amount and total figurines are derived from the associated
 * line items.
 *
 * @param purchaseId unique identifier of the purchase
 * @param orderDate date when the purchase was placed
 * @param store store or seller where the purchase was made
 * @param orderNumber seller-provided order identifier
 * @param currency currency used for the transaction
 * @param totalAmount total purchase amount calculated from all line items
 * @param totalFigurines total number of figurines included in the purchase
 * @param shippingStatus current shipping status
 * @param trackingNumber shipment tracking identifier
 * @param carrier shipping provider responsible for delivery
 * @param shippedDate date when the purchase was shipped
 * @param deliveredDate date when the purchase was delivered
 * @param lineItems figurines included in this purchase
 */
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
