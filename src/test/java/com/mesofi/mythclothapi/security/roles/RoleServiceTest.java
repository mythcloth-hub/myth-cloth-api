package com.mesofi.mythclothapi.security.roles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.mesofi.mythclothapi.config.MapperTestConfig;
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

@SpringBootTest(classes = {RoleService.class, MapperTestConfig.class})
public class RoleServiceTest {

  @Autowired private RoleService roleService;

  @MockitoBean private RoleRepository roleRepository;
  @MockitoBean private PermissionRepository permissionRepository;

  @Test
  void createRole_shouldThrowRoleAlreadyExistsException_whenRoleAlreadyExists() {
    // Arrange
    Role existingRole = role(1L, "Admin");
    when(roleRepository.findByDescription("Admin")).thenReturn(Optional.of(existingRole));

    RoleReq request = new RoleReq("Admin");

    // Act + Assert
    assertThatThrownBy(() -> roleService.createRole(request))
        .isInstanceOfSatisfying(
            RoleAlreadyExistsException.class,
            ex -> {
              assertThat(ex.getMessage()).isEqualTo("Duplicate Role with description: 'Admin'");
              assertThat(ex.getDescription()).isEqualTo("Admin");
              assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
            });
  }

  @Test
  void createRole_shouldPersistAndReturnMappedResponse_whenRequestIsValid() {
    // Arrange
    when(roleRepository.findByDescription("Admin")).thenReturn(Optional.empty());
    when(roleRepository.save(any(Role.class)))
        .thenAnswer(
            invocation -> {
              Role entity = invocation.getArgument(0);
              entity.setId(1L);
              return entity;
            });

    RoleReq request = new RoleReq("Admin");

    // Act
    RoleResp response = roleService.createRole(request);

    // Assert
    assertThat(response).isEqualTo(new RoleResp(1L, "Admin"));

    ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
    verify(roleRepository).save(captor.capture());

    Role saved = captor.getValue();
    assertThat(saved.getDescription()).isEqualTo("Admin");
  }

  @Test
  void retrieveRole_shouldThrowRoleNotFoundException_whenRoleDoesNotExist() {
    // Arrange
    when(roleRepository.findById(99L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> roleService.retrieveRole(99L))
        .isInstanceOfSatisfying(
            RoleNotFoundException.class,
            ex -> {
              assertThat(ex.getMessage()).isEqualTo("Role not found");
              assertThat(ex.getId()).isEqualTo(99L);
            });

    verify(roleRepository).findById(99L);
  }

  @Test
  void retrieveRole_shouldReturnMappedResponse_whenRoleExists() {
    // Arrange
    Role role = role(7L, "Admin");

    when(roleRepository.findById(7L)).thenReturn(Optional.of(role));

    // Act
    RoleResp response = roleService.retrieveRole(7L);

    // Assert
    assertThat(response).isEqualTo(new RoleResp(7L, "Admin"));
    verify(roleRepository).findById(7L);
  }

  @Test
  void retrieveRoles_shouldReturnMappedResponses_whenRepositoryReturnsEntities() {
    // Arrange
    when(roleRepository.findAll(Sort.by("id")))
        .thenReturn(List.of(role(1L, "Admin"), role(2L, "Basic Collector")));

    // Act
    List<RoleResp> responses = roleService.retrieveRoles();

    // Assert
    assertThat(responses)
        .containsExactly(new RoleResp(1L, "Admin"), new RoleResp(2L, "Basic Collector"));

    verify(roleRepository).findAll(Sort.by("id"));
  }

  @Test
  void updateRole_shouldThrowRoleNotFoundException_whenRoleDoesNotExist() {
    // Arrange
    RoleReq request = new RoleReq("Admin");
    when(roleRepository.findById(77L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> roleService.updateRole(77L, request))
        .isInstanceOfSatisfying(
            RoleNotFoundException.class,
            ex -> {
              assertThat(ex.getMessage()).isEqualTo("Role not found");
              assertThat(ex.getId()).isEqualTo(77L);
            });

    verify(roleRepository).findById(77L);
    verify(roleRepository, never()).save(any(Role.class));
  }

  @Test
  void updateRole_shouldUpdateExistingEntityAndReturnMappedResponse_whenRequestIsValid() {
    // Arrange
    Role existing = role(3L, "Old Description");
    RoleReq request = new RoleReq("Updated Role");

    when(roleRepository.findById(3L)).thenReturn(Optional.of(existing));
    when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    RoleResp response = roleService.updateRole(3L, request);

    // Assert
    assertThat(response).isEqualTo(new RoleResp(3L, "Updated Role"));

    ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
    verify(roleRepository).save(captor.capture());

    Role saved = captor.getValue();
    assertThat(saved).isSameAs(existing);
    assertThat(saved.getDescription()).isEqualTo("Updated Role");

    verify(roleRepository).findById(3L);
  }

  @Test
  void addPermissionToRole_shouldThrowRoleNotFoundException_whenRoleDoesNotExist() {
    // Arrange
    when(roleRepository.findById(77L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> roleService.addPermissionToRole(77L, 88L))
        .isInstanceOfSatisfying(
            RoleNotFoundException.class,
            ex -> {
              assertThat(ex.getMessage()).isEqualTo("Role not found");
              assertThat(ex.getId()).isEqualTo(77L);
            });

    verify(roleRepository).findById(77L);
    verify(roleRepository, never()).save(any(Role.class));
  }

  @Test
  void addPermissionToRole_shouldThrowPermissionNotFoundException_whenPermissionDoesNotExist() {
    // Arrange
    when(roleRepository.findById(77L)).thenReturn(Optional.of(role(77L, "Admin")));
    when(permissionRepository.findById(88L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> roleService.addPermissionToRole(77L, 88L))
        .isInstanceOfSatisfying(
            PermissionNotFoundException.class,
            ex -> {
              assertThat(ex.getMessage()).isEqualTo("Permission not found");
              assertThat(ex.getId()).isEqualTo(88L);
            });

    verify(roleRepository).findById(77L);
    verify(permissionRepository).findById(88L);
    verify(roleRepository, never()).save(any(Role.class));
  }

  @Test
  void
      addPermissionToRole_shouldThrowRoleAlreadyAssociatedToPermissionException_whenAssociationExists() {
    // Arrange
    Role existing = role(77L, "Admin");
    RolePermission rp = new RolePermission();
    rp.setPermission(permission(88L, "figurines:write"));
    existing.setPermissions(List.of(rp));

    when(roleRepository.findById(77L)).thenReturn(Optional.of(existing));
    when(permissionRepository.findById(88L))
        .thenReturn(Optional.of(permission(88L, "figurines:write")));

    // Act + Assert
    assertThatThrownBy(() -> roleService.addPermissionToRole(77L, 88L))
        .isInstanceOfSatisfying(
            RoleAlreadyAssociatedToPermissionException.class,
            ex -> {
              assertThat(ex.getMessage())
                  .isEqualTo("Role with ID 77 already has permission 88 assigned.");
              assertThat(ex.getRoleId()).isEqualTo(77L);
              assertThat(ex.getPermissionId()).isEqualTo(88L);
            });

    verify(roleRepository).findById(77L);
    verify(permissionRepository).findById(88L);
    verify(roleRepository, never()).save(any(Role.class));
  }

  @Test
  void addPermissionToRole_shouldAddPermissionToRole_whenRequestIsValid() {
    // Arrange
    Role existing = role(77L, "Admin");
    RolePermission rp = new RolePermission();
    rp.setPermission(permission(100L, "figurines:read"));
    List<RolePermission> permissions = new ArrayList<>();
    permissions.add(rp);
    existing.setPermissions(permissions);

    when(roleRepository.findById(77L)).thenReturn(Optional.of(existing));
    when(permissionRepository.findById(88L))
        .thenReturn(Optional.of(permission(88L, "figurines:write")));
    when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act + Assert
    roleService.addPermissionToRole(77L, 88L);

    ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
    verify(roleRepository).save(captor.capture());

    Role saved = captor.getValue();
    assertThat(saved.getPermissions().size()).isEqualTo(2);
    assertThat(saved.getPermissions().get(1).getRole()).isNotNull();
    assertThat(saved.getPermissions().get(1).getPermission()).isNotNull();

    verify(roleRepository).findById(77L);
    verify(permissionRepository).findById(88L);
  }

  @Test
  void retrievePermissionsByRoleId_shouldReturnMappedResponses_whenRepositoryReturnsEntities_() {
    // Arrange
    when(roleRepository.findAll(Sort.by("id")))
        .thenReturn(List.of(role(1L, "Admin"), role(2L, "Basic Collector")));

    // Act
    List<RoleResp> responses = roleService.retrieveRoles();

    // Assert
    assertThat(responses)
        .containsExactly(new RoleResp(1L, "Admin"), new RoleResp(2L, "Basic Collector"));

    verify(roleRepository).findAll(Sort.by("id"));
  }

  @Test
  void retrievePermissionsByRoleId_shouldThrowRoleNotFoundException_whenRoleDoesNotExist() {
    // Arrange
    when(roleRepository.findById(77L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> roleService.retrievePermissionsByRoleId(77L))
        .isInstanceOfSatisfying(
            RoleNotFoundException.class,
            ex -> {
              assertThat(ex.getMessage()).isEqualTo("Role not found");
              assertThat(ex.getId()).isEqualTo(77L);
            });

    verify(roleRepository).findById(77L);
    verify(roleRepository, never()).save(any(Role.class));
  }

  @Test
  void retrievePermissionsByRoleId_shouldReturnMappedResponses_whenRepositoryReturnsEntities() {
    // Arrange
    Role existingAdmin = role(77L, "Admin");

    RolePermission rp1 = new RolePermission();
    rp1.setPermission(permission(100L, "figurines:write"));
    RolePermission rp2 = new RolePermission();
    rp2.setPermission(permission(88L, "figurines:write"));
    existingAdmin.setPermissions(List.of(rp1, rp2));

    when(roleRepository.findById(77L)).thenReturn(Optional.of(existingAdmin));

    // Act
    List<PermissionResp> responses = roleService.retrievePermissionsByRoleId(77L);

    // Assert
    assertThat(responses)
        .containsExactly(
            new PermissionResp(100L, "figurines:write"),
            new PermissionResp(88L, "figurines:write"));

    verify(roleRepository).findById(77L);
  }

  private Role role(Long id, String description) {
    Role role = new Role();
    role.setId(id);
    role.setDescription(description);

    return role;
  }

  private Permission permission(Long id, String description) {
    Permission permission = new Permission();
    permission.setId(id);
    permission.setDescription(description);

    return permission;
  }
}
