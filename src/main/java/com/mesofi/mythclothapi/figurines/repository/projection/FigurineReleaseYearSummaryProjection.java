package com.mesofi.mythclothapi.figurines.repository.projection;

/**
 * Projection containing the number of released figurines grouped by release
 * year and lineup.
 */
public interface FigurineReleaseYearSummaryProjection {

    /**
     * Returns the year in which the figurines were released.
     *
     * @return the release year
     */
    Integer getReleaseYear();

    /**
     * Returns the description of the lineup associated with the figurines.
     *
     * @return the lineup description
     */
    String getLineupDescription();

    /**
     * Returns the number of figurines released in the given year and lineup.
     *
     * @return the figurine count
     */
    Long getFigurineCount();
}
