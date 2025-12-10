package com.mesofi.mythclothapi.figurines.dto;

import java.util.List;

public record FigurineResp(
    long id, String name, String standardName, List<FigurineDistributorInfoResp> distributors) {}
