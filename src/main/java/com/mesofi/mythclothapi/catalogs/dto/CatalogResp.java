package com.mesofi.mythclothapi.catalogs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CatalogResp(long id, String description) {}
