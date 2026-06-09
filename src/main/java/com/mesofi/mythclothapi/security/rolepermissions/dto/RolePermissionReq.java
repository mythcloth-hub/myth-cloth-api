package com.mesofi.mythclothapi.security.rolepermissions.dto;

import jakarta.validation.constraints.NotNull;

public record RolePermissionReq(
    @NotNull(message = "permissionId must not be null") Long permissionId) {}
