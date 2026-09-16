package com.mesofi.mythclothapi.collectorspurchases;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonInclude;

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
        @NotNull @Size(min = 3, max = 150) String seller) {
}
