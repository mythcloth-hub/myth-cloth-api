package com.mesofi.mythclothapi.collectorspurchases;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

/**
 * Represents a request to create a collector purchase.
 *
 * @param purchaseDate
 *            the date of the purchase; must not be null and must be a past or
 *            present date
 */
public record CollectorPurchaseReq(
        /*
         * The date of the purchase. This field is mandatory and must be a past or
         * present date.
         */
        @NotNull @PastOrPresent LocalDate purchaseDate) {

}
