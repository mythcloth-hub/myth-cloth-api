package com.mesofi.mythclothapi.distributors.model;

import jakarta.validation.constraints.NotNull;

public record DistributorRequest(
    @NotNull(message = "name must not be blank") DistributorName name,
    @NotNull(message = "country is required") CountryCode country,
    String website) {}
