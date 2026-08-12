package com.mesofi.mythclothapi.security.rolepermissions;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.mesofi.mythclothapi.security.permissions.PermissionRepository;
import com.mesofi.mythclothapi.security.permissions.exceptions.PermissionNotFoundException;
import com.mesofi.mythclothapi.security.permissions.model.Permission;
import com.mesofi.mythclothapi.security.rolepermissions.dto.SyncPermissionsReq;
import com.mesofi.mythclothapi.security.roles.RoleRepository;
import com.mesofi.mythclothapi.security.roles.exceptions.RoleNotFoundException;
import com.mesofi.mythclothapi.security.roles.model.Role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for synchronizing the permissions assigned to a role.
 *
 * <p>
 * The synchronization process ensures that the role's current permissions
 * exactly match the permissions specified in the request. Permissions not
 * included in the request are removed, while missing permissions are added.
 * </p>
 *
 * <p>
 * The operation is transactional to ensure that all permission changes are
 * persisted as a single unit of work.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RolePermissionSyncService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    /**
     * Synchronizes the permissions assigned to a role.
     *
     * <p>
     * The method performs the following operations:
     * </p>
     * <ol>
     * <li>Retrieves the target role.</li>
     * <li>Retrieves and validates all requested permissions.</li>
     * <li>Removes permissions that are no longer present in the request.</li>
     * <li>Adds permissions that are not currently assigned to the role.</li>
     * <li>Persists the updated role and its permission associations.</li>
     * </ol>
     *
     * <p>
     * If any requested permission does not exist, the operation fails and no
     * changes are persisted.
     * </p>
     *
     * @param roleId
     *            the unique identifier of the role whose permissions should be
     *            synchronized
     * @param request
     *            the request containing the desired permission identifiers
     * @throws RoleNotFoundException
     *             if no role exists with the specified identifier
     * @throws PermissionNotFoundException
     *             if one or more requested permissions do not exist
     */
    @Transactional
    public void syncPermissions(Long roleId, SyncPermissionsReq request) {

        // Fetch the target role, including its current permissions.
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new RoleNotFoundException(roleId));

        // Fetch all requested permissions from the database to ensure they exist.
        List<Permission> targetPermissions = permissionRepository.findAllById(request.permissionIds());

        if (targetPermissions.size() != request.permissionIds().size()) {
            throw new PermissionNotFoundException("One or more permission IDs provided do not exist.");
        }

        // Determine the permissions that should remain assigned to the role.
        Set<Long> incomingIds = targetPermissions.stream().map(Permission::getId).collect(Collectors.toSet());

        // Remove associations that are not present in the incoming request.
        role.getPermissions().removeIf(existingRp -> !incomingIds.contains(existingRp.getPermission().getId()));

        // Determine which permissions remain assigned after removals.
        Set<Long> currentlyAssignedIds = role.getPermissions().stream().map(rp -> rp.getPermission().getId())
                .collect(Collectors.toSet());

        // Add permissions that are not currently assigned.
        for (Permission permission : targetPermissions) {
            if (!currentlyAssignedIds.contains(permission.getId())) {
                RolePermission newLink = new RolePermission();
                newLink.setRole(role);
                newLink.setPermission(permission);
                role.getPermissions().add(newLink);
            }
        }

        // Persist the updated role and its permission associations.
        // Cascade and orphan removal configured on Role handle the
        // corresponding RolePermission persistence operations.
        roleRepository.save(role);
    }
}