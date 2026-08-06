package com.mesofi.mythclothapi.figurines.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FigurineRestockResp(long id, LocalDate releaseDate) {
}
