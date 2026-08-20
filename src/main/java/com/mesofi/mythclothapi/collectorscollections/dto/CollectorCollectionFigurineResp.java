package com.mesofi.mythclothapi.collectorscollections.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;

/**
 * Represents a figurine inside a collector collection listing.
 *
 * @param id
 *            figurine identifier
 * @param name
 *            figurine display name used in collection views
 * @param releaseStatus
 *            current release status of the figurine
 * @param notes
 *            optional figurine notes or remarks
 * @param imageUrl
 *            first figurine image URL when available
 * @param isCollected
 *            whether the figurine belongs to the collection
 * @param ownedQuantity
 *            number of copies owned in the collection
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorCollectionFigurineResp(long id, String name, ReleaseStatus releaseStatus, String notes,
        String imageUrl, @JsonProperty("isCollected") boolean isCollected, int ownedQuantity) {
}
