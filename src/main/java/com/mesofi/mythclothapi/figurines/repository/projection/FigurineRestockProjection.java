package com.mesofi.mythclothapi.figurines.repository.projection;

import java.time.LocalDate;

/**
 * Projection representing the restock history of a figurine, including all
 * previous releases in the chain.
 *
 * <p>
 * This record is populated through a recursive common table expression (CTE)
 * query in the {@code FigurineRepository} to gather all related figurines in
 * the previous-release chain. For each figurine in the chain, only the first
 * distributor record is considered when determining the release date.
 * </p>
 *
 * @param figurineId
 *            the ID of the figurine in the restock history
 * @param releaseDate
 *            the release date of the figurine, based on its first distributor
 *            record
 */
public record FigurineRestockProjection(Long figurineId, LocalDate releaseDate) {
}
