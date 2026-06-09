package com.mesofi.mythclothapi.security.roles.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.mesofi.mythclothapi.common.Descriptive;
import com.mesofi.mythclothapi.security.permissions.model.Permission;
import com.mesofi.mythclothapi.security.rolepermissions.model.RolePermission;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "roles")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Role extends Descriptive {

  // RolePermission.role
  @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RolePermission> permissions = new ArrayList<>();

  public boolean addPermission(Permission permission) {
    // Check if association already exists in the list
    boolean alreadyExists =
        this.permissions.stream()
            .anyMatch(rp -> rp.getPermission().getId().equals(permission.getId()));

    if (!alreadyExists) {
      RolePermission rolePermission = new RolePermission();
      rolePermission.setRole(this);
      rolePermission.setPermission(permission);

      this.permissions.add(rolePermission);
      permission.getRoles().add(rolePermission); // Keep both sides in sync
    }
    return alreadyExists;
  }
}
