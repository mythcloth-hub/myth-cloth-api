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

/**
 * Request DTO representing a collector purchase summary and its associated figurine line items.
 *
 * <p>This object contains the purchase-level information, such as store, order details, currency,
 * and shipping information, together with the list of figurines included in the purchase.
 *
 * <p>A purchase represents historical transaction data and is tracked separately from the
 * collector's current collection inventory.
 *
 * <p>Each line item contains the individual figurine purchase details, including quantity, price
 * paid, and purchase type.
 *
 * @param orderDate date when the purchase was placed
 * @param store store or seller where the purchase was made
 * @param orderNumber external order identifier provided by the seller
 * @param currency currency used for the purchase transaction
 * @param shippingStatus current shipping status of the purchase
 * @param trackingNumber shipment tracking identifier
 * @param carrier shipping provider responsible for delivery
 * @param lineItems figurines included in this purchase
 */
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
