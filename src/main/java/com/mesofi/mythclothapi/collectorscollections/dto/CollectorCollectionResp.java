package com.mesofi.mythclothapi.collectorscollections.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorCollectionResp(long id, String name, String imageUrl, String description, boolean isFavorite,
        int totalFigurines, List<Long> figurineIds) {
}
