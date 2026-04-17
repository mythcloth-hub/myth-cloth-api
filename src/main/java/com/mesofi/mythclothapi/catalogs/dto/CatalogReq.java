package com.mesofi.mythclothapi.catalogs.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CatalogReq(
    @NotNull(message = "description must not be blank")
        @Size(max = 100, message = "description must not exceed 100 characters")
        String description) {}
