package com.mesofi.mythclothapi.stats.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record YearStatisticsResp(int year, Map<String, Integer> lineUp) {}
