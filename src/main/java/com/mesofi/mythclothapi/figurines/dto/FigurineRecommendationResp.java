package com.mesofi.mythclothapi.figurines.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a recommended figurine in a collector's collection.
 * 
 * @param id
 *            unique identifier of the figurine
 * @param name
 *            normalized figurine name
 * @param imageUrl
 *            URL of the figurine's image
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FigurineRecommendationResp(long id, String name, String imageUrl) {
}
