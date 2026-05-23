package com.mesofi.mythclothapi.stats.model;

import com.mesofi.mythclothapi.figurines.model.Figurine;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Mutable accumulator model for release-price statistics during yearly aggregation.
 *
 * <p>Stores running totals and tracked figurines for highest and lowest release prices.
 */
@Getter
@Setter
@Builder
public class ReleasePrices {
  /** Running average release price for the current aggregation window. */
  private Double average;

  /** Highest observed release price for the current aggregation window. */
  private Double highest;

  /** Lowest observed release price for the current aggregation window. */
  private Double lowest;

  /** Figurine associated with {@link #highest}. */
  Figurine highestPriceFigurine;

  /** Figurine associated with {@link #lowest}. */
  Figurine lowestPriceFigurine;

  /** Running sum of all observed release prices. */
  private Double total;

  /** Number of releases included in the aggregation. */
  private int count;
}
