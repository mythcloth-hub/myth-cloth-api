package com.mesofi.mythclothapi.figurineevents.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.figurines.model.Figurine;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FigurineEventResp(
    long id, String description, LocalDate eventDate, Figurine figurine) {}
