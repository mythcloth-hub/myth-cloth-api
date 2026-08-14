package com.mesofi.mythclothapi.security.roles;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mesofi.mythclothapi.security.roles.dto.RoleReq;
import com.mesofi.mythclothapi.security.roles.dto.RoleResp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for managing application roles.
 *
 * <p>
 * All endpoints require the authenticated user to have the {@code ADMIN} role.
 * Individual operations additionally require the corresponding role-management
 * permission.
 * </p>
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
// @PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RoleService service;

    /**
     * Creates a new application role.
     *
     * <p>
     * The authenticated user must have the {@code roles:write} authority.
     * </p>
     *
     * @param roleRequest
     *            the request containing the role information
     * @return a {@link ResponseEntity} containing the created role and a
     *         {@code Location} header pointing to the newly created resource
     */
    @PostMapping
    // @PreAuthorize("hasAuthority('roles:write')")
    public ResponseEntity<RoleResp> createRole(@Valid @RequestBody RoleReq roleRequest) {
        RoleResp response = service.createRole(roleRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    /**
     * Retrieves an application role by its identifier.
     *
     * <p>
     * The authenticated user must have the {@code roles:read} authority.
     * </p>
     *
     * @param id
     *            the unique identifier of the role
     * @return the requested role
     */
    @GetMapping("/{id}")
    // @PreAuthorize("hasAuthority('roles:read')")
    public RoleResp retrieveRole(@PathVariable Long id) {
        return service.retrieveRole(id);
    }

    /**
     * Retrieves all application roles.
     *
     * <p>
     * The authenticated user must have the {@code roles:read} authority.
     * </p>
     *
     * @return a list containing all application roles
     */
    @GetMapping
    // @PreAuthorize("hasAuthority('roles:read')")
    public List<RoleResp> retrieveRoles() {
        return service.retrieveRoles();
    }

    /**
     * Updates an existing application role.
     *
     * <p>
     * The authenticated user must have the {@code roles:update} authority.
     * </p>
     *
     * @param id
     *            the unique identifier of the role to update
     * @param roleRequest
     *            the request containing the updated role information
     * @return a {@link ResponseEntity} containing the updated role
     */
    @PutMapping("/{id}")
    // @PreAuthorize("hasAuthority('roles:update')")
    public ResponseEntity<RoleResp> updateRole(@PathVariable Long id, @Valid @RequestBody RoleReq roleRequest) {

        RoleResp updated = service.updateRole(id, roleRequest);
        return ResponseEntity.ok(updated);
    }
}
