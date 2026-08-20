package com.mesofi.mythclothapi.collectorscollections.dto;

/**
 * Provides summary statistics about a collector's figurine collection,
 * including preordered and released figurines, copy counts, and missing
 * released figurines.
 *
 * @param preorderedCopies
 *            total number of copies of figurines that have been preordered,
 *            including duplicate copies of the same figurine
 * @param ownedCopies
 *            total number of copies of released figurines owned by the
 *            collector, including duplicate copies of the same figurine
 * @param preorderedFigurines
 *            total number of distinct figurines that have been preordered,
 *            excluding duplicate copies
 * @param ownedFigurines
 *            total number of distinct released figurines owned by the
 *            collector, excluding duplicate copies
 * @param missingReleasedFigurines
 *            total number of released figurines in the catalog that the
 *            collector does not currently own
 */
public record CollectorCollectionSummaryStatsResp(int preorderedCopies, int ownedCopies, int preorderedFigurines,
        int ownedFigurines, int missingReleasedFigurines) {
}
