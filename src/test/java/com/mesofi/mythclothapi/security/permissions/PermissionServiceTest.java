package com.mesofi.mythclothapi.security.permissions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.mesofi.mythclothapi.security.permissions.dto.PermissionReq;
import com.mesofi.mythclothapi.security.permissions.dto.PermissionResp;
import com.mesofi.mythclothapi.security.permissions.exceptions.PermissionAlreadyExistsException;
import com.mesofi.mythclothapi.security.permissions.exceptions.PermissionNotFoundException;
import com.mesofi.mythclothapi.security.permissions.model.Permission;

@SpringBootTest(classes = {PermissionService.class, MapperTestConfig.class})
public class PermissionServiceTest {

  @Autowired private PermissionService permissionService;

  @MockitoBean private PermissionRepository permissionRepository;

  @Test
  void createPermission_shouldThrowPermissionAlreadyExistsException_whenPermissionAlreadyExists() {
    // Arrange
    Permission existingPermission = permission(1L, "figurines:create");
    when(permissionRepository.findByDescription("figurines:create"))
        .thenReturn(Optional.of(existingPermission));

    PermissionReq request = new PermissionReq("figurines:create");

    // Act + Assert
    assertThatThrownBy(() -> permissionService.createPermission(request))
        .isInstanceOfSatisfying(
            PermissionAlreadyExistsException.class,
            ex -> {
              assertThat(ex.getMessage())
                  .isEqualTo("Duplicate Permission with description: 'figurines:create'");
              assertThat(ex.getDescription()).isEqualTo("figurines:create");
              assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
            });
  }

  @Test
  void createPermission_shouldPersistAndReturnMappedResponse_whenRequestIsValid() {
    // Arrange
    when(permissionRepository.findByDescription("figurines:create")).thenReturn(Optional.empty());
    when(permissionRepository.save(any(Permission.class)))
        .thenAnswer(
            invocation -> {
              Permission entity = invocation.getArgument(0);
              entity.setId(1L);
              return entity;
            });

    PermissionReq request = new PermissionReq("figurines:create");

    // Act
    PermissionResp response = permissionService.createPermission(request);

    // Assert
    assertThat(response).isEqualTo(new PermissionResp(1L, "figurines:create"));

    ArgumentCaptor<Permission> captor = ArgumentCaptor.forClass(Permission.class);
    verify(permissionRepository).save(captor.capture());

    Permission saved = captor.getValue();
    assertThat(saved.getDescription()).isEqualTo("figurines:create");
  }

  @Test
  void retrievePermission_shouldThrowPermissionNotFoundException_whenPermissionDoesNotExist() {
    // Arrange
    when(permissionRepository.findById(99L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> permissionService.retrievePermission(99L))
        .isInstanceOfSatisfying(
            PermissionNotFoundException.class,
            ex -> {
              assertThat(ex.getMessage()).isEqualTo("Permission not found");
              assertThat(ex.getId()).isEqualTo(99L);
            });

    verify(permissionRepository).findById(99L);
  }

  @Test
  void retrievePermission_shouldReturnMappedResponse_whenPermissionExists() {
    // Arrange
    Permission permission = permission(7L, "figurines:read");

    when(permissionRepository.findById(7L)).thenReturn(Optional.of(permission));

    // Act
    PermissionResp response = permissionService.retrievePermission(7L);

    // Assert
    assertThat(response).isEqualTo(new PermissionResp(7L, "figurines:read"));
    verify(permissionRepository).findById(7L);
  }

  @Test
  void retrievePermission_shouldReturnMappedResponses_whenRepositoryReturnsEntities() {
    // Arrange
    when(permissionRepository.findAll(Sort.by("id")))
        .thenReturn(List.of(permission(1L, "figurines:read"), permission(2L, "figurines:update")));

    // Act
    List<PermissionResp> responses = permissionService.retrievePermissions();

    // Assert
    assertThat(responses)
        .containsExactly(
            new PermissionResp(1L, "figurines:read"), new PermissionResp(2L, "figurines:update"));

    verify(permissionRepository).findAll(Sort.by("id"));
  }

  @Test
  void updatePermission_shouldThrowPermissionNotFoundException_whenPermissionDoesNotExist() {
    // Arrange
    PermissionReq request = new PermissionReq("figurines:read");
    when(permissionRepository.findById(77L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> permissionService.updatePermission(77L, request))
        .isInstanceOfSatisfying(
            PermissionNotFoundException.class,
            ex -> {
              assertThat(ex.getMessage()).isEqualTo("Permission not found");
              assertThat(ex.getId()).isEqualTo(77L);
            });

    verify(permissionRepository).findById(77L);
    verify(permissionRepository, never()).save(any(Permission.class));
  }

  @Test
  void updatePermission_shouldUpdateExistingEntityAndReturnMappedResponse_whenRequestIsValid() {
    // Arrange
    Permission existing = permission(3L, "Old Description");
    PermissionReq request = new PermissionReq("Updated Permission");

    when(permissionRepository.findById(3L)).thenReturn(Optional.of(existing));
    when(permissionRepository.save(any(Permission.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    PermissionResp response = permissionService.updatePermission(3L, request);

    // Assert
    assertThat(response).isEqualTo(new PermissionResp(3L, "Updated Permission"));

    ArgumentCaptor<Permission> captor = ArgumentCaptor.forClass(Permission.class);
    verify(permissionRepository).save(captor.capture());

    Permission saved = captor.getValue();
    assertThat(saved).isSameAs(existing);
    assertThat(saved.getDescription()).isEqualTo("Updated Permission");

    verify(permissionRepository).findById(3L);
  }

  @Test
  void removePermission_shouldThrowPermissionNotFoundException_whenPermissionDoesNotExist() {
    // Arrange
    when(permissionRepository.existsById(1L)).thenReturn(false);

    // Act + Assert
    assertThatThrownBy(() -> permissionService.removePermission(1L))
        .isInstanceOfSatisfying(
            PermissionNotFoundException.class,
            ex -> {
              assertThat(ex.getMessage()).isEqualTo("Permission not found");
              assertThat(ex.getId()).isEqualTo(1L);
              assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            });
  }

  @Test
  void removePermission_shouldPersistAndReturnMappedResponse_whenRequestIsValid() {
    // Arrange
    when(permissionRepository.existsById(1L)).thenReturn(true);
    when(permissionRepository.save(any(Permission.class)))
        .thenAnswer(
            invocation -> {
              Permission entity = invocation.getArgument(0);
              entity.setId(1L);
              return entity;
            });

    // Act
    permissionService.removePermission(1L);

    // Assert
    ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
    verify(permissionRepository).deleteById(captor.capture());

    Long saved = captor.getValue();
    assertThat(saved).isEqualTo(1L);
  }

  private Permission permission(Long id, String description) {
    Permission permission = new Permission();
    permission.setId(id);
    permission.setDescription(description);

    return permission;
  }
}
