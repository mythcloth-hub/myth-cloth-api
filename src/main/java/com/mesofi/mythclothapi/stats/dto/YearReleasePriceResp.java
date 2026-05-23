package com.mesofi.mythclothapi.stats.dto;

import java.math.BigDecimal;

/**
 * Yearly summary of figurine release prices.
 *
 * @param year release year
 * @param averageReleasePrice average release price for the year
 * @param highestReleasePrice highest release price for the year
 * @param lowestReleasePrice lowest release price for the year
 * @param highestPriceFigurines figurine reference for the highest release price
 * @param lowestPriceFigurines figurine reference for the lowest release price
 * @param releaseCount number of releases included in the aggregation
 */
public record YearReleasePriceResp(
    int year,
    BigDecimal averageReleasePrice,
    BigDecimal highestReleasePrice,
    BigDecimal lowestReleasePrice,
    FigurinePriceResp highestPriceFigurines,
    FigurinePriceResp lowestPriceFigurines,
    int releaseCount) {}
