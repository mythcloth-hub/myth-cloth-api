package com.mesofi.mythclothapi.figurinedistributions;

import java.time.LocalDate;

/**
 * Projection representing the minimal figurine distributor dataset returned by
 * the custom figurine distributor query.
 *
 * <p>
 * This record is populated through the native SQL result set mapping and is
 * intended for use cases where loading the full {@code FigurineDistributor}
 * entity would be unnecessary.
 * </p>
 *
 * @param releaseDate
 *            the release date of the figurine
 * @param releaseDateConfirmed
 *            whether the release date is confirmed
 * @param announcementDate
 *            the announcement date of the figurine
 * @param countryCode
 *            the country code of the distributor
 */
public record FigurineDistributorProjection(LocalDate releaseDate, boolean releaseDateConfirmed,
        LocalDate announcementDate, String countryCode) {
}
