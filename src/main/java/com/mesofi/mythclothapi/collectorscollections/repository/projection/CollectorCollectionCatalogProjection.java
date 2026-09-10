package com.mesofi.mythclothapi.collectorscollections.repository.projection;

/**
 * Projection containing summary statistics for the figurine catalog.
 */
public interface CollectorCollectionCatalogProjection {

    /**
     *
     * Total number of distinct figurines included in the collection, regardless of
     * whether they are currently owned by the collector. This represents the size
     * of the collection. This is useful if you want to know how many figurines
     * belong to the collection.
     *
     * @return the total number of figurines
     */
    int getTotalFigurines();

    /**
     * Returns the total number of released figurines in the collection.
     *
     * @return the total number of released figurines
     */
    int getTotalReleased();

    /**
     * Returns the total number of announced figurines in the collection.
     *
     * @return the total number of announced figurines
     */
    int getTotalAnnounced();
}
