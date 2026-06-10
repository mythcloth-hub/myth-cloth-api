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
import com.mesofi.mythclothapi.security.rolepermissions.model.RolePermission;
import com.mesofi.mythclothapi.security.roles.dto.RoleReq;
import com.mesofi.mythclothapi.security.roles.dto.RoleResp;
import com.mesofi.mythclothapi.security.roles.exceptions.RoleAlreadyAssociatedToPermissionException;
import com.mesofi.mythclothapi.security.roles.exceptions.RoleAlreadyExistsException;
import com.mesofi.mythclothapi.security.roles.exceptions.RoleNotFoundException;
import com.mesofi.mythclothapi.security.roles.model.Role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;
  private final SecurityMapper mapper;

  @Transactional
  public RoleResp createRole(RoleReq request) {
    log.info("Creating role: {}", request.description());

    Role entity = mapper.toRole(request);
    // make sure the roles contain different descriptions.
    roleRepository
        .findByDescription(entity.getDescription())
        .ifPresent(
            existing -> {
              throw new RoleAlreadyExistsException(entity.getDescription());
            });

    var saved = roleRepository.save(entity);
    return mapper.toRoleResp(saved);
  }

  @Transactional(readOnly = true)
  public RoleResp retrieveRole(Long id) {
    return roleRepository
        .findById(id)
        .map(mapper::toRoleResp)
        .orElseThrow(() -> new RoleNotFoundException(id));
  }

  @Transactional(readOnly = true)
  public List<RoleResp> retrieveRoles() {
    return roleRepository.findAll(Sort.by("id")).stream().map(mapper::toRoleResp).toList();
  }

  @Transactional
  public RoleResp updateRole(Long id, RoleReq request) {
    log.info("Updating role {} to {}", id, request.description());
    var existing = roleRepository.findById(id).orElseThrow(() -> new RoleNotFoundException(id));

    existing.setDescription(request.description());

    var saved = roleRepository.save(existing);
    return mapper.toRoleResp(saved);
  }

  @Transactional
  public void addPermissionToRole(Long roleId, Long permissionId) {
    Role role =
        roleRepository.findById(roleId).orElseThrow(() -> new RoleNotFoundException(roleId));
    Permission permission =
        permissionRepository
            .findById(permissionId)
            .orElseThrow(() -> new PermissionNotFoundException(permissionId));

    // Check if association already exists in the list
    boolean alreadyExists =
        role.getPermissions().stream()
            .anyMatch(rp -> rp.getPermission().getId().equals(permission.getId()));

    if (alreadyExists) {
      throw new RoleAlreadyAssociatedToPermissionException(role.getId(), permission.getId());
    }

    RolePermission rolePermission = new RolePermission();
    rolePermission.setRole(role);
    rolePermission.setPermission(permission);
    role.getPermissions().add(rolePermission);

    roleRepository.save(role);
  }

  @Transactional(readOnly = true)
  public List<PermissionResp> retrievePermissionsByRoleId(Long roleId) {
    Role role =
        roleRepository.findById(roleId).orElseThrow(() -> new RoleNotFoundException(roleId));

    return role.getPermissions().stream()
        .map(RolePermission::getPermission)
        .map(permission -> new PermissionResp(permission.getId(), permission.getDescription()))
        .toList();
  }
}
