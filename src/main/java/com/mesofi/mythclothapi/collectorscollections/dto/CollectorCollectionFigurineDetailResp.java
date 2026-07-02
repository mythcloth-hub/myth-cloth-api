package com.mesofi.mythclothapi.collectorscollections.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.figurines.dto.FigurineDistributorResp;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorCollectionFigurineDetailResp(
    String displayableName,
    List<FigurineDistributorResp> distributors,
    String tamashiiUrl,
    CatalogResp lineUp,
    String lineUpUrl) {}
