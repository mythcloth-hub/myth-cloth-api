package com.mesofi.mythclothapi.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesofi.mythclothapi.security.permissions.PermissionRepository;
import com.mesofi.mythclothapi.security.permissions.model.Permission;
import com.mesofi.mythclothapi.security.permissions.model.Permissions;
import com.mesofi.mythclothapi.security.rolepermissions.RolePermissionRepository;
import com.mesofi.mythclothapi.security.rolepermissions.model.RolePermission;
import com.mesofi.mythclothapi.security.roles.RoleRepository;
import com.mesofi.mythclothapi.security.roles.model.Role;
import com.mesofi.mythclothapi.security.roles.model.Roles;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityDataService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    private static final List<String> AVAILABLE_ROLES = List.of(Roles.ADMIN, Roles.COLLECTOR, Roles.DEMO);

    private static final List<String> AVAILABLE_PERMISSIONS = List.of(Permissions.PERMISSIONS_DELETE,
            Permissions.PERMISSIONS_READ, Permissions.PERMISSIONS_UPDATE, Permissions.PERMISSIONS_WRITE,

            Permissions.ROLES_PERMISSIONS_ASSIGN, Permissions.ROLES_PERMISSIONS_READ,
            Permissions.ROLES_PERMISSIONS_SYNC, Permissions.ROLES_READ, Permissions.ROLES_UPDATE,
            Permissions.ROLES_WRITE,

            Permissions.ANNIVERSARIES_DELETE, Permissions.ANNIVERSARIES_READ, Permissions.ANNIVERSARIES_UPDATE,
            Permissions.ANNIVERSARIES_WRITE,

            Permissions.CATALOGS_DELETE, Permissions.CATALOGS_READ, Permissions.CATALOGS_UPDATE,
            Permissions.CATALOGS_WRITE,

            Permissions.COLLECTIONS_FIGURINES_ADD, Permissions.COLLECTIONS_FIGURINES_READ,
            Permissions.COLLECTIONS_FIGURINES_DELETE, Permissions.COLLECTIONS_READ, Permissions.COLLECTIONS_DELETE,
            Permissions.COLLECTIONS_UPDATE,

            Permissions.DISTRIBUTORS_DELETE, Permissions.DISTRIBUTORS_READ, Permissions.DISTRIBUTORS_UPDATE,
            Permissions.DISTRIBUTORS_WRITE,

            Permissions.FIGURINES_DELETE, Permissions.FIGURINES_LOAD, Permissions.FIGURINES_IMAGES_ADD,
            Permissions.FIGURINES_IMAGES_DELETE, Permissions.FIGURINES_IMAGES_READ, Permissions.FIGURINES_EVENTS_ADD,
            Permissions.FIGURINES_EVENTS_DELETE, Permissions.FIGURINES_EVENTS_READ, Permissions.FIGURINES_EVENTS_UPDATE,
            Permissions.FIGURINES_STORES_READ, Permissions.FIGURINES_STORES_ASSIGN, Permissions.FIGURINES_UPDATE,
            Permissions.FIGURINES_WRITE,

            Permissions.PURCHASES_ADD, Permissions.PURCHASES_READ, Permissions.PURCHASES_UPDATE,
            Permissions.PURCHASES_DELETE,

            Permissions.STATS_READ,

            Permissions.STORES_WRITE);

    private static final Map<String, List<String>> ROLE_PERMISSIONS_MAP = Map.of(Roles.ADMIN, AVAILABLE_PERMISSIONS,
            Roles.COLLECTOR,
            List.of(Permissions.COLLECTIONS_FIGURINES_ADD, Permissions.COLLECTIONS_FIGURINES_READ,
                    Permissions.COLLECTIONS_FIGURINES_DELETE, Permissions.COLLECTIONS_READ,
                    Permissions.COLLECTIONS_DELETE, Permissions.COLLECTIONS_UPDATE, Permissions.FIGURINES_IMAGES_READ,
                    Permissions.FIGURINES_EVENTS_READ, Permissions.FIGURINES_STORES_READ, Permissions.PURCHASES_ADD,
                    Permissions.PURCHASES_READ, Permissions.PURCHASES_UPDATE, Permissions.STATS_READ),
            Roles.DEMO, AVAILABLE_PERMISSIONS);

    private static final Map<String, Role> roleMap = new HashMap<>();
    private static final Map<String, Permission> permissionMap = new HashMap<>();

    @Transactional
    public void initialize() {
        log.info("Initializing Security Data...");

        if (rolePermissionRepository.count() != 0) {
            log.info("Role Permission Repository has been initialized, no need to initialize.");
            return;
        }

        // Create the roles
        for (String role : AVAILABLE_ROLES) {
            roleMap.put(role, getOrCreateRole(role));
        }
        log.info("Roles initialized correctly: {}, {}, {}", roleMap.get(Roles.ADMIN).getName(),
                roleMap.get(Roles.COLLECTOR).getName(), roleMap.get(Roles.DEMO).getName());

        // Create the permissions
        for (String permission : AVAILABLE_PERMISSIONS) {
            permissionMap.put(permission, getOrCreatePermissions(permission));
        }
        log.info("Permissions initialized correctly: {} permissions retrieved.", permissionMap.size());

        // Now the relationships
        for (Map.Entry<String, List<String>> entry : ROLE_PERMISSIONS_MAP.entrySet()) {
            String roleName = entry.getKey();
            List<String> permissions = entry.getValue();

            for (String permission : permissions) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRole(roleMap.get(roleName));
                rolePermission.setPermission(permissionMap.get(permission));
                rolePermissionRepository.save(rolePermission);
            }
        }
        log.info("Role-Permission relationships initialized correctly.");
    }

    private Role getOrCreateRole(String roleName) {
        return roleRepository.findByName(roleName).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName(roleName);

            return roleRepository.save(newRole);
        });
    }

    private Permission getOrCreatePermissions(String permissionName) {
        return permissionRepository.findByName(permissionName).orElseGet(() -> {
            Permission newPermission = new Permission();
            newPermission.setName(permissionName);

            return permissionRepository.save(newPermission);
        });
    }
}
