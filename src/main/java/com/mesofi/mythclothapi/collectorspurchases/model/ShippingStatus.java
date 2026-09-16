package com.mesofi.mythclothapi.collectorspurchases.model;

/**
 * Enum representing the shipping status of a purchase.
 */
public enum ShippingStatus {
    /**
     * The purchase is pending and has not yet been shipped.
     */
    PENDING,
    /**
     * The purchase has been shipped but not yet delivered.
     */
    SHIPPED,
    /**
     * The purchase has been delivered to the recipient.
     */
    DELIVERED
}
