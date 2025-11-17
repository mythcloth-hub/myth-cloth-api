package com.mesofi.mythclothapi.distributors.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DistributorResponse(
    long id, String name, String description, String country, String website) {}
