package com.mesofi.mythclothapi.security.rolepermissions;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mesofi.mythclothapi.security.permissions.dto.PermissionResp;
import com.mesofi.mythclothapi.security.rolepermissions.dto.RolePermissionReq;
import com.mesofi.mythclothapi.security.rolepermissions.dto.SyncPermissionsReq;
import com.mesofi.mythclothapi.security.roles.RoleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for managing permissions assigned to application roles.
 *
 * <p>
 * All endpoints require the authenticated user to have the {@code ADMIN} role.
 * Individual operations additionally require the corresponding role-permission
 * authority.
 * </p>
 *
 * <p>
 * Role permissions can be added individually, retrieved for a role, or
 * synchronized with a specified set of permissions.
 * </p>
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/roles/{roleId}/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RolePermissionController {

    private final RoleService service;
    private final RolePermissionSyncService syncService;

    /**
     * Assigns a permission to a role.
     *
     * <p>
     * The authenticated user must have the {@code roles:permissions:assign}
     * authority.
     * </p>
     *
     * @param roleId
     *            the unique identifier of the role
     * @param rolePermissionRequest
     *            the request containing the permission to assign
     * @return a response with no content and an HTTP {@code 204 No Content} status
     *         when the permission is successfully assigned
     */
    @PostMapping
    @PreAuthorize("hasAuthority('roles:permissions:assign')")
    public ResponseEntity<Void> addPermissionToRole(@PathVariable Long roleId,
            @Valid @RequestBody RolePermissionReq rolePermissionRequest) {

        service.addPermissionToRole(roleId, rolePermissionRequest.permissionId());

        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves all permissions assigned to a role.
     *
     * <p>
     * The authenticated user must have the {@code roles:permissions:read}
     * authority.
     * </p>
     *
     * @param roleId
     *            the unique identifier of the role
     * @return a list containing the permissions assigned to the role
     */
    @GetMapping
    @PreAuthorize("hasAuthority('roles:permissions:read')")
    public List<PermissionResp> retrievePermissionsByRoleId(@PathVariable Long roleId) {

        return service.retrievePermissionsByRoleId(roleId);
    }

    /**
     * Synchronizes the permissions assigned to a role.
     *
     * <p>
     * The existing role-permission assignments are synchronized with the
     * permissions provided in the request. The authenticated user must have the
     * {@code roles:permissions:sync} authority.
     * </p>
     *
     * @param roleId
     *            the unique identifier of the role
     * @param request
     *            the request containing the permissions to synchronize
     * @return a response with no content and an HTTP {@code 204 No Content} status
     *         when synchronization completes successfully
     */
    @PutMapping
    @PreAuthorize("hasAuthority('roles:permissions:sync')")
    public ResponseEntity<Void> syncRolePermissions(@PathVariable Long roleId,
            @Valid @RequestBody SyncPermissionsReq request) {

        syncService.syncPermissions(roleId, request);

        return ResponseEntity.noContent().build();
    }
}