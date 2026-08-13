package com.mesofi.mythclothapi.figurines.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;

/**
 * Provides a concise representation of a figurine for summary views and
 * lightweight API responses.
 *
 * <p>
 * The response includes the figurine identifier, display name, lineup, and the
 * first available official image URL. Empty or {@code null} properties are
 * excluded from the serialized JSON response.
 *
 * @param id
 *            figurine identifier
 * @param displayableName
 *            formatted name displayed to users
 * @param lineUp
 *            lineup associated with the figurine
 * @param officialImageUrl
 *            URL of the first available official image
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FigurineSummaryResp(long id, String displayableName, CatalogResp lineUp, String officialImageUrl) {
}