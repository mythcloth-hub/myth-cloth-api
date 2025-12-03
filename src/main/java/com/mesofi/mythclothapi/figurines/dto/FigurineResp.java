package com.mesofi.mythclothapi.figurines.dto;

import java.util.List;

import com.mesofi.mythclothapi.catalogs.dto.CatalogReq;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;

public record FigurineResp(
    Long id, String standardName, CatalogReq lineUp, List<DistributorResp> distributors) {}
