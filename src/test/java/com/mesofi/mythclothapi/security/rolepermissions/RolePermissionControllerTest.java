package com.mesofi.mythclothapi.security.rolepermissions;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mesofi.mythclothapi.security.permissions.dto.PermissionResp;
import com.mesofi.mythclothapi.security.rolepermissions.dto.RolePermissionReq;
import com.mesofi.mythclothapi.security.rolepermissions.dto.SyncPermissionsReq;
import com.mesofi.mythclothapi.security.roles.RoleService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(RolePermissionController.class)
public class RolePermissionControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private RoleService roleService;

  @MockitoBean private RolePermissionSyncService syncService;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void addPermissionToRole_shouldReturn204_whenPermissionAddedToRole() throws Exception {
    RolePermissionReq request = new RolePermissionReq(2L);

    doNothing().when(roleService).addPermissionToRole(1L, 2L);

    mockMvc
        .perform(
            post("/roles/{roleId}/permissions", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());

    verify(roleService).addPermissionToRole(1L, 2L);
  }

  @Test
  void retrievePermissionsByRoleId_shouldReturn200_whenPermissionEntriesExist() throws Exception {
    when(roleService.retrievePermissionsByRoleId(1L))
        .thenReturn(
            List.of(
                new PermissionResp(1L, "figurines:read"),
                new PermissionResp(2L, "figurines:write")));

    mockMvc
        .perform(get("/roles/{roleId}/permissions", 1L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].description").value("figurines:read"))
        .andExpect(jsonPath("$[1].id").value(2L))
        .andExpect(jsonPath("$[1].description").value("figurines:write"));

    verify(roleService).retrievePermissionsByRoleId(1L);
  }

  @Test
  void syncRolePermissions_shouldReturn204_whenPermissionSyncIsCorrect() throws Exception {
    SyncPermissionsReq request = new SyncPermissionsReq(List.of(1L, 2L, 3L));

    doNothing().when(syncService).syncPermissions(1L, request);

    mockMvc
        .perform(
            put("/roles/{roleId}/permissions", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());

    verify(syncService).syncPermissions(1L, request);
  }
}
