package com.mesofi.mythclothapi.security.rolepermissions.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.mesofi.mythclothapi.common.BaseId;
import com.mesofi.mythclothapi.security.permissions.model.Permission;
import com.mesofi.mythclothapi.security.roles.model.Role;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
// Enforce unique combination at the Database level
@Table(
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_role_permission",
            columnNames = {"role_id", "permission_id"}))
public class RolePermission extends BaseId {

  // Many roles → many permissions

  @ManyToOne(optional = false)
  private Role role;

  @ManyToOne(optional = false)
  private Permission permission;

  // Many roles → many permissions
}
