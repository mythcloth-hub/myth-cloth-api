package com.mesofi.mythclothapi.collectorscollections.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorCollectionResp(
    long id, String name, String description, int totalFigurines, List<Long> figurineIds) {}
