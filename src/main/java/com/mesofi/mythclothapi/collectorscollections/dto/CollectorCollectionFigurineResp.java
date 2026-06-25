package com.mesofi.mythclothapi.collectorscollections.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorCollectionFigurineResp(
    long id,
    String name,
    String displayableName,
    ReleaseStatus releaseStatus,
    String notes,
    List<String> officialImageUrls,
    @JsonProperty("isCollected") boolean isCollected,
    int ownedQuantity) {}
