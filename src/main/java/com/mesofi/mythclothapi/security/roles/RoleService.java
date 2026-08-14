package com.mesofi.mythclothapi.security.roles;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesofi.mythclothapi.security.SecurityMapper;
import com.mesofi.mythclothapi.security.permissions.PermissionRepository;
import com.mesofi.mythclothapi.security.permissions.dto.PermissionResp;
import com.mesofi.mythclothapi.security.permissions.exceptions.PermissionNotFoundException;
import com.mesofi.mythclothapi.security.permissions.model.Permission;
import com.mesofi.mythclothapi.security.rolepermissions.RolePermission;
import com.mesofi.mythclothapi.security.roles.dto.RoleReq;
import com.mesofi.mythclothapi.security.roles.dto.RoleResp;
import com.mesofi.mythclothapi.security.roles.exceptions.RoleAlreadyExistsException;
import com.mesofi.mythclothapi.security.roles.exceptions.RoleNotFoundException;
import com.mesofi.mythclothapi.security.roles.exceptions.RolePermissionAlreadyExistsException;
import com.mesofi.mythclothapi.security.roles.model.Role;
import com.mesofi.mythclothapi.security.service.SecurityDataService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for managing application roles and their permissions.
 *
 * <p>
 * Provides operations for creating, retrieving, and updating roles, as well as
 * managing the permissions assigned to each role.
 * </p>
 *
 * <p>
 * Role and permission associations are managed through {@link RolePermission}
 * entities.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final SecurityMapper mapper;

    /**
     * Creates a new application role.
     *
     * <p>
     * The role name must be unique. If a role with the same name already exists, a
     * {@link RoleAlreadyExistsException} is thrown.
     * </p>
     *
     * @param request
     *            the request containing the role information
     * @return the created role
     * @throws RoleAlreadyExistsException
     *             if a role with the requested name already exists
     */
    @Transactional
    public RoleResp createRole(RoleReq request) {
        log.info("Creating role: {}", request.description());

        Role entity = mapper.toRole(request);

        // Ensure that role names are unique.
        roleRepository.findByName(entity.getName()).ifPresent(existing -> {
            throw new RoleAlreadyExistsException(existing.getName());
        });

        var saved = roleRepository.save(entity);
        return mapper.toRoleResp(saved);
    }

    /**
     * Retrieves an application role by its identifier.
     *
     * @param id
     *            the unique identifier of the role
     * @return the requested role
     * @throws RoleNotFoundException
     *             if no role exists with the specified identifier
     */
    @Transactional(readOnly = true)
    public RoleResp retrieveRole(Long id) {
        return roleRepository.findById(id).map(mapper::toRoleResp).orElseThrow(() -> new RoleNotFoundException(id));
    }

    /**
     * Retrieves all application roles ordered by their identifier.
     *
     * @return a list containing all application roles
     */
    @Transactional(readOnly = true)
    public List<RoleResp> retrieveRoles() {
        return roleRepository.findAll(Sort.by("id")).stream().map(mapper::toRoleResp).toList();
    }

    /**
     * Updates an existing application role.
     *
     * <p>
     * The role's name is replaced with the description provided in the request.
     * </p>
     *
     * @param id
     *            the unique identifier of the role to update
     * @param request
     *            the request containing the updated role information
     * @return the updated role
     * @throws RoleNotFoundException
     *             if no role exists with the specified identifier
     */
    @Transactional
    public RoleResp updateRole(Long id, RoleReq request) {
        log.info("Updating role {} to {}", id, request.description());

        var existing = roleRepository.findById(id).orElseThrow(() -> new RoleNotFoundException(id));

        existing.setName(request.description());

        var saved = roleRepository.save(existing);
        return mapper.toRoleResp(saved);
    }

    /**
     * Assigns a permission to an existing role.
     *
     * <p>
     * The role and permission must both exist, and the permission must not already
     * be assigned to the role.
     * </p>
     * Check if this method can be replaced with the
     * {@link SecurityDataService#initializeSecurityData()}
     *
     * @param roleId
     *            the unique identifier of the role
     * @param permissionId
     *            the unique identifier of the permission to assign
     * @throws RoleNotFoundException
     *             if the specified role does not exist
     * @throws PermissionNotFoundException
     *             if the specified permission does not exist
     * @throws RolePermissionAlreadyExistsException
     *             if the permission is already assigned to the role
     */
    @Transactional
    @Deprecated(forRemoval = true)
    public void addPermissionToRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new RoleNotFoundException(roleId));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new PermissionNotFoundException(permissionId));

        // Check whether the association already exists.
        boolean alreadyExists = role.getPermissions().stream()
                .anyMatch(rp -> rp.getPermission().getId().equals(permission.getId()));

        if (alreadyExists) {
            throw new RolePermissionAlreadyExistsException(role.getId(), permission.getId());
        }

        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(role);
        rolePermission.setPermission(permission);
        role.getPermissions().add(rolePermission);

        roleRepository.save(role);
    }

    /**
     * Retrieves all permissions assigned to a role.
     *
     * @param roleId
     *            the unique identifier of the role
     * @return a list containing the permissions assigned to the role
     * @throws RoleNotFoundException
     *             if no role exists with the specified identifier
     */
    @Transactional(readOnly = true)
    public List<PermissionResp> retrievePermissionsByRoleId(Long roleId) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new RoleNotFoundException(roleId));

        return role.getPermissions().stream().map(RolePermission::getPermission)
                .map(permission -> new PermissionResp(permission.getId(), permission.getName())).toList();
    }
}
