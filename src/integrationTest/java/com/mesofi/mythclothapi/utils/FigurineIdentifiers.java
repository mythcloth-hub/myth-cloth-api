package com.mesofi.mythclothapi.utils;

/**
 * Represents the identifiers of a figurine and its related resources.
 *
 * <p>
 * This record encapsulates the unique identifiers for a figurine, including its
 * own ID, as well as the IDs of its distributor, group, series, and lineup.
 *
 * @param id
 *            the unique identifier of the figurine
 * @param distributorId
 *            the unique identifier of the distributor associated with the
 *            figurine
 * @param groupId
 *            the unique identifier of the group associated with the figurine
 * @param seriesId
 *            the unique identifier of the series associated with the figurine
 * @param lineUpId
 *            the unique identifier of the lineup associated with the figurine
 */
public record FigurineIdentifiers(Long id, Long distributorId, Long groupId, Long seriesId, Long lineUpId) {
}
