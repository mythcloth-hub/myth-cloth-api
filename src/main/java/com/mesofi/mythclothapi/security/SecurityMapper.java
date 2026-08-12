package com.mesofi.mythclothapi.security;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mesofi.mythclothapi.security.permissions.dto.PermissionReq;
import com.mesofi.mythclothapi.security.permissions.dto.PermissionResp;
import com.mesofi.mythclothapi.security.permissions.model.Permission;
import com.mesofi.mythclothapi.security.roles.dto.RoleReq;
import com.mesofi.mythclothapi.security.roles.dto.RoleResp;
import com.mesofi.mythclothapi.security.roles.model.Role;

/**
 * MapStruct mapper for converting between security entities and their
 * corresponding request and response DTOs.
 *
 * <p>
 * Provides mappings for application roles and permissions while explicitly
 * excluding persistence-managed fields and entity relationships from request
 * mappings.
 * </p>
 */
@Mapper(componentModel = "spring")
public interface SecurityMapper {

    /**
     * Maps a role creation or update request to a {@link Role} entity.
     *
     * <p>
     * The request's {@code description} is mapped to the entity's {@code name}. The
     * entity identifier, permissions, and audit fields are ignored because they are
     * managed by the persistence layer.
     * </p>
     *
     * @param request
     *            the role request to map
     * @return a {@link Role} entity populated from the request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "name", source = "description")
    Role toRole(RoleReq request);

    /**
     * Maps a {@link Role} entity to a role response DTO.
     *
     * <p>
     * The entity's {@code name} is exposed as the response's {@code description}.
     * </p>
     *
     * @param role
     *            the role entity to map
     * @return a {@link RoleResp} containing the role information
     */
    @Mapping(target = "description", source = "name")
    RoleResp toRoleResp(Role role);

    /**
     * Maps a permission creation or update request to a {@link Permission} entity.
     *
     * <p>
     * The request's {@code description} is mapped to the entity's {@code name}. The
     * entity identifier, roles, and audit fields are ignored because they are
     * managed by the persistence layer.
     * </p>
     *
     * @param request
     *            the permission request to map
     * @return a {@link Permission} entity populated from the request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "name", source = "description")
    Permission toPermission(PermissionReq request);

    /**
     * Maps a {@link Permission} entity to a permission response DTO.
     *
     * <p>
     * The entity's {@code name} is exposed as the response's {@code description}.
     * </p>
     *
     * @param permission
     *            the permission entity to map
     * @return a {@link PermissionResp} containing the permission information
     */
    @Mapping(target = "description", source = "name")
    PermissionResp toPermissionResp(Permission permission);
}