package com.mesofi.mythclothapi.distributors.dto;

import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.distributors.model.DistributorName;

import jakarta.validation.constraints.NotNull;

public record DistributorReq(
    @NotNull(message = "name must not be blank") DistributorName name,
    @NotNull(message = "country is required") CountryCode country,
    String website) {}
