package com.mesofi.mythclothapi.collectorscollections.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response DTO representing a collector's collection with its details and
 * associated figurine IDs.
 *
 * <p>
 * This record encapsulates the following information:
 * <ul>
 * <li>{@code id}: Unique identifier of the collection.</li>
 * <li>{@code name}: Name of the collection.</li>
 * <li>{@code imageUrl}: URL of the collection's image.</li>
 * <li>{@code description}: Description of the collection.</li>
 * <li>{@code isFavorite}: Flag indicating if the collection is marked as a
 * favorite.</li>
 * <li>{@code collectedFigurines}: Count of figurines currently collected in
 * this collection.</li>
 * <li>{@code totalFigurines}: Total number of figurines that can be collected
 * in this collection.</li>
 * <li>{@code figurineIds}: List of IDs representing the figurines associated
 * with this collection.</li>
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorCollectionResp(long id, String name, String imageUrl, String description, boolean isFavorite,
        int collectedFigurines, int totalFigurines, List<Long> figurineIds) {
}
