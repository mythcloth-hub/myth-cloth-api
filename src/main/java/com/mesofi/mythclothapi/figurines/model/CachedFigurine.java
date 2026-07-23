package com.mesofi.mythclothapi.figurines.model;

/**
 * Lightweight representation of a figurine used for similarity matching.
 * <p>
 * Instances of this record are cached to avoid storing JPA entities in the
 * cache. Only the information required by the matching algorithm is retained:
 * the figurine identifier and its display name.
 *
 * @param id
 *            the unique identifier of the figurine
 * @param displayName
 *            the displayable figurine name used for similarity comparisons
 *            against normalized store listing names
 */
public record CachedFigurine(Long id, String displayName) {
}
