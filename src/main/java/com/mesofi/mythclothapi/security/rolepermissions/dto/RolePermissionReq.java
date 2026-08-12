package com.mesofi.mythclothapi.security.rolepermissions.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request containing the identifier of a permission to assign to a role.
 *
 * @param permissionId
 *            the unique identifier of the permission
 */
public record RolePermissionReq(@NotNull(message = "permissionId must not be null") Long permissionId) {
}