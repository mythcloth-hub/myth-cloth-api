package com.mesofi.mythclothapi.security.rolepermissions.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

/** Payload to sync permissions for a specific role. */
public record SyncPermissionsReq(
    @NotNull(message = "Permission IDs list cannot be null") List<Long> permissionIds) {}
