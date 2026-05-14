package com.mesofi.mythclothapi.anniversaries.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.anniversaries.model.AnniversaryType;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record AnniversaryResp(long id, String description, int year, AnniversaryType type) {}
