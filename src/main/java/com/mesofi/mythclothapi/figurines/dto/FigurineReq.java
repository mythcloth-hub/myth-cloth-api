package com.mesofi.mythclothapi.figurines.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FigurineReq(
    @NotBlank @Size(max = 100, message = "Name must not exceed 100 characters") String name,
    @NotEmpty(message = "At least one distributor must be provided") @Valid
        List<DistributorReq> distributors,
    String tamashiiUrl,
    @NotNull Long distributionId,
    @NotNull Long lineUpId,
    @NotNull Long seriesId,
    @NotNull Long groupId,
    Long anniversaryId,
    Boolean isMetalBody,
    Boolean isOriginalColorEdition,
    Boolean isRevival,
    Boolean isPlainCloth,
    Boolean isBattleDamaged,
    Boolean isGoldenArmor,
    Boolean isGold24kEdition,
    Boolean isMangaVersion,
    Boolean isMultiPack,
    Boolean isArticulable,
    String notes,
    List<String> officialImageUrls,
    List<String> unofficialImageUrls) {}
