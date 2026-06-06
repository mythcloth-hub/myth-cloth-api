package com.mesofi.mythclothapi.stats.dto;

import java.util.List;

/**
 * Monthly release statistics grouped by line-up.
 *
 * @param month month number (1-12)
 * @param name localized month name
 * @param lineUp grouped line-up details for the month
 */
public record MonthStatisticsResp(int month, String name, List<LineUpByMonthResp> lineUp) {}
