package com.mesofi.mythclothapi.distributors.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DistributorResp(
    long id, String name, String description, String country, String website) {}
