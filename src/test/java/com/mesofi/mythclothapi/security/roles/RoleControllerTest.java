package com.mesofi.mythclothapi.security.roles;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
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

import com.mesofi.mythclothapi.security.roles.dto.RoleReq;
import com.mesofi.mythclothapi.security.roles.dto.RoleResp;
import com.mesofi.mythclothapi.security.roles.exceptions.RoleNotFoundException;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(RoleController.class)
public class RoleControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private RoleService service;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void createRole_shouldReturn400_whenRequestBodyIsMissing() throws Exception {
    mockMvc
        .perform(post("/roles"))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.detail")
                .value(
                    "Required request body is missing: public org.springframework.http.ResponseEntity<com.mesofi.mythclothapi.security.roles.dto.RoleResp> com.mesofi.mythclothapi.security.roles.RoleController.createRole(com.mesofi.mythclothapi.security.roles.dto.RoleReq)"))
        .andExpect(jsonPath("$.instance").value("/roles"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Invalid body"))
        .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(service);
  }

  @Test
  void createRole_shouldReturn415_whenContentTypeIsMissing() throws Exception {

    mockMvc
        .perform(post("/roles").content("{}"))
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(
            jsonPath("$.detail").value("Content-Type 'application/octet-stream' is not supported"))
        .andExpect(jsonPath("$.instance").value("/roles"))
        .andExpect(jsonPath("$.status").value("415"))
        .andExpect(jsonPath("$.title").value("Unsupported Media Type"))
        .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(service);
  }

  @Test
  void createRole_shouldReturn400_whenRequestBodyFailsValidation() throws Exception {

    mockMvc
        .perform(post("/roles").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
        .andExpect(jsonPath("$.instance").value("/roles"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Validation Failed"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.errors.description").value("description must not be blank"));

    verifyNoInteractions(service);
  }

  @Test
  void createRole_shouldReturn400_whenDescriptionExceedsMaxLength() throws Exception {
    String requestBody = "{\"description\":\"%s\"}".formatted("a".repeat(201));

    mockMvc
        .perform(post("/roles").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
        .andExpect(jsonPath("$.instance").value("/roles"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Validation Failed"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(
            jsonPath("$.errors.description").value("description must not exceed 200 characters"));

    verifyNoInteractions(service);
  }

  @Test
  void createRole_shouldReturn201AndLocationHeader() throws Exception {
    RoleReq request = new RoleReq("Admin");
    RoleResp response = new RoleResp(1L, "Admin");

    when(service.createRole(request)).thenReturn(response);

    mockMvc
        .perform(
            post("/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/roles/1"))
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.description").value("Admin"));

    verify(service).createRole(request);
  }

  @Test
  void retrieveRole_shouldReturn200_whenRoleExists() throws Exception {
    RoleResp response = new RoleResp(7L, "Admin");

    when(service.retrieveRole(7L)).thenReturn(response);

    mockMvc
        .perform(get("/roles/{id}", 7L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(7L))
        .andExpect(jsonPath("$.description").value("Admin"));

    verify(service).retrieveRole(7L);
  }

  @Test
  void retrieveRole_shouldReturn404_whenRoleDoesNotExist() throws Exception {
    when(service.retrieveRole(7L)).thenThrow(new RoleNotFoundException(7L));

    mockMvc
        .perform(get("/roles/{id}", 7L))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("Role not found"))
        .andExpect(jsonPath("$.instance").value("/roles/7"))
        .andExpect(jsonPath("$.status").value("404"))
        .andExpect(jsonPath("$.title").value("Role not found"))
        .andExpect(jsonPath("$.timestamp").exists());

    verify(service).retrieveRole(7L);
  }

  @Test
  void retrieveRole_shouldReturn200_whenRoleEntriesExist() throws Exception {
    when(service.retrieveRoles())
        .thenReturn(List.of(new RoleResp(1L, "Admin"), new RoleResp(2L, "Basic Collector")));

    mockMvc
        .perform(get("/roles"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].description").value("Admin"))
        .andExpect(jsonPath("$[1].id").value(2L))
        .andExpect(jsonPath("$[1].description").value("Basic Collector"));

    verify(service).retrieveRoles();
  }

  @Test
  void updateRole_shouldReturn200_whenRoleEntriesExist() throws Exception {
    RoleReq request = new RoleReq("Admin");
    RoleResp response = new RoleResp(1L, "Admin");

    when(service.updateRole(1L, request)).thenReturn(response);

    mockMvc
        .perform(
            put("/roles/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.description").value("Admin"));

    verify(service).updateRole(1L, request);
  }
}
