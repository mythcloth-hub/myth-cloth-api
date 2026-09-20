package com.mesofi.mythclothapi.figurines.model;

/**
 * A record that combines a {@link Figurine} with its associated collection ID.
 *
 * <p>
 * This record is used to represent a figurine along with the ID of the
 * collection it belongs to. It is useful for scenarios where both the figurine
 * details and its collection context are needed together.
 * </p>
 *
 * @param figurine
 *            the figurine object containing its details
 * @param collectionFigurineId
 *            the ID of the collection to which the figurine belongs
 */
public record FigurineWithCollectionId(Figurine figurine, Long collectionFigurineId) {
}
