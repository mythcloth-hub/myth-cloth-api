package com.mesofi.mythclothapi.figurineevents;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.figurineevents.dto.FigurineEventReq;
import com.mesofi.mythclothapi.figurineevents.dto.FigurineEventResp;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEventType;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(FigurineEventController.class)
class FigurineEventControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private FigurineEventService service;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void createEvent_shouldReturn400_whenRequestBodyIsMissing() throws Exception {

    mockMvc
        .perform(post("/figurines/{figurineId}/events", 1L))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.detail")
                .value(
                    "Required request body is missing: public org.springframework.http.ResponseEntity<com.mesofi.mythclothapi.figurineevents.dto.FigurineEventResp> com.mesofi.mythclothapi.figurineevents.FigurineEventController.createEvent(java.lang.Long,com.mesofi.mythclothapi.figurineevents.dto.FigurineEventReq)"))
        .andExpect(jsonPath("$.instance").value("/figurines/1/events"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Invalid body"))
        .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(service);
  }

  @Test
  void createEvent_shouldReturn415_whenContentTypeIsMissing() throws Exception {

    mockMvc
        .perform(post("/figurines/{figurineId}/events", 1L).content("{}"))
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(
            jsonPath("$.detail").value("Content-Type 'application/octet-stream' is not supported"))
        .andExpect(jsonPath("$.instance").value("/figurines/1/events"))
        .andExpect(jsonPath("$.status").value("415"))
        .andExpect(jsonPath("$.title").value("Unsupported Media Type"))
        .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(service);
  }

  @Test
  void createEvent_shouldReturn400_whenRequestBodyFailsValidation() throws Exception {

    mockMvc
        .perform(
            post("/figurines/{figurineId}/events", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
        .andExpect(jsonPath("$.instance").value("/figurines/1/events"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Validation Failed"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.errors.description").value("description must not be blank"))
        .andExpect(jsonPath("$.errors.date").value("event date must be provided"))
        .andExpect(jsonPath("$.errors.type").value("must not be null"))
        .andExpect(jsonPath("$.errors.region").value("must not be null"))
        .andExpect(jsonPath("$.errors.figurineId").value("must not be null"));

    verifyNoInteractions(service);
  }

  @Test
  void createEvent_shouldReturn201AndDelegateUsingPathFigurineId() throws Exception {
    FigurineEventReq request = createEventRequest();
    request.setFigurineId(999L);
    request.setType(FigurineEventType.ANNOUNCEMENT);
    FigurineEventResp response = createEventResponse(77L);

    when(service.createFigurineEvent(any())).thenReturn(response);

    mockMvc
        .perform(
            post("/figurines/{figurineId}/events", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", endsWith("/events/77")))
        .andExpect(jsonPath("$.id").value(77L))
        .andExpect(jsonPath("$.description").value("Pre-order opened"))
        .andExpect(jsonPath("$.date").value("2020-01-01"));

    verify(service)
        .createFigurineEvent(
            argThat(
                payload ->
                    payload.getFigurineId() == 1L
                        && "Pre-order opened".equals(payload.getDescription())
                        && LocalDate.of(2020, 1, 1).equals(payload.getDate())));
  }

  @Test
  void retrieveEvent_shouldReturn200_whenEventExists() throws Exception {
    FigurineEventResp response = createEventResponse(15L);

    when(service.retrieveFigurineEvent(1L, 15L)).thenReturn(response);

    mockMvc
        .perform(get("/figurines/{figurineId}/events/{id}", 1L, 15L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(15L))
        .andExpect(jsonPath("$.date").value("2020-01-01"))
        .andExpect(jsonPath("$.type").value("PREORDER_OPEN"))
        .andExpect(jsonPath("$.region").value("JP"))
        .andExpect(jsonPath("$.description").value("Pre-order opened"));

    verify(service).retrieveFigurineEvent(1L, 15L);
  }

  @Test
  void retrieveEvents_shouldReturnList_whenEventsExist() throws Exception {
    when(service.retrieveFigurineEvents(1L))
        .thenReturn(List.of(createEventResponse(10L), createEventResponse(11L)));

    mockMvc
        .perform(get("/figurines/{figurineId}/events", 1L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(10L))
        .andExpect(jsonPath("$[0].date").value("2020-01-01"))
        .andExpect(jsonPath("$[0].type").value("PREORDER_OPEN"))
        .andExpect(jsonPath("$[0].region").value("JP"))
        .andExpect(jsonPath("$[0].description").value("Pre-order opened"))
        .andExpect(jsonPath("$[1].id").value(11L))
        .andExpect(jsonPath("$[1].date").value("2020-01-01"))
        .andExpect(jsonPath("$[1].type").value("PREORDER_OPEN"))
        .andExpect(jsonPath("$[1].region").value("JP"))
        .andExpect(jsonPath("$[1].description").value("Pre-order opened"));

    verify(service).retrieveFigurineEvents(1L);
  }

  @Test
  void retrieveEvents_shouldReturnBadRequest_whenFigurineIdIsNotPositive() {
    assertThatThrownBy(() -> mockMvc.perform(get("/figurines/{figurineId}/events", 0L)))
        .hasRootCauseInstanceOf(jakarta.validation.ConstraintViolationException.class);

    verifyNoInteractions(service);
  }

  @Test
  void updateEvent_shouldReturn200_whenRequestIsValid() throws Exception {
    FigurineEventReq request = createEventRequest();
    FigurineEventResp response = createEventResponse(20L);

    when(service.updateFigurineEvent(any(), any(), any())).thenReturn(response);

    mockMvc
        .perform(
            put("/figurines/{figurineId}/events/{id}", 1L, 20L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(20L));

    verify(service)
        .updateFigurineEvent(
            argThat(figurineId -> figurineId == 1L),
            argThat(eventId -> eventId == 20L),
            argThat(
                payload ->
                    payload.getFigurineId() == 1L
                        && "Pre-order opened".equals(payload.getDescription())
                        && LocalDate.of(2020, 1, 1).equals(payload.getDate())));
  }

  @Test
  void removeEvent_shouldReturn204_whenRequestIsValid() throws Exception {
    mockMvc
        .perform(delete("/figurines/{figurineId}/events/{id}", 1L, 5L))
        .andExpect(status().isNoContent());

    verify(service).removeFigurineEvent(1L, 5L);
  }

  private FigurineEventReq createEventRequest() {
    FigurineEventReq request = new FigurineEventReq();
    request.setDescription("Pre-order opened");
    request.setDate(LocalDate.of(2020, 1, 1));
    request.setRegion(CountryCode.JP);
    request.setType(FigurineEventType.ANNOUNCEMENT);
    request.setFigurineId(1L);
    return request;
  }

  private FigurineEventResp createEventResponse(long id) {
    return new FigurineEventResp(
        id,
        LocalDate.of(2020, 1, 1),
        FigurineEventType.PREORDER_OPEN,
        CountryCode.JP,
        "Pre-order opened");
  }
}
