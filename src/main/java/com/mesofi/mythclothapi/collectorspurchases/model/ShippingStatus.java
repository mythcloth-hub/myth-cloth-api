package com.mesofi.mythclothapi.collectorspurchases.model;

/**
 * Represents the shipping lifecycle status of a collector purchase.
 *
 * <p>This enum tracks the current fulfillment state of a purchase, from the initial order placement
 * until delivery or pickup completion.
 */
public enum ShippingStatus {

  /** Purchase has been created and the order has been placed but has not been shipped yet. */
  ORDERED,

  /** Purchase has been shipped by the seller or shipping provider. */
  SHIPPED,

  /** Purchase is available for collection by the buyer from a pickup location. */
  READY_TO_PICKUP,

  /** Purchase has been successfully delivered or collected by the buyer. */
  DELIVERED
}
