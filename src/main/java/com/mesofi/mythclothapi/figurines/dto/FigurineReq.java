package com.mesofi.mythclothapi.figurines.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FigurineReq(@NotBlank String name, @NotNull Long lineUpId) {}
