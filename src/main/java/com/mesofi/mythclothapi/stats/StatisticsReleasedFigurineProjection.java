package com.mesofi.mythclothapi.stats;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Projection interface for released figurine statistics.
 */
public interface StatisticsReleasedFigurineProjection {

    /**
     * Gets the ID of the released figurine.
     *
     * @return the ID of the released figurine
     */
    Long getId();

    /**
     * Gets the name of the released figurine.
     *
     * @return the name of the released figurine
     */
    String getName();

    /**
     * Gets the price of the released figurine.
     *
     * @return the price of the released figurine
     */
    BigDecimal getPrice();

    /**
     * Gets the release date of the released figurine.
     *
     * @return the release date of the released figurine
     */
    LocalDate getReleaseDate();
}
