package com.mesofi.mythclothapi.security.permissions;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mesofi.mythclothapi.security.permissions.dto.PermissionReq;
import com.mesofi.mythclothapi.security.permissions.dto.PermissionResp;
import com.mesofi.mythclothapi.security.permissions.exceptions.PermissionNotFoundException;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(PermissionController.class)
public class PermissionControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private PermissionService service;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void createPermission_shouldReturn400_whenRequestBodyIsMissing() throws Exception {
    mockMvc
        .perform(post("/permissions"))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.detail")
                .value(
                    "Required request body is missing: public org.springframework.http.ResponseEntity<com.mesofi.mythclothapi.security.permissions.dto.PermissionResp> com.mesofi.mythclothapi.security.permissions.PermissionController.createPermission(com.mesofi.mythclothapi.security.permissions.dto.PermissionReq)"))
        .andExpect(jsonPath("$.instance").value("/permissions"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Invalid body"))
        .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(service);
  }

  @Test
  void createPermission_shouldReturn415_whenContentTypeIsMissing() throws Exception {

    mockMvc
        .perform(post("/permissions").content("{}"))
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(
            jsonPath("$.detail").value("Content-Type 'application/octet-stream' is not supported"))
        .andExpect(jsonPath("$.instance").value("/permissions"))
        .andExpect(jsonPath("$.status").value("415"))
        .andExpect(jsonPath("$.title").value("Unsupported Media Type"))
        .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(service);
  }

  @Test
  void createPermission_shouldReturn400_whenRequestBodyFailsValidation() throws Exception {

    mockMvc
        .perform(post("/permissions").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
        .andExpect(jsonPath("$.instance").value("/permissions"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Validation Failed"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.errors.description").value("description must not be blank"));

    verifyNoInteractions(service);
  }

  @Test
  void createPermission_shouldReturn201AndLocationHeader() throws Exception {
    PermissionReq request = new PermissionReq("figurines:read");
    PermissionResp response = new PermissionResp(1L, "figurines:read");

    when(service.createPermission(request)).thenReturn(response);

    mockMvc
        .perform(
            post("/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/permissions/1"))
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.description").value("figurines:read"));

    verify(service).createPermission(request);
  }

  @Test
  void retrievePermission_shouldReturn200_whenPermissionExists() throws Exception {
    PermissionResp response = new PermissionResp(7L, "figurines:read");

    when(service.retrievePermission(7L)).thenReturn(response);

    mockMvc
        .perform(get("/permissions/{id}", 7L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(7L))
        .andExpect(jsonPath("$.description").value("figurines:read"));

    verify(service).retrievePermission(7L);
  }

  @Test
  void retrievePermission_shouldReturn404_whenPermissionDoesNotExist() throws Exception {
    when(service.retrievePermission(7L)).thenThrow(new PermissionNotFoundException(7L));

    mockMvc
        .perform(get("/permissions/{id}", 7L))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("Permission not found"))
        .andExpect(jsonPath("$.instance").value("/permissions/7"))
        .andExpect(jsonPath("$.status").value("404"))
        .andExpect(jsonPath("$.title").value("Permission not found"))
        .andExpect(jsonPath("$.timestamp").exists());

    verify(service).retrievePermission(7L);
  }

  @Test
  void retrievePermission_shouldReturn200_whenPermissionEntriesExist() throws Exception {
    when(service.retrievePermissions())
        .thenReturn(
            List.of(
                new PermissionResp(1L, "figurines:read"),
                new PermissionResp(2L, "figurines:write")));

    mockMvc
        .perform(get("/permissions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].description").value("figurines:read"))
        .andExpect(jsonPath("$[1].id").value(2L))
        .andExpect(jsonPath("$[1].description").value("figurines:write"));

    verify(service).retrievePermissions();
  }

  @Test
  void updatePermission_shouldReturn200_whenPermissionEntriesExist() throws Exception {
    PermissionReq request = new PermissionReq("figurines:read");
    PermissionResp response = new PermissionResp(1L, "figurines:write");

    when(service.updatePermission(1L, request)).thenReturn(response);

    mockMvc
        .perform(
            put("/permissions/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.description").value("figurines:write"));

    verify(service).updatePermission(1L, request);
  }

  @Test
  void removePermission_shouldReturn204_whenPermissionIsDeleted() throws Exception {
    doNothing().when(service).removePermission(1L);

    mockMvc.perform(delete("/permissions/{id}", 1L)).andExpect(status().isNoContent());

    verify(service).removePermission(1L);
  }
}
