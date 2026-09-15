package com.mesofi.mythclothapi.collectorscollections.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating or updating a collector collection.
 *
 * <p>
 * This record encapsulates the necessary information to create or update a
 * collector collection. It includes details such as the collection's name,
 * image URL, and description. The {@code subCollection} flag indicates whether
 * the collection is a sub-collection of another collection.
 *
 * <p>
 * Validation constraints ensure that:
 *
 * <ul>
 * <li>{@code name} must not be blank and must not exceed 200 characters.
 * <li>{@code imageUrl} must not exceed 500 characters.
 * <li>{@code description} must not exceed 200 characters.
 * </ul>
 *
 * @param subCollection
 *            Flag indicating if the collection is a sub-collection.
 * @param name
 *            Name of the collection (required, max 200 characters).
 * @param imageUrl
 *            URL of the collection's image (optional, max 500 characters).
 * @param description
 *            Description of the collection (optional, max 200 characters).
 */
public record CollectorCollectionReq(boolean subCollection, @NotBlank @Size(max = 200) String name,
        @Size(max = 500) String imageUrl, @Size(max = 200) String description) {
}
