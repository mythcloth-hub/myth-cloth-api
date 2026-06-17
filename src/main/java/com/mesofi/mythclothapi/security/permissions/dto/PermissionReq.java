package com.mesofi.mythclothapi.security.permissions.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * API request payload used to create or update a permission entry.
 *
 * @param description human-readable permission description
 */
public record PermissionReq(
    @NotNull(message = "description must not be blank")
        @Size(max = 200, message = "description must not exceed 200 characters")
        @Pattern(
            regexp = "^[a-z0-9_-]+(:[a-z0-9_-]+)+$",
            message =
                "description must follow the format 'resource:action[:subaction...]' (e.g., 'posts:create' or 'posts:create:comment') using lowercase letters, numbers, hyphens, or underscores")
        String description) {}
