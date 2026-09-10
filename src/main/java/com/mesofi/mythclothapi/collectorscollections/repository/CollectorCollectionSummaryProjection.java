package com.mesofi.mythclothapi.collectorscollections.repository;

/**
 * Projection containing summary statistics for a collector's collection,
 * distinguishing between total copies and unique figurines.
 */
public interface CollectorCollectionSummaryProjection {

    /**
     * Returns the total number of copies of preordered figurines in the collection.
     *
     * @return the total number of preordered copies
     */
    int getPreorderedQuantity();

    /**
     * Returns the total number of copies of released figurines in the collection.
     *
     * @return the total number of released copies
     */
    int getReleasedQuantity();

    /**
     * Returns the number of unique preordered figurines in the collection.
     *
     * @return the number of unique preordered figurines
     */
    int getPreorderedFigurines();

    /**
     * Returns the number of unique released figurines in the collection.
     *
     * @return the number of unique released figurines
     */
    int getReleasedFigurines();
}
