package com.mesofi.mythclothapi.integration.fb;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FbTokenData(
    @JsonProperty("app_id") String appId,
    String type,
    String application,
    @JsonProperty("data_access_expires_at") long dataAccessExpiresAt,
    @JsonProperty("expires_at") long expiresAt,
    @JsonProperty("is_valid") boolean valid,
    String[] scopes,
    @JsonProperty("user_id") String userId) {}
