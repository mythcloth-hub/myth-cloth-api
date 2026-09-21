package com.mesofi.mythclothapi.collectorspurchases.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.collectorspurchases.model.ShippingStatus;

/**
 * Represents a request to update the shipping status of a collector purchase.
 *
 * @param shippingStatus
 *            the new shipping status to be set for the purchase; must not be
 *            null
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ShippingStatusReq(
        /*
         * The new shipping status to be set for the purchase. This field is mandatory
         * and must not be null.
         */
        ShippingStatus shippingStatus) {
}
