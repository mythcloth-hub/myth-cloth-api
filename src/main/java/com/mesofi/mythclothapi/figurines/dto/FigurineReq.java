package com.mesofi.mythclothapi.figurines.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record FigurineReq(
    @NotBlank @Size(max = 100, message = "Name must not exceed 100 characters") String name,
    @Valid List<DistributorReq> distributors,
    @Size(max = 50, message = "Tamashii URL must not exceed 50 characters") String tamashiiUrl,
    Long distributionId,
    @NotNull @Positive Long lineUpId,
    @NotNull @Positive Long seriesId,
    @Positive Long groupId,
    @Positive Long anniversaryId,
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
