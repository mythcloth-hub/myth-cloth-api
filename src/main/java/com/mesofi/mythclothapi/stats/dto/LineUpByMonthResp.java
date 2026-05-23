package com.mesofi.mythclothapi.stats.dto;

import java.util.List;

public record LineUpByMonthResp(String line, List<FigurineByMonthResp> figurines) {}
