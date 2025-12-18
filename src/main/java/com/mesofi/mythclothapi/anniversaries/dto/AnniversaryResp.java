package com.mesofi.mythclothapi.anniversaries.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AnniversaryResp(long id, String description, int year) {}
