package com.mesofi.mythclothapi.stats.dto;

import java.util.List;

/**
 * Yearly release statistics grouped by line-up.
 *
 * @param year release year
 * @param lineUp grouped counts by line-up description
 */
public record YearStatisticsResp(int year, List<LineUpCountResp> lineUp) {}
