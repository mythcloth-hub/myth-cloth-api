package com.mesofi.mythclothapi.collectors.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Login request payload containing provider-issued authentication tokens.
 *
 * @param idToken OpenID Connect ID token (used by providers such as Google)
 * @param accessToken OAuth access token (used by providers such as Facebook)
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorLoginReq(String idToken, String accessToken) {}
