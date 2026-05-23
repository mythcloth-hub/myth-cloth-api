package com.mesofi.mythclothapi.stats.dto;

import java.util.List;

public record YearStatisticsResp(int year, List<LineUpCountResp> lineUp) {}
