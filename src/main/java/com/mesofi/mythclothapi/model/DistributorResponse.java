package com.mesofi.mythclothapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DistributorResponse(long id, String name, String country, String website) {}
