package com.mesofi.mythclothapi.security.rolepermissions;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.mesofi.mythclothapi.security.permissions.PermissionRepository;
import com.mesofi.mythclothapi.security.permissions.model.Permission;
import com.mesofi.mythclothapi.security.rolepermissions.dto.SyncPermissionsReq;
import com.mesofi.mythclothapi.security.rolepermissions.model.RolePermission;
import com.mesofi.mythclothapi.security.roles.RoleRepository;
import com.mesofi.mythclothapi.security.roles.exceptions.RoleNotFoundException;
import com.mesofi.mythclothapi.security.roles.model.Role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RolePermissionSyncService {

  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;

  @Transactional
  public void syncPermissions(Long roleId, SyncPermissionsReq request) {
    // 1. Fetch the target Role (including its current permissions)
    Role role =
        roleRepository.findById(roleId).orElseThrow(() -> new RoleNotFoundException(roleId));

    // 2. Fetch all requested Permissions from the DB to ensure they exist
    List<Permission> targetPermissions = permissionRepository.findAllById(request.permissionIds());

    // Quick validation check
    if (targetPermissions.size() != request.permissionIds().size()) {
      throw new IllegalArgumentException("One or more permission IDs provided do not exist.");
    }

    // 3. Determine which permissions need to be REMOVED
    Set<Long> incomingIds =
        targetPermissions.stream().map(Permission::getId).collect(Collectors.toSet());

    // Remove links that are not in the incoming payload
    role.getPermissions()
        .removeIf(existingRp -> !incomingIds.contains(existingRp.getPermission().getId()));

    // 4. Determine which permissions need to be ADDED
    Set<Long> currentlyAssignedIds =
        role.getPermissions().stream()
            .map(rp -> rp.getPermission().getId())
            .collect(Collectors.toSet());

    for (Permission permission : targetPermissions) {
      if (!currentlyAssignedIds.contains(permission.getId())) {
        // Map a new link entity
        RolePermission newLink = new RolePermission();
        newLink.setRole(role);
        newLink.setPermission(permission);

        // Add to the managed list
        role.getPermissions().add(newLink);
      }
    }

    // 5. Save changes
    // Because CascadeType.ALL and orphanRemoval = true are set on Role,
    // saving the role handles all DB changes automatically.
    roleRepository.save(role);
  }
}
