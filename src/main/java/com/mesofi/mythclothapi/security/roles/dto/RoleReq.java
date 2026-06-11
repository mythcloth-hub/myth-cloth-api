package com.mesofi.mythclothapi.security.roles.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * API request payload used to create or update a role entry.
 *
 * @param description human-readable role description
 */
public record RoleReq(
    @NotNull(message = "description must not be blank")
        @Size(max = 200, message = "description must not exceed 200 characters")
        String description) {}
