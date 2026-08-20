package com.mesofi.mythclothapi.figurines.repository;

/**
 * Projection containing summary statistics for the figurine catalog.
 */
public interface FigurineCatalogSummaryProjection {

    /**
     * Returns the total number of figurines in the catalog.
     *
     * @return the total number of figurines
     */
    int getTotalFigurines();

    /**
     * Returns the total number of released figurines in the catalog.
     *
     * @return the total number of released figurines
     */
    int getTotalReleased();

    /**
     * Returns the total number of announced figurines in the catalog.
     *
     * @return the total number of announced figurines
     */
    int getTotalAnnounced();
}
