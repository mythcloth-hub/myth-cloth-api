package com.mesofi.mythclothapi.collectors;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorLoginResp(
    Long collectorId,
    String displayName,
    String email,
    String accessToken,
    String tokenType,
    long expiresInSeconds) {}
