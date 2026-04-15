package com.mesofi.mythclothapi.figurines.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryResp;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.figurineevents.dto.FigurineEventResp;
import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FigurineResp(
    long id,
    String name,
    String displayableName,
    List<FigurineDistributorResp> distributors,
    String tamashiiUrl,
    ReleaseStatus releaseStatus,
    CatalogResp distribution,
    CatalogResp lineUp,
    CatalogResp series,
    CatalogResp group,
    AnniversaryResp anniversary,
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
    List<String> unofficialImageUrls,
    List<FigurineEventResp> events,
    Instant createdAt,
    Instant updatedAt) {}
