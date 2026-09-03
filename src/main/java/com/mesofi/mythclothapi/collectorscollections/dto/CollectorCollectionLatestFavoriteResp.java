package com.mesofi.mythclothapi.collectorscollections.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents the latest favorite figurines in a collector's collection.
 *
 * @param id
 *            unique identifier of the figurine
 * @param name
 *            normalized figurine name
 * @param imageUrl
 *            URL of the figurine's image
 * @param ownedQuantity
 *            number of copies owned in the collection
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorCollectionLatestFavoriteResp(long id, String name, String imageUrl, int ownedQuantity) {
}
