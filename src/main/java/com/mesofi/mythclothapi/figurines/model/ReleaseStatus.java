package com.mesofi.mythclothapi.figurines.model;

/**
 * Represents the release status of a figurine.
 *
 * <p>
 * The status indicates the current stage of a figurine's release lifecycle,
 * from an early rumor or prototype through announcement and eventual release.
 * </p>
 */
public enum ReleaseStatus {

    /**
     * The figurine has been officially announced but has not yet been released.
     */
    ANNOUNCED,

    /**
     * The figurine has been officially released.
     */
    RELEASED,

    /**
     * The figurine is rumored but has not been officially announced.
     */
    RUMORED,

    /**
     * The figurine has been presented as a prototype but has not been released.
     */
    PROTOTYPE,

    /**
     * The figurine is known to exist but is not currently scheduled or confirmed
     * for release.
     */
    UNRELEASED
}