package com.mesofi.mythclothapi.catalogs.model;

import java.util.List;

import com.mesofi.mythclothapi.anniversaries.model.Anniversary;
import com.mesofi.mythclothapi.common.Descriptive;
import com.mesofi.mythclothapi.distributors.model.Distributor;

/**
 * Provides catalog reference data used when processing figurines.
 *
 * <p>
 * Contains the catalog entities required to resolve relationships and
 * attributes during figurine import and mapping operations.
 * </p>
 *
 * @param distributors
 *            the available distributors
 * @param distributions
 *            the available distributions
 * @param lineUps
 *            the available figurine lineups
 * @param series
 *            the available series
 * @param groups
 *            the available figurine groups
 * @param anniversaries
 *            the available anniversaries
 */
public record CatalogContext(List<Distributor> distributors, List<Descriptive> distributions, List<Descriptive> lineUps,
        List<Descriptive> series, List<Descriptive> groups, List<Anniversary> anniversaries) {
}
