package com.mesofi.mythclothapi.integration.fb;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Facebook token debug payload containing token metadata and validation state.
 *
 * @param appId application id associated with the token
 * @param type token type
 * @param application Facebook application name
 * @param dataAccessExpiresAt data-access expiration epoch seconds
 * @param expiresAt token expiration epoch seconds
 * @param valid token validity flag
 * @param scopes granted scopes
 * @param userId Facebook user identifier
 */
public record FbTokenData(
    @JsonProperty("app_id") String appId,
    String type,
    String application,
    @JsonProperty("data_access_expires_at") Long dataAccessExpiresAt,
    @JsonProperty("expires_at") Long expiresAt,
    @JsonProperty("is_valid") boolean valid,
    String[] scopes,
    @JsonProperty("user_id") String userId,
    FbTokenError error) {}
