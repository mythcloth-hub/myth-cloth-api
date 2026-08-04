package com.mesofi.mythclothapi.catalogs.model;

/**
 * Enumeration of the supported Saint Seiya figurine product lineups.
 *
 * <p>
 * Each constant represents a recognized Bandai/Tamashii Nations product line
 * used throughout the application to classify figurines, perform mappings, and
 * exchange lineup information between services.
 *
 * <p>
 * These values provide a stable, type-safe representation of lineup names and
 * should be kept synchronized with the corresponding catalog data.
 */
public enum LineUpType {

    /** Myth Cloth EX lineup. */
    MYTH_CLOTH_EX,

    /** Original Myth Cloth lineup. */
    MYTH_CLOTH,

    /** Myth Cloth Appendix lineup. */
    APPENDIX,

    /** DD Panoramation lineup. */
    DD_PANORAMATION,

    /** Saint Cloth Legend lineup. */
    SAINT_CLOTH_LEGEND,

    /** Saint Cloth Crown lineup. */
    SAINT_CLOTH_CROWN,

    /** Figuarts Zero lineup. */
    FIGUARTS_ZERO,

    /** Standard Figuarts lineup. */
    FIGUARTS,

    /** Tamashii Nations Box lineup. */
    TAMASHII_NATIONS_BOX,

    /** Saint Cloth Action lineup. */
    SAINT_CLOTH_ACTION,

    /** Saint Cloth Rebirth lineup. */
    SAINT_CLOTH_REBIRTH,

    /** Saint Cloth Series lineup. */
    SAINT_CLOTH_SERIES,

    /** Metal Build EX Project lineup. */
    METALBUILD_EX_PROJECT
}
