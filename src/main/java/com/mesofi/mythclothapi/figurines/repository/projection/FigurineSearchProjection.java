package com.mesofi.mythclothapi.figurines.repository.projection;

/**
 * Projection representing the minimal figurine dataset returned by the custom
 * figurine search query.
 *
 * <p>
 * This record is populated through the native
 * {@code FigurineSearchProjectionMapping} SQL result set mapping and is
 * intended for search/listing use cases where loading the full {@code Figurine}
 * entity would be unnecessary.
 * </p>
 *
 * @param id
 *            the figurine identifier
 * @param normalizedName
 *            the normalized figurine name used internally for searching and
 *            matching
 * @param displayName
 *            the display-friendly figurine name shown to clients
 * @param currentReleaseStatus
 *            the current release status of the figurine
 * @param lineupDescription
 *            the description of the figurine's lineup
 * @param groupDescription
 *            the description of the figurine's group or series
 * @param anniversaryDescription
 *            the anniversary label associated with the figurine, when present
 * @param isMetalBody
 *            whether the figurine belongs to the Metal Body variant line
 * @param isOce
 *            whether the figurine is an OCE (Original Color Edition) variant
 * @param isRevival
 *            whether the figurine is a revival release
 * @param isGold
 *            whether the figurine is a gold variant
 * @param imageUrl
 *            the primary image URL selected for the figurine, or {@code null}
 *            when no image is available
 */
public record FigurineSearchProjection(Long id, String normalizedName, String displayName, String currentReleaseStatus,
        String lineupDescription, String groupDescription, String anniversaryDescription, boolean isMetalBody,
        boolean isOce, boolean isRevival, boolean isGold, String imageUrl) {
}
