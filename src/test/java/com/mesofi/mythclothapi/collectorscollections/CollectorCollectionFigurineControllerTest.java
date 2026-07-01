package com.mesofi.mythclothapi.collectorscollections;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mesofi.mythclothapi.collectors.exceptions.CollectorNotFoundException;
import com.mesofi.mythclothapi.collectorscollections.dto.AssignFigurinesReq;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectionAssignmentMode;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionFigurineDetailResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionFigurineResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionReq;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionResp;
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectionAlreadyExistsException;
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectionNotFoundException;
import com.mesofi.mythclothapi.security.config.SecurityConfig;

@WebMvcTest(CollectorCollectionFigurineController.class)
@Import(SecurityConfig.class)
class CollectorCollectionFigurineControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CollectorCollectionFigurineService service;

  @MockitoBean private JwtDecoder jwtDecoder;

  @Test
  void addFigurineToCollection_shouldReturnMethodNotAllowed_whenRequestMethodIsInvalid()
      throws Exception {
    mockMvc
        .perform(
            post("/collections")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("collections:figurines:add"))))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.detail").value("Request method 'POST' is not supported"))
        .andExpect(jsonPath("$.instance").value("/collections"))
        .andExpect(jsonPath("$.status").value("405"))
        .andExpect(jsonPath("$.title").value("Method Not Allowed"))
        .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(service);
  }

  @Disabled
  @ParameterizedTest
  @ValueSource(strings = {"/collections/2", "/collections/2/figurines"})
  void addFigurineToCollection_shouldReturn404_whenPostingToInvalidEndpoint(String invalidEndpoint)
      throws Exception {
    mockMvc
        .perform(
            post(invalidEndpoint)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("collections:figurines:add"))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("The URL you are calling does not exist."))
        .andExpect(jsonPath("$.instance").value(invalidEndpoint))
        .andExpect(jsonPath("$.status").value("404"))
        .andExpect(jsonPath("$.title").value("Endpoint not found"))
        .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(service);
  }

  @Test
  void addFigurineToCollection_shouldReturnUnauthorized_whenJwtTokenIsMissing() throws Exception {
    mockMvc.perform(post("/collections/2/figurines/0")).andExpect(status().isUnauthorized());
    verifyNoInteractions(service);
  }

  @Test
  void addFigurineToCollection_shouldReturnNoContent_whenValidJwtIsProvided() throws Exception {
    mockMvc
        .perform(
            post("/collections/2/figurines/9")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("collections:figurines:add"))))
        .andExpect(status().isNoContent());

    AssignFigurinesReq req =
        new AssignFigurinesReq(List.of(9L), CollectionAssignmentMode.AUTO, List.of(2L), null);

    verify(service).assignFigurinesToCollections(123L, req);
  }

  @Test
  void assignFigurinesToCollections_shouldReturnUnauthorized_whenJwtTokenIsMissing()
      throws Exception {
    mockMvc.perform(post("/collections/assign-figurines")).andExpect(status().isUnauthorized());
    verifyNoInteractions(service);
  }

  @Test
  void assignFigurinesToCollections_shouldReturnBadRequest_whenBodyIsMissing() throws Exception {
    mockMvc
        .perform(
            post("/collections/assign-figurines")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("collections:figurines:add"))))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.detail")
                .value(
                    "Required request body is missing: public org.springframework.http.ResponseEntity<java.lang.Void> com.mesofi.mythclothapi.collectorscollections.CollectorCollectionFigurineController.assignFigurinesToCollections(org.springframework.security.oauth2.jwt.Jwt,com.mesofi.mythclothapi.collectorscollections.dto.AssignFigurinesReq)"))
        .andExpect(jsonPath("$.instance").value("/collections/assign-figurines"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Invalid body"))
        .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(service);
  }

  @Test
  void assignFigurinesToCollections_shouldReturn415_whenContentTypeIsMissing() throws Exception {
    mockMvc
        .perform(
            post("/collections/assign-figurines")
                .content("{}")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("collections:figurines:add"))))
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(
            jsonPath("$.detail").value("Content-Type 'application/octet-stream' is not supported"))
        .andExpect(jsonPath("$.instance").value("/collections/assign-figurines"))
        .andExpect(jsonPath("$.status").value("415"))
        .andExpect(jsonPath("$.title").value("Unsupported Media Type"))
        .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(service);
  }

  @Test
  void assignFigurinesToCollections_shouldReturnBadRequest_whenBodyParametersAreMissing()
      throws Exception {
    String requestBody = "{}";

    mockMvc
        .perform(
            post("/collections/assign-figurines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("collections:figurines:add"))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
        .andExpect(jsonPath("$.instance").value("/collections/assign-figurines"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Validation Failed"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.errors.collectionMode").value("must not be null"))
        .andExpect(jsonPath("$.errors.figurineIds").value("must not be empty"));

    verifyNoInteractions(service);
  }

  @Test
  void assignFigurinesToCollections_shouldReturnBadRequest_whenCollectionModeValueIsInvalid()
      throws Exception {
    String requestBody = "{\"collectionMode\":\"test\"}";

    mockMvc
        .perform(
            post("/collections/assign-figurines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("collections:figurines:add"))))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.detail")
                .value(
                    "JSON parse error: Cannot deserialize value of type `com.mesofi.mythclothapi.collectorscollections.dto.CollectionAssignmentMode` from String \"test\": not one of the values accepted for Enum class: [EXISTING, CREATE, AUTO]"))
        .andExpect(jsonPath("$.instance").value("/collections/assign-figurines"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Invalid body"))
        .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(service);
  }

  @Test
  void assignFigurinesToCollections_shouldReturnBadRequest_whenFigurineIdsAreEmpty()
      throws Exception {
    String requestBody = "{\"collectionMode\":\"EXISTING\"}";

    mockMvc
        .perform(
            post("/collections/assign-figurines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("collections:figurines:add"))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
        .andExpect(jsonPath("$.instance").value("/collections/assign-figurines"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Validation Failed"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.errors.figurineIds").value("must not be empty"));

    verifyNoInteractions(service);
  }

  @Test
  void assignFigurinesToCollections_shouldReturnNotFound_whenCollectorDoesNotExist()
      throws Exception {
    AssignFigurinesReq req =
        new AssignFigurinesReq(
            List.of(3L),
            CollectionAssignmentMode.CREATE,
            null,
            new CollectorCollectionReq("test", "test desc"));

    doThrow(new CollectorNotFoundException(123L))
        .when(service)
        .assignFigurinesToCollections(123L, req);

    String requestBody =
        "{\"collectionMode\":\"CREATE\", \"figurineIds\": [3], \"collection\": {\"name\": \"test\", \"description\": \"test desc\"}}";

    mockMvc
        .perform(
            post("/collections/assign-figurines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("collections:figurines:add"))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("Collector with id 123 was not found"))
        .andExpect(jsonPath("$.instance").value("/collections/assign-figurines"))
        .andExpect(jsonPath("$.status").value("404"))
        .andExpect(jsonPath("$.title").value("Collector with id 123 was not found"))
        .andExpect(jsonPath("$.timestamp").exists());

    verify(service).assignFigurinesToCollections(123L, req);
  }

  @Test
  void assignFigurinesToCollections_shouldReturnConflict_whenCollectionAlreadyExists()
      throws Exception {
    AssignFigurinesReq req =
        new AssignFigurinesReq(
            List.of(3L),
            CollectionAssignmentMode.CREATE,
            null,
            new CollectorCollectionReq("my collection", "test desc"));

    doThrow(new CollectionAlreadyExistsException("my collection"))
        .when(service)
        .assignFigurinesToCollections(123L, req);

    String requestBody =
        "{\"collectionMode\":\"CREATE\", \"figurineIds\": [3], \"collection\": {\"name\": \"my collection\", \"description\": \"test desc\"}}";

    mockMvc
        .perform(
            post("/collections/assign-figurines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("collections:figurines:add"))))
        .andExpect(status().isConflict())
        .andExpect(
            jsonPath("$.detail").value("Collection with name 'my collection' already exists"))
        .andExpect(jsonPath("$.instance").value("/collections/assign-figurines"))
        .andExpect(jsonPath("$.status").value("409"))
        .andExpect(jsonPath("$.title").value("Collection with name 'my collection' already exists"))
        .andExpect(jsonPath("$.timestamp").exists());

    verify(service).assignFigurinesToCollections(123L, req);
  }

  @Test
  void assignFigurinesToCollections_shouldReturnNotFound_whenTargetCollectionDoesNotExist()
      throws Exception {
    AssignFigurinesReq req =
        new AssignFigurinesReq(List.of(3L), CollectionAssignmentMode.EXISTING, List.of(2L), null);

    doThrow(new CollectionNotFoundException(2L))
        .when(service)
        .assignFigurinesToCollections(123L, req);

    String requestBody =
        "{\"collectionMode\":\"EXISTING\", \"figurineIds\": [3], \"collectionIds\": [2]}";

    mockMvc
        .perform(
            post("/collections/assign-figurines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("collections:figurines:add"))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("Collection with id 2 was not found"))
        .andExpect(jsonPath("$.instance").value("/collections/assign-figurines"))
        .andExpect(jsonPath("$.status").value("404"))
        .andExpect(jsonPath("$.title").value("Collection with id 2 was not found"))
        .andExpect(jsonPath("$.timestamp").exists());

    verify(service).assignFigurinesToCollections(123L, req);
  }

  @Test
  void assignFigurinesToCollections_shouldAssignFigurines_whenRequestIsValid() throws Exception {
    String requestBody = "{\"collectionMode\":\"EXISTING\", \"figurineIds\": [3]}";

    mockMvc
        .perform(
            post("/collections/assign-figurines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("collections:figurines:add"))))
        .andExpect(status().isNoContent());

    AssignFigurinesReq req =
        new AssignFigurinesReq(List.of(3L), CollectionAssignmentMode.EXISTING, null, null);

    verify(service).assignFigurinesToCollections(123L, req);
  }

  @Test
  void retrieveCollections_shouldReturnNotFound_whenCollectorDoesNotExist() throws Exception {

    when(service.retrieveCollections(123L)).thenThrow(new CollectorNotFoundException(123L));

    mockMvc
        .perform(
            get("/collections")
                .contentType(MediaType.APPLICATION_JSON)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("collections:read"))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("Collector with id 123 was not found"))
        .andExpect(jsonPath("$.instance").value("/collections"))
        .andExpect(jsonPath("$.status").value("404"))
        .andExpect(jsonPath("$.title").value("Collector with id 123 was not found"))
        .andExpect(jsonPath("$.timestamp").exists());

    verify(service).retrieveCollections(123L);
  }

  @Test
  void retrieveCollections_shouldReturnCollectorCollections_whenRequestIsAuthenticated()
      throws Exception {

    CollectorCollectionResp resp =
        new CollectorCollectionResp(1L, "test", "test desc", 1, List.of(1L));
    when(service.retrieveCollections(123L)).thenReturn(List.of(resp));

    mockMvc
        .perform(
            get("/collections")
                .contentType(MediaType.APPLICATION_JSON)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("collections:read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].name").value("test"))
        .andExpect(jsonPath("$[0].description").value("test desc"))
        .andExpect(jsonPath("$[0].totalFigurines").value(1))
        .andExpect(jsonPath("$[0].figurineIds[0]").value(1));

    verify(service).retrieveCollections(123L);
  }

  @Test
  void retrieveCollectionFigurines_shouldReturnFigurines_whenRequestIsAuthenticated()
      throws Exception {
    CollectorCollectionFigurineResp resp =
        new CollectorCollectionFigurineResp(9L, "Seiya", null, null, null, true, 2, 1991);
    when(service.retrieveCollectionFigurines(123L, 2L)).thenReturn(List.of(resp));

    mockMvc
        .perform(
            get("/collections/2/figurines")
                .contentType(MediaType.APPLICATION_JSON)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("collections:figurines:read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(9))
        .andExpect(jsonPath("$[0].name").value("Seiya"))
        .andExpect(jsonPath("$[0].isCollected").value(true))
        .andExpect(jsonPath("$[0].ownedQuantity").value(2))
        .andExpect(jsonPath("$[0].year").value(1991));

    verify(service).retrieveCollectionFigurines(123L, 2L);
  }

  @Test
  void retrieveCollectionFigurine_shouldReturnFigurine_whenRequestIsAuthenticated()
      throws Exception {
    CollectorCollectionFigurineDetailResp resp = new CollectorCollectionFigurineDetailResp("Seiya");
    when(service.retrieveCollectionFigurine(123L, 2L, 9L)).thenReturn(resp);

    mockMvc
        .perform(
            get("/collections/2/figurines/9")
                .contentType(MediaType.APPLICATION_JSON)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("collections:figurines:read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.displayableName").value("Seiya"));

    verify(service).retrieveCollectionFigurine(123L, 2L, 9L);
  }

  @Test
  void deleteCollection_shouldReturnNoContent_whenRequestIsAuthenticated() throws Exception {
    mockMvc
        .perform(
            delete("/collections/2")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("collections:delete"))))
        .andExpect(status().isNoContent());

    verify(service).deleteCollection(123L, 2L);
  }

  @Test
  void updateCollection_shouldReturnUpdatedCollection_whenRequestIsAuthenticated()
      throws Exception {
    CollectorCollectionResp resp =
        new CollectorCollectionResp(2L, "Updated", "Updated desc", 0, List.of());
    when(service.updateCollection(123L, 2L, new CollectorCollectionReq("Updated", "Updated desc")))
        .thenReturn(resp);

    mockMvc
        .perform(
            put("/collections/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Updated\",\"description\":\"Updated desc\"}")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("collections:update"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(2))
        .andExpect(jsonPath("$.name").value("Updated"))
        .andExpect(jsonPath("$.description").value("Updated desc"));

    verify(service)
        .updateCollection(123L, 2L, new CollectorCollectionReq("Updated", "Updated desc"));
  }
}
