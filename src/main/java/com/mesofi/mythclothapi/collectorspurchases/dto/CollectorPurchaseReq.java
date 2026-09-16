package com.mesofi.mythclothapi.collectorspurchases.dto;

import java.time.LocalDate;
import java.util.Currency;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.collectorspurchases.model.PurchaseChannel;
import com.mesofi.mythclothapi.collectorspurchases.model.ShippingStatus;

/**
 * Represents a request to create a collector purchase.
 *
 * @param purchaseDate
 *            the date of the purchase; must not be null and must be a past or
 *            present date
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorPurchaseReq(
        /*
         * The date of the purchase. This field is mandatory and must be a past or
         * present date.
         */
        @NotNull @PastOrPresent LocalDate purchaseDate,
        /*
         * The name of the seller from whom the purchase was made. This field is
         * mandatory and must be between 3 and 150 characters in length.
         */
        @NotNull @Size(min = 3, max = 150) String seller,
        /*
         * The order number of the purchase if provided by the seller. This field is
         * optional and has a maximum length of 50 characters.
         */
        @Size(max = 50) String orderNumber,
        /*
         * The currency in which the purchase was made. This field is mandatory and must
         * be a valid currency code.
         */
        @NotNull Currency currency,
        /*
         * The channel through which the purchase was made. This field is mandatory and
         * must be one of the predefined purchase channels.
         */
        @NotNull PurchaseChannel purchaseChannel,
        /*
         * The shipping status of the purchase. This field is NOT mandatory.
         */
        ShippingStatus shippingStatus,
        /*
         * The tracking number of the purchase if provided by the seller. This field is
         * optional and has a maximum length of 100 characters.
         */
        @Size(max = 100) String trackingNumber,
        /*
         * The carrier responsible for the shipment. This field is optional and has a
         * maximum length of 100 characters.
         */
        @Size(max = 100) String carrier) {
}
