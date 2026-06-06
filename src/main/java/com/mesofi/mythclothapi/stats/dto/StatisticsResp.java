package com.mesofi.mythclothapi.stats.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Aggregated figurine statistics returned by the stats endpoint.
 *
 * @param totalFigurines total figurines matching the filter
 * @param countByLineUp counts grouped by line-up description
 * @param countBySeries counts grouped by series description
 * @param countByGroup counts grouped by group description
 * @param countByAnniversary counts grouped by anniversary description
 * @param totalByReleaseStatus counts grouped by computed release status
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record StatisticsResp(
    int totalFigurines,
    Map<String, Integer> countByLineUp,
    Map<String, Integer> countBySeries,
    Map<String, Integer> countByGroup,
    Map<String, Integer> countByAnniversary,
    Map<String, Integer> totalByReleaseStatus) {}
