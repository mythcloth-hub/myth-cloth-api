package com.mesofi.mythclothapi.security.service;

import static com.mesofi.mythclothapi.security.roles.model.RoleType.ADMIN;
import static com.mesofi.mythclothapi.security.roles.model.RoleType.COLLECTOR;
import static com.mesofi.mythclothapi.security.roles.model.RoleType.DEMO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.mesofi.mythclothapi.security.permissions.PermissionRepository;
import com.mesofi.mythclothapi.security.permissions.model.Permission;
import com.mesofi.mythclothapi.security.permissions.model.Permissions;
import com.mesofi.mythclothapi.security.rolepermissions.RolePermission;
import com.mesofi.mythclothapi.security.rolepermissions.RolePermissionRepository;
import com.mesofi.mythclothapi.security.roles.RoleRepository;
import com.mesofi.mythclothapi.security.roles.model.Role;

@ActiveProfiles("test")
@SpringBootTest(classes = SecurityDataService.class)
class SecurityDataServiceTest {

    @Autowired
    private SecurityDataService securityDataService;

    @MockitoBean
    private RoleRepository roleRepository;

    @MockitoBean
    private PermissionRepository permissionRepository;

    @MockitoBean
    private RolePermissionRepository rolePermissionRepository;

    @BeforeEach
    void setUp() throws Exception {
        clearStaticMap("roleMap");
        clearStaticMap("permissionMap");
    }

    @Test
    void initializeSecurityData_shouldSkipInitialization_whenRolePermissionsAlreadyExist() {
        // Arrange
        when(rolePermissionRepository.count()).thenReturn(1L);

        // Act
        securityDataService.initializeSecurityData();

        // Assert
        verify(rolePermissionRepository).count();
        verify(roleRepository, never()).findByName(anyString());
        verify(permissionRepository, never()).findByName(anyString());
        verify(rolePermissionRepository, never()).save(any(RolePermission.class));
    }

    @Test
    void initializeSecurityData_shouldCreateRolesPermissionsAndRelationships_whenRepositoryIsEmpty() {
        // Arrange
        when(rolePermissionRepository.count()).thenReturn(0L);

        Role adminRole = role(1L, ADMIN.getDisplayName());
        Role collectorRole = role(2L, COLLECTOR.getDisplayName());
        Role demoRole = role(3L, DEMO.getDisplayName());
        when(roleRepository.findByName(anyString())).thenAnswer(invocation -> {
            String roleName = invocation.getArgument(0);
            return switch (roleName) {
                case "Admin" -> Optional.of(adminRole);
                case "Collector" -> Optional.empty();
                case "Demo" -> Optional.of(demoRole);
                default -> Optional.empty();
            };
        });
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role entity = invocation.getArgument(0);
            entity.setId(99L);
            return entity;
        });

        Permission permissionsRead = permission(10L, Permissions.PERMISSIONS_READ);
        when(permissionRepository.findByName(anyString())).thenAnswer(invocation -> {
            String permissionName = invocation.getArgument(0);
            return Permissions.PERMISSIONS_READ.equals(permissionName)
                    ? Optional.of(permissionsRead)
                    : Optional.empty();
        });
        when(permissionRepository.save(any(Permission.class))).thenAnswer(invocation -> {
            Permission entity = invocation.getArgument(0);
            entity.setId(200L);
            return entity;
        });

        when(rolePermissionRepository.save(any(RolePermission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        securityDataService.initializeSecurityData();

        // Assert
        verify(rolePermissionRepository).count();
        verify(roleRepository).findByName(ADMIN.getDisplayName());
        verify(roleRepository).findByName(COLLECTOR.getDisplayName());
        verify(roleRepository).findByName(DEMO.getDisplayName());
        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(roleCaptor.capture());
        verify(permissionRepository).findByName(Permissions.PERMISSIONS_READ);
        ArgumentCaptor<Permission> permissionCaptor = ArgumentCaptor.forClass(Permission.class);
        verify(permissionRepository, times(getAvailablePermissionsCount() - 1)).save(permissionCaptor.capture());
        assertThat(getAvailablePermissionsCount()).isGreaterThan(1);
        ArgumentCaptor<RolePermission> rolePermissionCaptor = ArgumentCaptor.forClass(RolePermission.class);
        verify(rolePermissionRepository, times(getRolePermissionCount())).save(rolePermissionCaptor.capture());
        assertThat(adminRole.getName()).isEqualTo(ADMIN.getDisplayName());
        assertThat(demoRole.getName()).isEqualTo(DEMO.getDisplayName());
        assertThat(roleCaptor.getValue().getName()).isEqualTo(COLLECTOR.getDisplayName());
        assertThat(permissionsRead.getName()).isEqualTo(Permissions.PERMISSIONS_READ);
        assertThat(permissionCaptor.getAllValues())
                .allSatisfy(permission -> assertThat(permission.getName()).isNotNull());
        assertThat(permissionCaptor.getAllValues()).extracting(Permission::getName)
                .contains(Permissions.ROLES_PERMISSIONS_ASSIGN, Permissions.STORES_WRITE);
        assertThat(rolePermissionCaptor.getAllValues()).allSatisfy(rolePermission -> {
            assertThat(rolePermission.getRole()).isNotNull();
            assertThat(rolePermission.getPermission()).isNotNull();
            assertThat(rolePermission.getRole().getName()).isNotNull();
            assertThat(rolePermission.getPermission().getName()).isNotNull();
        });
    }

    private Role role(Long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        return role;
    }

    private Permission permission(Long id, String name) {
        Permission permission = new Permission();
        permission.setId(id);
        permission.setName(name);
        return permission;
    }

    private void clearStaticMap(String fieldName) throws Exception {
        Field field = SecurityDataService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        Map<?, ?> map = (Map<?, ?>) field.get(null);
        map.clear();
    }

    private int getAvailablePermissionsCount() {
        try {
            Field field = SecurityDataService.class.getDeclaredField("AVAILABLE_PERMISSIONS");
            field.setAccessible(true);
            return ((List<?>) field.get(null)).size();
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private int getRolePermissionCount() {
        try {
            Field field = SecurityDataService.class.getDeclaredField("ROLE_PERMISSIONS_MAP");
            field.setAccessible(true);

            Map<?, ?> rolePermissionsMap = (Map<?, ?>) field.get(null);
            return rolePermissionsMap.values().stream().mapToInt(value -> ((List<?>) value).size()).sum();
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
