package com.mesofi.mythclothapi.catalogs;

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

import com.mesofi.mythclothapi.catalogs.dto.CatalogReq;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.catalogs.exceptions.CatalogNotFoundException;
import com.mesofi.mythclothapi.catalogs.exceptions.RepositoryNotFoundException;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(CatalogController.class)
class CatalogControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CatalogService service;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void createCatalog_shouldReturn404_whenPostingToRootPath() throws Exception {

    mockMvc
        .perform(post("/"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("The URL you are calling does not exist."))
        .andExpect(jsonPath("$.instance").value("/"))
        .andExpect(jsonPath("$.status").value("404"))
        .andExpect(jsonPath("$.title").value("Endpoint not found"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void createCatalog_shouldReturn400_whenRequestBodyIsMissing() throws Exception {

    mockMvc
        .perform(post("/catalogs/groups"))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.detail")
                .value(
                    "Required request body is missing: public org.springframework.http.ResponseEntity<com.mesofi.mythclothapi.catalogs.dto.CatalogResp> com.mesofi.mythclothapi.catalogs.CatalogController.createCatalog(com.mesofi.mythclothapi.catalogs.dto.CatalogType,com.mesofi.mythclothapi.catalogs.dto.CatalogReq)"))
        .andExpect(jsonPath("$.instance").value("/catalogs/groups"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Invalid body"))
        .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(service);
  }

  @Test
  void createCatalog_shouldReturn415_whenContentTypeIsMissing() throws Exception {

    mockMvc
        .perform(post("/catalogs/groups").content("{}"))
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(
            jsonPath("$.detail").value("Content-Type 'application/octet-stream' is not supported"))
        .andExpect(jsonPath("$.instance").value("/catalogs/groups"))
        .andExpect(jsonPath("$.status").value("415"))
        .andExpect(jsonPath("$.title").value("Unsupported Media Type"))
        .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(service);
  }

  @Test
  void createCatalog_shouldReturn400_whenRequestBodyFailsValidation() throws Exception {

    mockMvc
        .perform(post("/catalogs/groups").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
        .andExpect(jsonPath("$.instance").value("/catalogs/groups"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Validation Failed"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.errors.description").value("description must not be blank"));

    verifyNoInteractions(service);
  }

  @Test
  void createCatalog_shouldReturn400_whenDescriptionExceedsMaxLength() throws Exception {
    String requestBody = "{\"description\":\"%s\"}".formatted("a".repeat(101));

    mockMvc
        .perform(
            post("/catalogs/groups").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
        .andExpect(jsonPath("$.instance").value("/catalogs/groups"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Validation Failed"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(
            jsonPath("$.errors.description").value("description must not exceed 100 characters"));

    verifyNoInteractions(service);
  }

  @Test
  void createCatalog_shouldReturn400_whenCatalogTypeIsUnknown() throws Exception {

    mockMvc
        .perform(
            post("/catalogs/unknown")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\":\"Gold Saints\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Your request parameters didn't convert correctly"))
        .andExpect(jsonPath("$.instance").value("/catalogs/unknown"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Validation Failed"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(
            jsonPath("$.error")
                .value(
                    "Value 'unknown' is not valid, provide one of the following values: [distributions, groups, lineups, series]"));

    verifyNoInteractions(service);
  }

  @Test
  void createCatalog_shouldReturn201AndLocationHeader() throws Exception {
    CatalogReq request = new CatalogReq("Gold Saints");
    CatalogResp response = new CatalogResp(1L, "Gold Saints");

    when(service.createCatalog("groups", request)).thenReturn(response);

    mockMvc
        .perform(
            post("/catalogs/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/catalogs/groups/1"))
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.description").value("Gold Saints"));

    verify(service).createCatalog("groups", request);
  }

  @Test
  void createCatalog_shouldReturn404_whenCatalogRepositoryDoesNotExist() throws Exception {
    CatalogReq request = new CatalogReq("Gold Saints");

    when(service.createCatalog("groups", request))
        .thenThrow(new RepositoryNotFoundException("groups"));

    mockMvc
        .perform(
            post("/catalogs/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("Repository not found: groups"))
        .andExpect(jsonPath("$.instance").value("/catalogs/groups"))
        .andExpect(jsonPath("$.status").value("404"))
        .andExpect(jsonPath("$.title").value("Repository not found: groups"))
        .andExpect(jsonPath("$.timestamp").exists());

    verify(service).createCatalog("groups", request);
  }

  @Test
  void retrieveCatalog_shouldReturn200_whenCatalogExists() throws Exception {
    CatalogResp response = new CatalogResp(7L, "Asgard");

    when(service.retrieveCatalog("groups", 7L)).thenReturn(response);

    mockMvc
        .perform(get("/catalogs/groups/{id}", 7L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(7L))
        .andExpect(jsonPath("$.description").value("Asgard"));

    verify(service).retrieveCatalog("groups", 7L);
  }

  @Test
  void retrieveCatalog_shouldReturn404_whenCatalogDoesNotExist() throws Exception {
    when(service.retrieveCatalog("groups", 7L)).thenThrow(new CatalogNotFoundException("groups"));

    mockMvc
        .perform(get("/catalogs/groups/{id}", 7L))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("Catalog not found: groups"))
        .andExpect(jsonPath("$.instance").value("/catalogs/groups/7"))
        .andExpect(jsonPath("$.status").value("404"))
        .andExpect(jsonPath("$.title").value("Catalog not found: groups"))
        .andExpect(jsonPath("$.timestamp").exists());

    verify(service).retrieveCatalog("groups", 7L);
  }

  @Test
  void retrieveCatalogs_shouldReturn200_whenCatalogEntriesExist() throws Exception {
    when(service.retrieveCatalogs("groups"))
        .thenReturn(List.of(new CatalogResp(1L, "Bronze Saints"), new CatalogResp(2L, "Asgard")));

    mockMvc
        .perform(get("/catalogs/groups"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].description").value("Bronze Saints"))
        .andExpect(jsonPath("$[1].id").value(2L))
        .andExpect(jsonPath("$[1].description").value("Asgard"));

    verify(service).retrieveCatalogs("groups");
  }

  @Test
  void retrieveCatalogs_shouldReturn200WithEmptyList_whenCatalogHasNoEntries() throws Exception {
    when(service.retrieveCatalogs("groups")).thenReturn(List.of());

    mockMvc
        .perform(get("/catalogs/groups"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));

    verify(service).retrieveCatalogs("groups");
  }

  @Test
  void updateCatalog_shouldReturn200_whenRequestIsValid() throws Exception {
    CatalogReq request = new CatalogReq("Athena Army");
    CatalogResp response = new CatalogResp(11L, "Athena Army");

    when(service.updateCatalog("groups", 11L, request)).thenReturn(response);

    mockMvc
        .perform(
            put("/catalogs/groups/{id}", 11L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(11L))
        .andExpect(jsonPath("$.description").value("Athena Army"));

    verify(service).updateCatalog("groups", 11L, request);
  }

  @Test
  void updateCatalog_shouldReturn400_whenRequestBodyFailsValidation() throws Exception {

    mockMvc
        .perform(
            put("/catalogs/groups/{id}", 11L).contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
        .andExpect(jsonPath("$.instance").value("/catalogs/groups/11"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Validation Failed"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.errors.description").value("description must not be blank"));

    verifyNoInteractions(service);
  }

  @Test
  void deleteCatalog_shouldReturn204_whenCatalogExists() throws Exception {

    mockMvc.perform(delete("/catalogs/groups/{id}", 20L)).andExpect(status().isNoContent());

    verify(service).deleteCatalog("groups", 20L);
  }
}
