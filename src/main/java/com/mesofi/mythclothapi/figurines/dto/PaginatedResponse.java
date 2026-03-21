package com.mesofi.mythclothapi.figurines.dto;

import java.util.List;

public record PaginatedResponse(
    List<FigurineResp> content, int page, int size, long totalElements, int totalPages) {}
