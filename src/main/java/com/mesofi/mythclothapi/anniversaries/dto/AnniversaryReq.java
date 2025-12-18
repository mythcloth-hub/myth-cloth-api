package com.mesofi.mythclothapi.anniversaries.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AnniversaryReq(
    @NotNull(message = "description must not be blank")
        @Size(max = 100, message = "description must not exceed 100 characters")
        String description,
    @Positive int year) {}
