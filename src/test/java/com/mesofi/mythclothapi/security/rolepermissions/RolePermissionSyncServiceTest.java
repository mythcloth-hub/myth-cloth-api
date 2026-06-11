package com.mesofi.mythclothapi.security.rolepermissions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.mesofi.mythclothapi.security.permissions.PermissionRepository;
import com.mesofi.mythclothapi.security.permissions.exceptions.PermissionNotFoundException;
import com.mesofi.mythclothapi.security.permissions.model.Permission;
import com.mesofi.mythclothapi.security.rolepermissions.dto.SyncPermissionsReq;
import com.mesofi.mythclothapi.security.rolepermissions.model.RolePermission;
import com.mesofi.mythclothapi.security.roles.RoleRepository;
import com.mesofi.mythclothapi.security.roles.exceptions.RoleNotFoundException;
import com.mesofi.mythclothapi.security.roles.model.Role;

@SpringBootTest(classes = RolePermissionSyncService.class)
public class RolePermissionSyncServiceTest {

  @Autowired private RolePermissionSyncService rolePermissionSyncService;

  @MockitoBean private RoleRepository roleRepository;
  @MockitoBean private PermissionRepository permissionRepository;

  @Test
  void syncPermissions_shouldThrowRoleNotFoundException_whenRoleDoesNotExist() {
    // Arrange
    SyncPermissionsReq request = new SyncPermissionsReq(List.of(1L, 2L, 3L));
    when(roleRepository.findById(99L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> rolePermissionSyncService.syncPermissions(99L, request))
        .isInstanceOfSatisfying(
            RoleNotFoundException.class,
            ex -> {
              assertThat(ex.getMessage()).isEqualTo("Role not found");
              assertThat(ex.getId()).isEqualTo(99L);
            });

    verify(roleRepository).findById(99L);
  }

  @Test
  void syncPermissions_shouldThrowPermissionNotFoundException_whenPermissionsDoesNotExist() {
    // Arrange
    Role targetRole = new Role();
    targetRole.setId(1L);
    targetRole.setDescription("Admin");

    List<Permission> targetPermissions = List.of(); // no permissions were found in DB.

    SyncPermissionsReq request = new SyncPermissionsReq(List.of(1L, 2L, 3L));
    when(roleRepository.findById(99L)).thenReturn(Optional.of(targetRole));
    when(permissionRepository.findAllById(List.of(1L, 2L, 3L))).thenReturn(targetPermissions);

    // Act + Assert
    assertThatThrownBy(() -> rolePermissionSyncService.syncPermissions(99L, request))
        .isInstanceOfSatisfying(
            PermissionNotFoundException.class,
            ex -> {
              assertThat(ex.getMessage())
                  .isEqualTo("One or more permission IDs provided do not exist.");
              assertThat(ex.getId()).isNull();
            });

    verify(roleRepository).findById(99L);
    verify(permissionRepository).findAllById(List.of(1L, 2L, 3L));
  }

  @Test
  void syncPermissions_shouldPersistAndReturnMappedResponse_whenRequestIsValid() {
    // Arrange
    Role targetRole = new Role();
    targetRole.setId(1L);
    targetRole.setDescription("Admin");

    RolePermission targetRolePermission1 = new RolePermission();
    targetRolePermission1.setId(1L);
    targetRolePermission1.setRole(targetRole);
    targetRolePermission1.setPermission(permission(8L, "catalogs:read"));

    RolePermission targetRolePermission2 = new RolePermission();
    targetRolePermission2.setId(2L);
    targetRolePermission2.setRole(targetRole);
    targetRolePermission2.setPermission(permission(2L, "figurines:read"));

    RolePermission targetRolePermission3 = new RolePermission();
    targetRolePermission3.setId(3L);
    targetRolePermission3.setRole(targetRole);
    targetRolePermission3.setPermission(permission(10L, "catalogs:read"));

    RolePermission targetRolePermission4 = new RolePermission();
    targetRolePermission4.setId(4L);
    targetRolePermission4.setRole(targetRole);
    targetRolePermission4.setPermission(permission(11L, "catalogs:update"));

    List<RolePermission> targetRolePermissions = new ArrayList<>();
    targetRolePermissions.add(targetRolePermission1);
    targetRolePermissions.add(targetRolePermission2);
    targetRolePermissions.add(targetRolePermission3);
    targetRolePermissions.add(targetRolePermission4);

    targetRole.setPermissions(targetRolePermissions);

    List<Permission> targetPermissions =
        List.of(
            permission(1L, "figurines:create"),
            permission(2L, "figurines:read"),
            permission(3L, "figurines:update")); // permissions in the DB

    SyncPermissionsReq request = new SyncPermissionsReq(List.of(1L, 2L, 3L));
    when(roleRepository.findById(99L)).thenReturn(Optional.of(targetRole));
    when(permissionRepository.findAllById(List.of(1L, 2L, 3L))).thenReturn(targetPermissions);
    when(roleRepository.save(any(Role.class)))
        .thenAnswer(
            invocation -> {
              Role entity = invocation.getArgument(0);
              // entity.setId(1L);
              return entity;
            });

    // Act
    rolePermissionSyncService.syncPermissions(99L, request);

    // Assert
    verify(roleRepository).findById(99L);
    verify(permissionRepository).findAllById(List.of(1L, 2L, 3L));

    ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
    verify(roleRepository).save(captor.capture());
    Role saved = captor.getValue();
    assertThat(saved.getDescription()).isEqualTo("Admin");
    List<RolePermission> allPermissions = saved.getPermissions();
    assertThat(allPermissions).hasSize(3);

    assertThat(allPermissions.getFirst().getId()).isEqualTo(2L);
    assertThat(allPermissions.getFirst().getPermission().getId()).isEqualTo(2L);

    assertThat(allPermissions.get(1).getId()).isNull();
    assertThat(allPermissions.get(1).getPermission().getId()).isEqualTo(1L);

    assertThat(allPermissions.getLast().getId()).isNull();
    assertThat(allPermissions.getLast().getPermission().getId()).isEqualTo(3L);

    saved.getPermissions().forEach(rp -> assertThat(rp.getRole()).isEqualTo(targetRole));
  }

  private Permission permission(Long id, String description) {
    Permission permission = new Permission();
    permission.setId(id);
    permission.setDescription(description);

    return permission;
  }
}
