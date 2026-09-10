package com.mesofi.mythclothapi.collectorscollections.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response DTO representing a collector's collection and its associated
 * figurines.
 *
 * <p>
 * This record provides the collection metadata, the number of figurines
 * currently collected, the total number of figurines included in the
 * collection, and the IDs of all figurines associated with the collection.
 *
 * <p>
 * The following fields are included:
 * <ul>
 * <li>{@code id}: Unique identifier of the collection.</li>
 * <li>{@code name}: Name of the collection.</li>
 * <li>{@code imageUrl}: Optional URL of the collection's image.</li>
 * <li>{@code description}: Optional description of the collection.</li>
 * <li>{@code isFavorite}: Indicates whether this is the collector's favorite
 * collection.</li>
 * <li>{@code collectedFigurines}: Number of figurines in the collection that
 * the collector currently owns.</li>
 * <li>{@code totalFigurines}: Total number of distinct figurines associated
 * with this collection, regardless of whether they are owned.</li>
 * <li>{@code figurineIds}: IDs of all figurines associated with the collection,
 * including both owned and unowned figurines.</li>
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorCollectionResp(
        /*
         * Unique identifier of the collection.
         */
        long id,

        /*
         * Name of the collection.
         */
        String name,

        /*
         * Optional URL of the collection's image.
         */
        String imageUrl,

        /*
         * Optional description of the collection.
         */
        String description,

        /*
         * Indicates whether this is the collector's favorite collection.
         */
        boolean isFavorite,

        /*
         * Number of figurines in the collection that the collector currently owns. This
         * value does not make distinction between Released and Announced figurines. It
         * simply counts all figurines the collector possesses.
         */
        int collectedFigurines,

        /*
         * Total number of distinct figurines included in the collection, regardless of
         * whether they are currently owned by the collector. This represents the size
         * of the collection. This is useful if you want to know how many figurines
         * belong to the collection.
         */
        int totalFigurines,

        /*
         * IDs of all figurines associated with the collection, including both owned and
         * unowned figurines. This is useful if you want to know which figurines belong
         * to the collection.
         */
        List<Long> figurineIds) {
}
