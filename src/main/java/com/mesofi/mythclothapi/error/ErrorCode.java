package com.mesofi.mythclothapi.error;

/**
 * Defines application-specific error codes returned by the API.
 *
 * <p>
 * Error codes provide stable identifiers that clients can use to handle errors
 * programmatically without relying on human-readable error messages, which may
 * change or be localized.
 * </p>
 */
public enum ErrorCode {

    // -------------------------------------------------------------------------
    // Authentication
    // -------------------------------------------------------------------------

    /**
     * Indicates that the provided authentication token is invalid, expired, or
     * cannot be validated by the application.
     */
    INVALID_TOKEN,

    // -------------------------------------------------------------------------
    // Collectors
    // -------------------------------------------------------------------------

    /**
     * Indicates that the requested collector could not be found.
     */
    COLLECTOR_NOT_FOUND,

    /**
     * Indicates that the requested collector collection could not be found.
     */
    COLLECTOR_COLLECTION_NOT_FOUND,

    /**
     * Indicates that an attempt was made to create a collector collection that
     * already exists.
     */
    COLLECTOR_COLLECTION_ALREADY_EXISTS,

    /**
     * Indicates that the requested collector purchase could not be found.
     */
    COLLECTOR_PURCHASE_NOT_FOUND,

    // -------------------------------------------------------------------------
    // Figurines
    // -------------------------------------------------------------------------

    /**
     * Indicates that the requested figurine could not be found.
     */
    FIGURINE_NOT_FOUND,

    /**
     * Indicates that the requested figurine event could not be found.
     */
    FIGURINE_EVENT_NOT_FOUND,

    /**
     * Indicates that the requested image could not be found.
     */
    FIGURINE_IMAGE_NOT_FOUND,

    /**
     * Indicates that an attempt was made to create a figurine image that already
     * exists.
     */
    FIGURINE_IMAGE_ALREADY_EXISTS,

    /**
     * Indicates that the requested anniversary could not be found.
     */
    FIGURINE_ANNIVERSARY_NOT_FOUND,

    // -------------------------------------------------------------------------
    // Catalogs
    // -------------------------------------------------------------------------

    /**
     * Indicates that the requested catalog could not be found.
     */
    CATALOG_NOT_FOUND,

    /**
     * Indicates that the requested catalog repository could not be found.
     */
    CATALOG_REPOSITORY_NOT_FOUND,

    // -------------------------------------------------------------------------
    // Distributors
    // -------------------------------------------------------------------------

    /**
     * Indicates that the requested distributor could not be found.
     */
    DISTRIBUTOR_NOT_FOUND,

    /**
     * Indicates that an attempt was made to create a distributor that already
     * exists.
     */
    DISTRIBUTOR_ALREADY_EXISTS,

    // -------------------------------------------------------------------------
    // Permissions
    // -------------------------------------------------------------------------

    /**
     * Indicates that the requested permission could not be found.
     */
    PERMISSION_NOT_FOUND,

    /**
     * Indicates that an attempt was made to create a permission that already
     * exists.
     */
    PERMISSION_ALREADY_EXISTS,

    // -------------------------------------------------------------------------
    // Roles
    // -------------------------------------------------------------------------

    /**
     * Indicates that the requested role could not be found.
     */
    ROLE_NOT_FOUND,

    /**
     * Indicates that an attempt was made to create a role that already exists.
     */
    ROLE_ALREADY_EXISTS,

    /**
     * Indicates that a permission is already associated with the specified role.
     */
    ROLE_PERMISSION_ALREADY_EXISTS,

    // -------------------------------------------------------------------------
    // Stores
    // -------------------------------------------------------------------------

    /**
     * Indicates that the requested store could not be found.
     */
    STORE_NOT_FOUND,

    // -------------------------------------------------------------------------
    // Other
    // -------------------------------------------------------------------------

    /**
     * Indicates that an unexpected error has occurred.
     */
    UNEXPECTED_ERROR,
}
