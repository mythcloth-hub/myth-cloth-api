package com.mesofi.mythclothapi.catalogs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CatalogResp(long id, String description) {}
