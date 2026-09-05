package com.mesofi.mythclothapi.stats.model;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Mutable accumulator model for release-price statistics during yearly
 * aggregation.
 *
 * <p>
 * Stores running totals and tracked figurines for highest and lowest release
 * prices.
 */
@Getter
@Setter
@Builder
public class ReleasePrices {
    /** Running average release price for the current aggregation window. */
    private BigDecimal average;

    /** Highest observed release price for the current aggregation window. */
    private BigDecimal highest;

    /** Lowest observed release price for the current aggregation window. */
    private BigDecimal lowest;

    /**
     * Figurine id with the highest observed release price for the current
     * aggregation window.
     */
    private Long highestPriceFigurineId;

    /**
     * Figurine name with the highest observed release price for the current
     * aggregation window.
     */
    private String highestPriceFigurineName;

    /**
     * Figurine id with the lowest observed release price for the current
     * aggregation window.
     */
    private Long lowestPriceFigurineId;

    /**
     * Figurine name with the lowest observed release price for the current
     * aggregation window.
     */
    private String lowestPriceFigurineName;

    /** Running sum of all observed release prices. */
    private BigDecimal total;

    /** Number of releases included in the aggregation. */
    private int count;
}
