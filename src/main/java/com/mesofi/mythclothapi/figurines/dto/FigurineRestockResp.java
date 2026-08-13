package com.mesofi.mythclothapi.figurines.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a previous release of a figurine exposed through the API.
 *
 * <p>
 * This response is used to provide the identifier and release date of a
 * figurine associated with the current figurine as a restock or subsequent
 * release.
 *
 * @param id
 *            identifier of the related figurine
 * @param releaseDate
 *            release date of the related figurine
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FigurineRestockResp(long id, LocalDate releaseDate) {
}