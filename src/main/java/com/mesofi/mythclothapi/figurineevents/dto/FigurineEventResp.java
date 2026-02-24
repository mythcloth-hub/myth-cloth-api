package com.mesofi.mythclothapi.figurineevents.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEventType;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FigurineEventResp(
    long id,
    LocalDate date,
    FigurineEventType type,
    CountryCode region,
    String description,
    FigurineResp figurine) {}
