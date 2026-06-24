package com.mesofi.mythclothapi.collectorscollections.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CollectorCollectionReq(
    @NotBlank @Size(max = 200) String name, @Size(max = 200) String description) {}
