package com.mesofi.mythclothapi.collectorscollections.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorCollectionFigurineResp(
    long id,
    String name,
    ReleaseStatus releaseStatus,
    String notes,
    String imageUrl,
    @JsonProperty("isCollected") boolean isCollected,
    int ownedQuantity,
    int year) {}
