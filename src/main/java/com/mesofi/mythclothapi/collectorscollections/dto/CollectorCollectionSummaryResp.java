package com.mesofi.mythclothapi.collectorscollections.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Provides summary information about a collector's collection and the
 * corresponding figurine catalog.
 *
 * @param summary
 *            summary statistics for the figurine catalog
 * @param collection
 *            summary statistics for the collector's collection
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorCollectionSummaryResp(CollectorCollectionCatalogSummaryResp summary,
        CollectorCollectionSummaryStatsResp collection) {
}
