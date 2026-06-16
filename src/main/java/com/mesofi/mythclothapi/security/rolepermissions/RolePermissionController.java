package com.mesofi.mythclothapi.security.rolepermissions;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mesofi.mythclothapi.security.permissions.dto.PermissionResp;
import com.mesofi.mythclothapi.security.rolepermissions.dto.RolePermissionReq;
import com.mesofi.mythclothapi.security.rolepermissions.dto.SyncPermissionsReq;
import com.mesofi.mythclothapi.security.roles.RoleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/roles/{roleId}/permissions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class RolePermissionController {

  private final RoleService service;
  private final RolePermissionSyncService syncService;

  @PostMapping
  @PreAuthorize("hasAuthority('roles:permissions:assign')")
  public ResponseEntity<Void> addPermissionToRole(
      @PathVariable Long roleId, @Valid @RequestBody RolePermissionReq rolePermissionRequest) {
    service.addPermissionToRole(roleId, rolePermissionRequest.permissionId());
    // Standard REST practice: Return 204 No Content for a successful association
    // with no new resource entity to return.
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  @PreAuthorize("hasAuthority('roles:permissions:read')")
  public List<PermissionResp> retrievePermissionsByRoleId(@PathVariable Long roleId) {
    return service.retrievePermissionsByRoleId(roleId);
  }

  @PutMapping
  @PreAuthorize("hasAuthority('roles:permissions:sync')")
  public ResponseEntity<Void> syncRolePermissions(
      @PathVariable Long roleId, @Valid @RequestBody SyncPermissionsReq request) {

    syncService.syncPermissions(roleId, request);

    // 204 No Content is the standard REST response for successful updates
    // when no body content needs to accompany the confirmation.
    return ResponseEntity.noContent().build();
  }
}
