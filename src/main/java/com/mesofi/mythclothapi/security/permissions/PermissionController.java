package com.mesofi.mythclothapi.security.permissions;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mesofi.mythclothapi.security.permissions.dto.PermissionReq;
import com.mesofi.mythclothapi.security.permissions.dto.PermissionResp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for managing application permissions.
 *
 * <p>
 * All endpoints require the authenticated user to have the {@code ADMIN} role.
 * Individual operations additionally require the corresponding
 * permission-management authority.
 * </p>
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PermissionController {

    private final PermissionService service;

    /**
     * Creates a new application permission.
     *
     * <p>
     * The authenticated user must have the {@code permissions:write} authority.
     * </p>
     *
     * @param permissionRequest
     *            the request containing the permission information
     * @return a {@link ResponseEntity} containing the created permission and a
     *         {@code Location} header pointing to the newly created resource
     */
    @PostMapping
    @PreAuthorize("hasAuthority('permissions:write')")
    public ResponseEntity<PermissionResp> createPermission(@Valid @RequestBody PermissionReq permissionRequest) {

        PermissionResp response = service.createPermission(permissionRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    /**
     * Retrieves an application permission by its identifier.
     *
     * <p>
     * The authenticated user must have the {@code permissions:read} authority.
     * </p>
     *
     * @param id
     *            the unique identifier of the permission
     * @return the requested permission
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('permissions:read')")
    public PermissionResp retrievePermission(@PathVariable Long id) {
        return service.retrievePermission(id);
    }

    /**
     * Retrieves all application permissions.
     *
     * <p>
     * The authenticated user must have the {@code permissions:read} authority.
     * </p>
     *
     * @return a list containing all application permissions
     */
    @GetMapping
    @PreAuthorize("hasAuthority('permissions:read')")
    public List<PermissionResp> retrievePermissions() {
        return service.retrievePermissions();
    }

    /**
     * Updates an existing application permission.
     *
     * <p>
     * The authenticated user must have the {@code permissions:update} authority.
     * </p>
     *
     * @param id
     *            the unique identifier of the permission to update
     * @param permissionRequest
     *            the request containing the updated permission information
     * @return a {@link ResponseEntity} containing the updated permission
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('permissions:update')")
    public ResponseEntity<PermissionResp> updatePermission(@PathVariable Long id,
            @Valid @RequestBody PermissionReq permissionRequest) {

        PermissionResp updated = service.updatePermission(id, permissionRequest);
        return ResponseEntity.ok(updated);
    }

    /**
     * Removes an existing application permission.
     *
     * <p>
     * The authenticated user must have the {@code permissions:delete} authority.
     * </p>
     *
     * @param id
     *            the unique identifier of the permission to remove
     * @return a response with no content and an HTTP {@code 204 No Content} status
     *         when the permission is successfully removed
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('permissions:delete')")
    public ResponseEntity<?> removePermission(@PathVariable Long id) {
        service.removePermission(id);
        return ResponseEntity.noContent().build();
    }
}