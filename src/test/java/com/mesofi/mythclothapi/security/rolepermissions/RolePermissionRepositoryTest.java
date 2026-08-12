package com.mesofi.mythclothapi.security.rolepermissions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.mesofi.mythclothapi.security.permissions.PermissionRepository;
import com.mesofi.mythclothapi.security.permissions.model.Permission;
import com.mesofi.mythclothapi.security.roles.RoleRepository;
import com.mesofi.mythclothapi.security.roles.model.Role;

@DataJpaTest
@ActiveProfiles("test")
class RolePermissionRepositoryTest {

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Test
    void shouldThrowException_whenRoleIsNull() {
        // Arrange
        Permission permission = new Permission();
        permission.setName("figurines:read");
        permission.setCreationDate(Instant.now());
        permission.setUpdateDate(Instant.now());
        Permission savedPermission = permissionRepository.saveAndFlush(permission);

        RolePermission rolePermission = new RolePermission();
        rolePermission.setPermission(savedPermission);

        // Act + Assert
        assertThatThrownBy(() -> rolePermissionRepository.saveAndFlush(rolePermission))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldPersistRolePermission_whenRequiredFieldsArePresent() {
        // Arrange
        Role role = new Role();
        role.setName("Admin");
        role.setCreationDate(Instant.now());
        role.setUpdateDate(Instant.now());
        Role savedRole = roleRepository.saveAndFlush(role);

        Permission permission = new Permission();
        permission.setName("figurines:read");
        permission.setCreationDate(Instant.now());
        permission.setUpdateDate(Instant.now());
        Permission savedPermission = permissionRepository.saveAndFlush(permission);

        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(savedRole);
        rolePermission.setPermission(savedPermission);
        rolePermission.setCreationDate(Instant.now());
        rolePermission.setUpdateDate(Instant.now());

        // Act
        RolePermission saved = rolePermissionRepository.saveAndFlush(rolePermission);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRole()).isEqualTo(savedRole);
        assertThat(saved.getPermission()).isEqualTo(savedPermission);
        assertThat(saved.getCreationDate()).isNotNull();
        assertThat(saved.getUpdateDate()).isNotNull();
    }
}
