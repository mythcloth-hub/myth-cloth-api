package com.mesofi.mythclothapi.collectors.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Signup response payload returned after successful collector registration.
 *
 * @param collectorId
 *            internal collector identifier
 * @param fullName
 *            collector full name
 * @param email
 *            collector email address
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorSignupResp(Long collectorId, String fullName, String email) {
}
