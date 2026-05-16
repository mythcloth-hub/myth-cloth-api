package com.mesofi.mythclothapi.stats.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record StatisticsResp(
    int totalFigurines,
    Map<String, Integer> totalByReleaseStatus,
    Map<String, Integer> countByLineUp,
    Map<String, Integer> countBySeries) {}
