package com.mesofi.mythclothapi.figurineimages.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FigurineImageResp(List<String> officialImageUrls) {}
