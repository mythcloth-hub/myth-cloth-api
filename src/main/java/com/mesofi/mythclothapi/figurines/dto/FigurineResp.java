package com.mesofi.mythclothapi.figurines.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FigurineResp(
    long id, String name, String displayableName, List<FigurineDistributorResp> distributors) {}
