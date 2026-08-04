package com.mesofi.mythclothapi.catalogs.model;

/**
 * Enumeration of the supported Saint Seiya figurine groups.
 *
 * <p>
 * Each constant represents a standardized figurine group used throughout the
 * application to classify figurines according to their armor version, character
 * category, or thematic role.
 *
 * <p>
 * These values provide a stable, type-safe representation of figurine groups
 * for business logic, data import, and display name generation, and should
 * remain synchronized with the corresponding group catalog data.
 */
public enum GroupType {

    /**
     * Bronze Saints wearing their initial Bronze Cloths.
     */
    BRONZE_SAINT_V1,

    /**
     * Bronze Saints wearing their New Bronze Cloths.
     */
    BRONZE_SAINT_V2,

    /**
     * Bronze Saints wearing their Final Bronze Cloths.
     */
    BRONZE_SAINT_V3,

    /**
     * Bronze Saints wearing their God Cloths.
     */
    BRONZE_SAINT_V4,

    /**
     * Bronze Saints wearing their Heaven Chapter Cloths.
     */
    BRONZE_SAINT_V5,

    /**
     * Steel Saints.
     */
    STEEL,

    /**
     * Gold Saints.
     */
    GOLD_SAINT,

    /**
     * Asgard God Warriors wearing God Robes.
     */
    GOD_ROBE,

    /**
     * Poseidon's Mariners wearing Scales.
     */
    POSEIDON_SCALE,

    /**
     * Saints wearing Hades Surplices.
     */
    SURPLICE_SAINT,

    /**
     * Hades' Specters.
     */
    SPECTER,

    /**
     * The three Judges of the Underworld.
     */
    JUDGE,

    /**
     * Divine characters, such as Athena, Poseidon, and Hades.
     */
    GOD,

    /**
     * Characters recognized as inheritors of the Gold Cloths.
     */
    INHERITOR,

    /**
     * Accessories, display items, and expansion sets.
     */
    ACCESSORIES
}
