package com.mesofi.mythclothapi.figurines;

import java.util.List;

/**
 * Filter criteria used to search and narrow figurine results.
 *
 * @param figurineIds figurine id's
 * @param name figurine name or partial name to match
 * @param lineUpId lineup identifier to filter by
 * @param seriesId series identifier to filter by
 * @param groupId group identifier to filter by
 * @param anniversaryId anniversary identifier to filter by
 * @param metalBody whether the figurine has a metal body
 * @param oce whether the figurine is an OCE variant
 * @param revival whether the figurine is a revival release
 * @param plainCloth whether the figurine has plain cloth
 * @param broken whether the figurine is a broken version
 * @param golden whether the figurine is a golden version
 * @param gold whether the figurine is gold-colored
 * @param manga whether the figurine is a manga variant
 * @param set whether the figurine belongs to a set
 * @param articulable whether the figurine is articulable
 * @param releaseStatus release status to filter by
 */
public record FigurineFilter(
    List<Long> figurineIds,
    String name,
    Long lineUpId,
    Long seriesId,
    Long groupId,
    Long anniversaryId,
    Boolean metalBody,
    Boolean oce,
    Boolean revival,
    Boolean plainCloth,
    Boolean broken,
    Boolean golden,
    Boolean gold,
    Boolean manga,
    Boolean set,
    Boolean articulable,
    String releaseStatus) {}
