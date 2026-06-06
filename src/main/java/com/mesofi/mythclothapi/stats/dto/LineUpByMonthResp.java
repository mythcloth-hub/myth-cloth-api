package com.mesofi.mythclothapi.stats.dto;

import java.util.List;

/**
 * Monthly release details grouped by line-up.
 *
 * @param line line-up description
 * @param figurines figurines released in the month for the line-up
 */
public record LineUpByMonthResp(String line, List<FigurineByMonthResp> figurines) {}
