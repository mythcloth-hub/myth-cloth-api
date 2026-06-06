package com.mesofi.mythclothapi.collectors.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Login response payload returned after successful collector authentication.
 *
 * @param collectorId internal collector identifier
 * @param displayName collector display name
 * @param email collector email address
 * @param accessToken API access token issued by this service
 * @param tokenType token type prefix to use in authorization headers
 * @param expiresInSeconds token validity duration in seconds
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorLoginResp(
    Long collectorId,
    String displayName,
    String email,
    String accessToken,
    String tokenType,
    long expiresInSeconds) {}
