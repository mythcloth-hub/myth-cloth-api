package com.mesofi.mythclothapi.catalogs.model;

/**
 * Enumeration of the supported Saint Seiya series and spin-offs.
 *
 * <p>
 * Each constant represents a standardized series or franchise associated with a
 * figurine, such as the original Saint Seiya storyline, spin-offs, movies, or
 * special projects.
 *
 * <p>
 * These values provide a stable, type-safe representation of figurine series
 * for business logic, data import, and display name generation, and should
 * remain synchronized with the corresponding series catalog data.
 */
public enum SeriesType {

    /**
     * Figurines based on the Soul of Gold anime series.
     */
    SOUL_OF_GOLD,

    /**
     * Figurines based on the live-action Knights of the Zodiac: Saint Seiya – The
     * Beginning film.
     */
    SS_THE_BEGINNING,

    /**
     * Figurines based on the Saintia Shō spin-off manga and anime.
     */
    SAINTIA_SHO,

    /**
     * Figurines based on the Legend of Sanctuary animated film.
     */
    LEGEND_OF_SANCTUARY,

    /**
     * Figurines based on The Lost Canvas manga and anime.
     */
    LOST_CANVAS
}
