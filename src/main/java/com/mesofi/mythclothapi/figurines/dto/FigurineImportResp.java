package com.mesofi.mythclothapi.figurines.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FigurineImportResp(long id, int imported, int skipped, String errorMessage, LocalDateTime completedAt) {
}
