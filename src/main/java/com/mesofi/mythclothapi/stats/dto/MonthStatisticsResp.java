package com.mesofi.mythclothapi.stats.dto;

import java.util.List;

public record MonthStatisticsResp(int month, String name, List<LineUpByMonthResp> lineUp) {}
