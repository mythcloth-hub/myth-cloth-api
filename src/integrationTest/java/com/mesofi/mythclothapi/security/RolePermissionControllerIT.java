package com.mesofi.mythclothapi.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.springframework.http.ResponseEntity;

import com.mesofi.mythclothapi.it.ControllerBaseIT;
import com.mesofi.mythclothapi.security.permissions.dto.PermissionReq;
import com.mesofi.mythclothapi.security.permissions.dto.PermissionResp;
import com.mesofi.mythclothapi.security.rolepermissions.dto.RolePermissionReq;
import com.mesofi.mythclothapi.security.rolepermissions.dto.SyncPermissionsReq;
import com.mesofi.mythclothapi.security.roles.dto.RoleReq;
import com.mesofi.mythclothapi.security.roles.dto.RoleResp;

public class RolePermissionControllerIT extends ControllerBaseIT {

  private final String ROLE = "/roles";
  private final String PERMISSION = "/permissions";
  private final String ROLE_PERMISSION = "/roles/{roleId}/permissions";

  // @Test
  @DisplayName("Test flow to manage roles and permissions")
  void fullRolePermissionFlow() {

    Long idRoleAdmin;
    Long idRoleBasicCollector;
    Long idPermission1;
    Long idPermission2;
    Long idPermission3;
    Long idPermission4;
    Long idPermission5;

    // List existing roles.
    List<RoleResp> availableRoles = getAllRoles();
    if (availableRoles.isEmpty()) {
      // Create roles and permissions if they don't exist. This is to ensure the test can be run
      // multiple times without manual cleanup.
      idRoleAdmin = createRole("Admin");
      idRoleBasicCollector = createRole("Basic Collector");
    } else {
      idRoleAdmin = availableRoles.getFirst().id();
      idRoleBasicCollector = availableRoles.getLast().id();
    }

    idPermission1 = createPermission("catalogs:read");
    idPermission2 = createPermission("catalogs:write");
    idPermission3 = createPermission("catalogs:delete");
    idPermission4 = createPermission("catalogs:update");
    idPermission5 = createPermission("figurines:create");

    // perform associations between roles and permissions
    addPermissionToRole(idRoleAdmin, idPermission1);
    addPermissionToRole(idRoleAdmin, idPermission2);
    addPermissionToRole(idRoleAdmin, idPermission3);
    addPermissionToRole(idRoleAdmin, idPermission4);
    addPermissionToRole(idRoleBasicCollector, idPermission1);

    List<PermissionResp> permissionsAdminRole = getPermissionByRole(idRoleAdmin, 4);
    List<PermissionResp> permissionsBCollectorRole = getPermissionByRole(idRoleBasicCollector, 1);

    assertThat(permissionsAdminRole.getFirst().description()).isEqualTo("catalogs:read");
    assertThat(permissionsAdminRole.get(1).description()).isEqualTo("catalogs:write");
    assertThat(permissionsAdminRole.get(2).description()).isEqualTo("catalogs:delete");
    assertThat(permissionsAdminRole.get(3).description()).isEqualTo("catalogs:update");
    assertThat(permissionsBCollectorRole.getFirst().description()).isEqualTo("catalogs:read");

    // Sync or update permissions
    syncPermissions(
        idRoleAdmin,
        idPermission5,
        idPermission2); // add idPermission5 and keep idPermission2, remove the rest of permissions

    List<PermissionResp> permissionsSynedAdminRole = getPermissionByRole(idRoleAdmin, 2);
    assertThat(permissionsSynedAdminRole.getFirst().description()).isEqualTo("catalogs:write");
    assertThat(permissionsSynedAdminRole.get(1).description()).isEqualTo("figurines:create");

    // Delete all roles and permissions

    // List the available permissions
    List<PermissionResp> availablePermissions = getAllPermissions(5);
    assertThat(availablePermissions.getFirst().description()).isEqualTo("catalogs:read");
    assertThat(availablePermissions.get(1).description()).isEqualTo("catalogs:write");
    assertThat(availablePermissions.get(2).description()).isEqualTo("catalogs:delete");
    assertThat(availablePermissions.get(3).description()).isEqualTo("catalogs:update");
    assertThat(availablePermissions.get(4).description()).isEqualTo("figurines:create");

    for (PermissionResp permission : availablePermissions) {
      deletePermission(permission.id());
    }

    // OK, all permissions were deleted, along with their role associations.
    getAllPermissions(0);

    // For now, the roles cannot be deleted.
  }

  private void deletePermission(long permissionId) {
    rest.delete().uri(PERMISSION + "/{id}", permissionId).retrieve().toBodilessEntity();
  }

  private List<RoleResp> getAllRoles() {
    ResponseEntity<RoleResp[]> response =
        rest.get().uri(ROLE).retrieve().toEntity(RoleResp[].class);

    assertThat(response.getStatusCode()).isEqualTo(OK);
    assertThat(response.getBody()).isNotNull();

    return Arrays.asList(response.getBody());
  }

  private List<PermissionResp> getAllPermissions(int expectedNumberOfPermissions) {
    ResponseEntity<PermissionResp[]> response =
        rest.get().uri(PERMISSION).retrieve().toEntity(PermissionResp[].class);

    assertThat(response.getBody()).hasSize(expectedNumberOfPermissions);
    assertThat(response.getStatusCode()).isEqualTo(OK);
    assertThat(response.getBody()).isNotNull();

    return Arrays.asList(response.getBody());
  }

  private List<PermissionResp> getPermissionByRole(Long idRole, int expectedNumberOfPermissions) {
    ResponseEntity<PermissionResp[]> response =
        rest.get().uri(ROLE_PERMISSION, idRole).retrieve().toEntity(PermissionResp[].class);

    assertThat(response.getBody()).hasSize(expectedNumberOfPermissions);
    assertThat(response.getStatusCode()).isEqualTo(OK);
    assertThat(response.getBody()).isNotNull();

    return Arrays.asList(response.getBody());
  }

  private void addPermissionToRole(Long idRole, Long idPermission) {
    RolePermissionReq request = new RolePermissionReq(idPermission);
    rest.post().uri(ROLE_PERMISSION, idRole).body(request).retrieve().toBodilessEntity();
  }

  private Long createRole(String description) {
    RoleReq request = new RoleReq(description);

    ResponseEntity<RoleResp> response =
        rest.post().uri(ROLE).body(request).retrieve().toEntity(RoleResp.class);

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody()).isNotNull();

    return response.getBody().id();
  }

  private Long createPermission(String description) {
    PermissionReq request = new PermissionReq(description);

    ResponseEntity<PermissionResp> response =
        rest.post().uri(PERMISSION).body(request).retrieve().toEntity(PermissionResp.class);

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody()).isNotNull();

    return response.getBody().id();
  }

  private void syncPermissions(Long idRole, Long... idPermissions) {
    SyncPermissionsReq request = new SyncPermissionsReq(Arrays.stream(idPermissions).toList());

    rest.put().uri(ROLE_PERMISSION, idRole).body(request).retrieve().toBodilessEntity();
  }
}
