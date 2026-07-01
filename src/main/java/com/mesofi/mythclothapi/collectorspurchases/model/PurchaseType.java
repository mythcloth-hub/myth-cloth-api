package com.mesofi.mythclothapi.collectorspurchases.model;

/**
 * Represents the type of transaction used to acquire a figurine.
 *
 * <p>This enum describes how a figurine was obtained as part of a collector purchase record. The
 * value is stored as part of purchase history and does not affect the collector's current
 * collection state.
 */
public enum PurchaseType {

  /** Figurine purchased as a new item directly from a retail seller. */
  RETAIL,

  /** Figurine purchased before official release or availability. */
  PREORDER,

  /** Figurine purchased from a previous owner or secondary market. */
  SECOND_HAND,
}
