package com.mesofi.mythclothapi.security.service;

import static com.mesofi.mythclothapi.security.roles.model.RoleType.ADMIN;
import static com.mesofi.mythclothapi.security.roles.model.RoleType.COLLECTOR;
import static com.mesofi.mythclothapi.security.roles.model.RoleType.DEMO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesofi.mythclothapi.security.permissions.PermissionRepository;
import com.mesofi.mythclothapi.security.permissions.model.Permission;
import com.mesofi.mythclothapi.security.permissions.model.Permissions;
import com.mesofi.mythclothapi.security.rolepermissions.RolePermission;
import com.mesofi.mythclothapi.security.rolepermissions.RolePermissionRepository;
import com.mesofi.mythclothapi.security.roles.RoleRepository;
import com.mesofi.mythclothapi.security.roles.model.Role;
import com.mesofi.mythclothapi.security.roles.model.RoleType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for initializing the application's security data.
 *
 * <p>
 * This service initializes the roles, permissions, and role-permission
 * relationships required by the application. Existing roles and permissions are
 * reused when available, while missing entries are created.
 * </p>
 *
 * <p>
 * Role-permission relationships are initialized only when no existing
 * relationships are found. This prevents the initialization process from
 * recreating relationships that have already been persisted.
 * </p>
 *
 * <p>
 * The configured roles and permissions are defined by {@link #AVAILABLE_ROLES},
 * {@link #AVAILABLE_PERMISSIONS}, and {@link #ROLE_PERMISSIONS_MAP}.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityDataService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    /**
     * Roles that are available to the application.
     */
    private static final List<RoleType> AVAILABLE_ROLES = List.of(ADMIN, COLLECTOR, DEMO);

    /**
     * Permissions that are available to the application.
     *
     * <p>
     * This list represents the complete set of permissions recognized by the
     * application's security model. Each permission is created in the database if
     * it does not already exist.
     * </p>
     */
    private static final List<String> AVAILABLE_PERMISSIONS = List.of(Permissions.COLLECTIONS_DELETE,
            Permissions.COLLECTIONS_DUPLICATE, Permissions.COLLECTIONS_FIGURINES_ADD,
            Permissions.COLLECTIONS_FIGURINES_DELETE, Permissions.COLLECTIONS_FIGURINES_READ,
            Permissions.COLLECTIONS_READ, Permissions.COLLECTIONS_UPDATE, Permissions.FIGURINES_CREATE,
            Permissions.FIGURINES_DELETE, Permissions.FIGURINES_EVENTS_ADD, Permissions.FIGURINES_EVENTS_DELETE,
            Permissions.FIGURINES_EVENTS_READ, Permissions.FIGURINES_EVENTS_UPDATE, Permissions.FIGURINES_STORES_ASSIGN,
            Permissions.FIGURINES_STORES_IGNORE, Permissions.FIGURINES_STORES_READ, Permissions.FIGURINES_UPDATE,
            Permissions.PURCHASES_CREATE, Permissions.PURCHASES_DELETE, Permissions.PURCHASES_READ,
            Permissions.PURCHASES_SYNC, Permissions.PURCHASES_UPDATE, Permissions.STATS_READ);

    /**
     * Defines the initial set of permissions assigned to each application role.
     *
     * <p>
     * The ADMIN and DEMO roles receive all available permissions, while the
     * COLLECTOR role receives only the permissions required for standard collector
     * functionality.
     * </p>
     */
    private static final Map<RoleType, List<String>> ROLE_PERMISSIONS_MAP = Map.of(
            // Initial, admin permissions
            ADMIN, AVAILABLE_PERMISSIONS,
            // Initial, collector permissions
            COLLECTOR,
            List.of(Permissions.COLLECTIONS_DELETE, Permissions.COLLECTIONS_DUPLICATE,
                    Permissions.COLLECTIONS_FIGURINES_ADD, Permissions.COLLECTIONS_FIGURINES_DELETE,
                    Permissions.COLLECTIONS_FIGURINES_READ, Permissions.COLLECTIONS_READ,
                    Permissions.COLLECTIONS_UPDATE, Permissions.STATS_READ),
            // Initial, demo permissions.
            DEMO,
            List.of(Permissions.COLLECTIONS_DELETE, Permissions.COLLECTIONS_DUPLICATE,
                    Permissions.COLLECTIONS_FIGURINES_ADD, Permissions.COLLECTIONS_FIGURINES_DELETE,
                    Permissions.COLLECTIONS_FIGURINES_READ, Permissions.COLLECTIONS_READ,
                    Permissions.COLLECTIONS_UPDATE, Permissions.FIGURINES_CREATE, Permissions.FIGURINES_DELETE,
                    Permissions.FIGURINES_EVENTS_ADD, Permissions.FIGURINES_EVENTS_DELETE,
                    Permissions.FIGURINES_EVENTS_READ, Permissions.FIGURINES_EVENTS_UPDATE,
                    Permissions.FIGURINES_STORES_ASSIGN, Permissions.FIGURINES_STORES_IGNORE,
                    Permissions.FIGURINES_STORES_READ, Permissions.FIGURINES_UPDATE, Permissions.PURCHASES_CREATE,
                    Permissions.PURCHASES_DELETE, Permissions.PURCHASES_READ, Permissions.PURCHASES_SYNC,
                    Permissions.PURCHASES_UPDATE, Permissions.STATS_READ));

    /**
     * In-memory lookup of roles initialized during the current operation.
     *
     * <p>
     * The map associates each {@link RoleType} with its corresponding persisted
     * {@link Role} entity.
     * </p>
     */
    private static final Map<RoleType, Role> roleMap = new HashMap<>();

    /**
     * In-memory lookup of permissions initialized during the current operation.
     *
     * <p>
     * The map associates each permission name with its corresponding persisted
     * {@link Permission} entity.
     * </p>
     */
    private static final Map<String, Permission> permissionMap = new HashMap<>();

    /**
     * Initializes the application's roles, permissions, and role-permission
     * relationships.
     *
     * <p>
     * If at least one role-permission relationship already exists, the
     * initialization is skipped. This assumes that the presence of existing
     * role-permission data indicates that the security configuration has already
     * been initialized.
     * </p>
     *
     * <p>
     * Existing roles and permissions are reused when found in the database. Missing
     * roles and permissions are created before the configured role-permission
     * relationships are persisted.
     * </p>
     *
     * @implNote This method is transactional to ensure that the initialization of
     *           roles, permissions, and their relationships is performed within a
     *           single transaction.
     */
    @Transactional
    public void initializeSecurityData() {
        log.info("Initializing Security Data...");

        if (rolePermissionRepository.count() != 0) {
            log.info("Role Permission Repository has been initialized, no need to initialize.");
            return;
        }

        // Create the roles
        for (RoleType role : AVAILABLE_ROLES) {
            roleMap.put(role, getOrCreateRole(role));
        }

        log.info("Roles initialized correctly: {}, {}, {}", roleMap.get(ADMIN).getName(),
                roleMap.get(COLLECTOR).getName(), roleMap.get(DEMO).getName());

        // Create the permissions
        for (String permission : AVAILABLE_PERMISSIONS) {
            permissionMap.put(permission, getOrCreatePermission(permission));
        }

        log.info("Permissions initialized correctly: {} permissions retrieved.", permissionMap.size());

        // Create role-permission relationships
        for (Map.Entry<RoleType, List<String>> entry : ROLE_PERMISSIONS_MAP.entrySet()) {
            RoleType roleName = entry.getKey();
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

    /**
     * Retrieves an existing role by its display name or creates a new role when no
     * matching role exists.
     *
     * @param roleName
     *            the role type to retrieve or create
     * @return the existing or newly created role
     */
    private Role getOrCreateRole(RoleType roleName) {
        return roleRepository.findByName(roleName.getDisplayName()).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName(roleName.getDisplayName());

            return roleRepository.save(newRole);
        });
    }

    /**
     * Retrieves an existing permission by name or creates a new permission when no
     * matching permission exists.
     *
     * @param permissionName
     *            the permission name to retrieve or create
     * @return the existing or newly created permission
     */
    private Permission getOrCreatePermission(String permissionName) {
        return permissionRepository.findByName(permissionName).orElseGet(() -> {
            Permission newPermission = new Permission();
            newPermission.setName(permissionName);

            return permissionRepository.save(newPermission);
        });
    }
}
