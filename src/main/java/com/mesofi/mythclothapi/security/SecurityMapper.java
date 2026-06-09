package com.mesofi.mythclothapi.security;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mesofi.mythclothapi.common.Descriptive;
import com.mesofi.mythclothapi.security.permissions.dto.PermissionReq;
import com.mesofi.mythclothapi.security.permissions.dto.PermissionResp;
import com.mesofi.mythclothapi.security.permissions.model.Permission;
import com.mesofi.mythclothapi.security.roles.dto.RoleReq;
import com.mesofi.mythclothapi.security.roles.dto.RoleResp;
import com.mesofi.mythclothapi.security.roles.model.Role;

@Mapper(componentModel = "spring")
public interface SecurityMapper {

  // Roles
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "permissions", ignore = true)
  Role toRole(RoleReq request);

  RoleResp toRoleResp(Descriptive descriptiveEntity);

  // Permission
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "roles", ignore = true)
  Permission toPermission(PermissionReq request);

  PermissionResp toPermissionResp(Descriptive descriptiveEntity);
}
