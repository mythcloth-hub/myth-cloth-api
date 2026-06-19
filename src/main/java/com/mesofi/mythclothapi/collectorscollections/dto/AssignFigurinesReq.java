package com.mesofi.mythclothapi.collectorscollections.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AssignFigurinesReq(
    @NotEmpty List<Long> figurineIds,
    @NotNull CollectionAssignmentMode collectionMode,
    List<Long> collectionIds,
    CollectorCollectionReq collection) {}
