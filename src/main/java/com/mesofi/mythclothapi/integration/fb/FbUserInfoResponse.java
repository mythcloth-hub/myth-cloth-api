package com.mesofi.mythclothapi.integration.fb;

/**
 * Facebook user profile response payload.
 *
 * @param id Facebook user identifier
 * @param name user display name
 * @param email user email address
 */
public record FbUserInfoResponse(String id, String name, String email) {}
