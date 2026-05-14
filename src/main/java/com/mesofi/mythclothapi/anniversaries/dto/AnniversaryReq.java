package com.mesofi.mythclothapi.anniversaries.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import com.mesofi.mythclothapi.anniversaries.model.AnniversaryType;

/**
 * API request payload used to create or update an anniversary catalog entry.
 *
 * @param description human-readable anniversary description
 * @param year anniversary number/year value (e.g. 10, 15, 20, 40)
 * @param type anniversary classification
 */
public record AnniversaryReq(
    @NotNull(message = "description must not be blank")
        @Size(max = 100, message = "description must not exceed 100 characters")
        String description,
    @NotNull @Positive Integer year,
    AnniversaryType type) {}
