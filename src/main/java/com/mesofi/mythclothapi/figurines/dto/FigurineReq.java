package com.mesofi.mythclothapi.figurines.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record FigurineReq(
    @NotBlank String name,
    @NotEmpty List<DistributorInfo> distributors,
    String tamashiiUrl,
    @NotNull Long distributionId,
    @NotNull Long lineUpId,
    @NotNull Long seriesId,
    @NotNull Long groupId,
    @NotNull Long anniversaryId,
    boolean metalBody,
    boolean oce,
    boolean revival,
    boolean plainCloth,
    boolean broken,
    boolean golden,
    boolean gold,
    boolean manga,
    boolean surplice,
    boolean set,
    boolean articulable,
    String remarks,
    List<String> officialImages,
    List<String> nonOfficialImages) {}
