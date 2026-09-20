package com.mesofi.mythclothapi.collectorspurchases.model;

/**
 * Represents the physical fulfillment and delivery status of a purchase.
 *
 * <p>
 * This status describes whether the purchased item has been shipped and
 * delivered. It does not indicate whether the purchase itself has been paid,
 * cancelled, or fulfilled by the seller.
 * </p>
 *
 * <p>
 * For preorders, {@link #NOT_SHIPPED} is expected until the item is released
 * and the seller ships the order. A preorder may therefore remain in this
 * status for an extended period without indicating a problem with the purchase.
 * </p>
 */
public enum ShippingStatus {

    /**
     * The purchased item has not yet been shipped.
     *
     * <p>
     * This includes purchases that are awaiting fulfillment, such as preorders that
     * have not yet reached their release date.
     * </p>
     */
    NOT_SHIPPED,

    /**
     * The purchased item has been shipped but has not yet been delivered.
     */
    SHIPPED,

    /**
     * The purchased item has been delivered to the recipient.
     */
    DELIVERED
}
