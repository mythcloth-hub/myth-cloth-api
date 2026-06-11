package com.mesofi.mythclothapi.security.permissions.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.mesofi.mythclothapi.common.Descriptive;
import com.mesofi.mythclothapi.security.rolepermissions.model.RolePermission;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "permissions")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Permission extends Descriptive {

  // RolePermission.permission
  @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RolePermission> roles = new ArrayList<>();
}
