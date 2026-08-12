package com.mesofi.mythclothapi.security.rolepermissions.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

/**
 * Request containing the desired set of permissions assigned to a role.
 *
 * <p>
 * The synchronization operation uses the supplied permission identifiers to
 * determine which permissions should be added to or removed from the role.
 * </p>
 *
 * @param permissionIds
 *            the identifiers of the permissions that should be assigned to the
 *            role
 */
public record SyncPermissionsReq(@NotNull(message = "Permission IDs list cannot be null") List<Long> permissionIds) {
}