package com.mesofi.mythclothapi.figurines.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record FigurineReq(
    @NotBlank String name,
    @NotEmpty(message = "At least one distributor must be provided") @Valid
        List<DistributorReq> distributors,
    String tamashiiUrl,
    @NotNull Long distributionId,
    @NotNull Long lineUpId,
    @NotNull Long seriesId,
    @NotNull Long groupId,
    Long anniversaryId,
    Boolean metalBody,
    Boolean oce,
    Boolean revival,
    Boolean plainCloth,
    Boolean broken,
    Boolean golden,
    Boolean gold,
    Boolean manga,
    Boolean surplice,
    Boolean set,
    Boolean articulable,
    String remarks,
    List<String> officialImages,
    List<String> nonOfficialImages) {}
