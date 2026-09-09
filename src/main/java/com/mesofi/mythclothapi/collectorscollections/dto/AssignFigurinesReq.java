package com.mesofi.mythclothapi.collectorscollections.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for assigning figurines to collector collections.
 *
 * <p>
 * This record encapsulates the necessary information to assign one or multiple
 * figurines to one or multiple collector collections. It supports different
 * assignment modes defined by {@link CollectionAssignmentMode}:
 *
 * <ul>
 * <li>{@code AUTO}: Assigns figurines to the collector's default collection.
 * <li>{@code CREATE}: Creates a new collection with the provided details and
 * assigns figurines to it.
 * <li>{@code EXISTING}: Assigns figurines to existing collections specified by
 * their IDs.
 * </ul>
 *
 * <p>
 * Validation constraints ensure that:
 *
 * <ul>
 * <li>{@code figurineIds} must not be empty.
 * <li>{@code collectionMode} must not be null.
 * </ul>
 *
 * @param figurineIds
 *            List of IDs of the figurines to be assigned.
 * @param collectionMode
 *            Mode of assignment for the figurines.
 * @param collectionIds
 *            Optional list of existing collection IDs for assignment when using
 *            {@code EXISTING} mode.
 * @param collection
 *            Optional details for creating a new collection when using
 *            {@code CREATE} mode.
 */
public record AssignFigurinesReq(@NotEmpty List<Long> figurineIds, @NotNull CollectionAssignmentMode collectionMode,
        List<Long> collectionIds, CollectorCollectionReq collection) {
}
