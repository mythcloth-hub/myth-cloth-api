package com.mesofi.mythclothapi.collectorscollections.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Provides summary statistics for the figurine catalog as it relates to a
 * collector's collection.
 *
 * @param totalFigurines
 *            total number of figurines in the catalog, including released and
 *            upcoming figurines
 * @param totalUpcoming
 *            total number of figurines that are upcoming and currently
 *            available for preorder
 * @param totalReleased
 *            total number of figurines that have been released and are
 *            currently available in the market
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorCollectionCatalogSummaryResp(int totalFigurines, int totalUpcoming, int totalReleased) {
}
