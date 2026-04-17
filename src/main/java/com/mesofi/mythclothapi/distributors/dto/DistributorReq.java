package com.mesofi.mythclothapi.distributors.dto;

import jakarta.validation.constraints.NotNull;

import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.distributors.model.DistributorName;

public record DistributorReq(
    @NotNull(message = "name must not be blank") DistributorName name,
    @NotNull(message = "countryCode is required") CountryCode countryCode,
    String website) {}
