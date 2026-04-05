package com.mesofi.mythclothapi.figurineevents;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.mesofi.mythclothapi.config.MapperTestConfig;
import com.mesofi.mythclothapi.config.MethodValidationTestConfig;
import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.figurineevents.dto.FigurineEventReq;
import com.mesofi.mythclothapi.figurineevents.dto.FigurineEventResp;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEvent;
import com.mesofi.mythclothapi.figurines.FigurineRepository;
import com.mesofi.mythclothapi.figurines.exceptions.FigurineNotFoundException;
import com.mesofi.mythclothapi.figurines.model.Figurine;

@Disabled
@SpringBootTest(
    classes = {
      FigurineEventService.class,
      MapperTestConfig.class,
      MethodValidationTestConfig.class
    })
public class FigurineEventServiceTest {

  @Autowired private FigurineEventService figurineEventService;

  @MockitoBean private FigurineRepository figurineRepository;
  @MockitoBean private FigurineEventRepository figurineEventRepository;

  @Test
  void createFigurineEvent_shouldThrowException_whenRequestIsNull() {
    assertThatThrownBy(() -> figurineEventService.createFigurineEvent(null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("createFigurineEvent.request")
        .hasMessageContaining("must not be null");
  }

  @Test
  void createFigurineEvent_shouldThrowException_whenFigurineIsNotFound() {
    // Arrange
    FigurineEventReq request = createRequest(40L, "Event details", LocalDate.of(2025, 1, 1));
    when(figurineRepository.findById(40L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> figurineEventService.createFigurineEvent(request))
        .isInstanceOf(FigurineNotFoundException.class)
        .extracting(ex -> ((FigurineNotFoundException) ex).getId())
        .isEqualTo(40L);

    verify(figurineRepository).findById(40L);
  }

  @Test
  void createFigurineEvent_shouldPersistAndReturnResponse_whenRequestIsValid() {
    // Arrange
    Figurine figurine = createFigurine(7L);
    FigurineEventReq request = createRequest(7L, "Tamashii event", LocalDate.of(2025, 1, 1));

    when(figurineRepository.findById(7L)).thenReturn(Optional.of(figurine));
    when(figurineEventRepository.save(any(FigurineEvent.class)))
        .thenAnswer(
            invocation -> {
              FigurineEvent event = invocation.getArgument(0);
              event.setId(99L);
              return event;
            });

    // Act
    FigurineEventResp response = figurineEventService.createFigurineEvent(request);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(99L);
    assertThat(response.description()).isEqualTo("Tamashii event");
    // assertThat(response.figurine()).isNotNull();
    // assertThat(response.figurine().id()).isEqualTo(7L);

    ArgumentCaptor<FigurineEvent> eventCaptor = ArgumentCaptor.forClass(FigurineEvent.class);
    verify(figurineEventRepository).save(eventCaptor.capture());

    FigurineEvent savedEvent = eventCaptor.getValue();
    assertThat(savedEvent.getFigurine()).isSameAs(figurine);
    assertThat(savedEvent.getDescription()).isEqualTo("Tamashii event");
    assertThat(savedEvent.getEventDate()).isEqualTo(LocalDate.of(2025, 1, 1));
  }

  @Test
  void retrieveFigurineEvent_shouldThrowException_whenEventIsMissing() {
    // Arrange
    when(figurineEventRepository.findByIdAndFigurineId(60L, 20L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> figurineEventService.retrieveFigurineEvent(20L, 60L))
        .isInstanceOf(FigurineEventNotFoundException.class)
        .extracting(ex -> ((FigurineEventNotFoundException) ex).getId())
        .isEqualTo(60L);
  }

  @Test
  void retrieveFigurineEvent_shouldReturnResponse_whenEventExists() {
    // Arrange
    FigurineEvent event =
        createEvent(18L, LocalDate.of(2024, 1, 1), "Found event", createFigurine(11L));
    when(figurineEventRepository.findByIdAndFigurineId(18L, 11L)).thenReturn(Optional.of(event));

    // Act
    FigurineEventResp response = figurineEventService.retrieveFigurineEvent(11L, 18L);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(18L);
    assertThat(response.description()).isEqualTo("Found event");
    assertThat(response.region()).isEqualTo(CountryCode.JP);
  }

  @Test
  void retrieveFigurineEvents_shouldThrowException_whenEventsListIsEmpty() {
    // Arrange
    when(figurineEventRepository.findAllByFigurineId(33L)).thenReturn(List.of());

    // Act + Assert
    assertThatThrownBy(() -> figurineEventService.retrieveFigurineEvents(33L))
        .isInstanceOf(FigurineNotFoundException.class)
        .extracting(ex -> ((FigurineNotFoundException) ex).getId())
        .isEqualTo(33L);
  }

  @Test
  void retrieveFigurineEvents_shouldReturnResponses_whenEventsExist() {
    // Arrange
    Figurine figurine = createFigurine(12L);
    FigurineEvent event1 = createEvent(1L, LocalDate.of(2024, 1, 1), "Announcement", figurine);
    FigurineEvent event2 = createEvent(2L, LocalDate.of(2024, 2, 2), "Release", figurine);
    when(figurineEventRepository.findAllByFigurineId(12L)).thenReturn(List.of(event1, event2));

    // Act
    List<FigurineEventResp> response = figurineEventService.retrieveFigurineEvents(12L);

    // Assert
    assertThat(response.size()).isEqualTo(2);
    assertThat(response.getFirst().description()).isEqualTo("Announcement");
    assertThat(response.get(1).description()).isEqualTo("Release");
  }

  @Test
  void updateFigurineEvent_shouldThrowException_whenEventIsMissing() {
    // Arrange
    FigurineEventReq request = createRequest(10L, "Updated", LocalDate.of(2025, 1, 1));
    when(figurineEventRepository.findByIdAndFigurineId(50L, 10L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> figurineEventService.updateFigurineEvent(10L, 50L, request))
        .isInstanceOf(FigurineEventNotFoundException.class)
        .extracting(ex -> ((FigurineEventNotFoundException) ex).getId())
        .isEqualTo(50L);
  }

  @Test
  void updateFigurineEvent_shouldThrowException_whenNewFigurineIsMissing() {
    // Arrange
    FigurineEvent existing =
        createEvent(6L, LocalDate.of(2024, 1, 1), "Initial", createFigurine(10L));
    FigurineEventReq request = createRequest(30L, "Updated", LocalDate.of(2025, 1, 1));

    when(figurineEventRepository.findByIdAndFigurineId(6L, 10L)).thenReturn(Optional.of(existing));
    when(figurineRepository.findById(30L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> figurineEventService.updateFigurineEvent(10L, 6L, request))
        .isInstanceOf(FigurineNotFoundException.class)
        .extracting(ex -> ((FigurineNotFoundException) ex).getId())
        .isEqualTo(30L);
  }

  @Test
  void updateFigurineEvent_shouldUpdateDetailsWithoutReassigning_whenFigurineIdIsTheSame() {
    // Arrange
    Figurine sameFigurine = createFigurine(10L);
    FigurineEvent existing = createEvent(6L, LocalDate.of(2024, 1, 1), "Initial", sameFigurine);
    FigurineEventReq request = createRequest(10L, "Updated description", LocalDate.of(2025, 1, 1));

    when(figurineEventRepository.findByIdAndFigurineId(6L, 10L)).thenReturn(Optional.of(existing));
    when(figurineRepository.findById(10L)).thenReturn(Optional.of(sameFigurine));
    when(figurineEventRepository.save(any(FigurineEvent.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    FigurineEventResp response = figurineEventService.updateFigurineEvent(10L, 6L, request);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.description()).isEqualTo("Updated description");

    ArgumentCaptor<FigurineEvent> eventCaptor = ArgumentCaptor.forClass(FigurineEvent.class);
    verify(figurineEventRepository).save(eventCaptor.capture());

    FigurineEvent updatedEvent = eventCaptor.getValue();
    assertThat(updatedEvent.getEventDate()).isEqualTo(LocalDate.of(2025, 1, 1));
    assertThat(updatedEvent.getDescription()).isEqualTo("Updated description");
    assertThat(updatedEvent.getFigurine()).isSameAs(sameFigurine);
  }

  @Test
  void updateFigurineEvent_shouldReassignFigurine_whenFigurineIdChanges() {
    // Arrange
    Figurine originalFigurine = createFigurine(10L);
    Figurine newFigurine = createFigurine(44L);
    FigurineEvent existing = createEvent(6L, LocalDate.of(2024, 1, 1), "Initial", originalFigurine);
    FigurineEventReq request = createRequest(44L, "Moved event", LocalDate.of(2025, 1, 1));

    when(figurineEventRepository.findByIdAndFigurineId(6L, 10L)).thenReturn(Optional.of(existing));
    when(figurineRepository.findById(44L)).thenReturn(Optional.of(newFigurine));
    when(figurineEventRepository.save(any(FigurineEvent.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    FigurineEventResp response = figurineEventService.updateFigurineEvent(10L, 6L, request);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.description()).isEqualTo("Moved event");
    // assertThat(response.figurine().id()).isEqualTo(44L);

    ArgumentCaptor<FigurineEvent> eventCaptor = ArgumentCaptor.forClass(FigurineEvent.class);
    verify(figurineEventRepository).save(eventCaptor.capture());
    assertThat(eventCaptor.getValue().getFigurine()).isSameAs(newFigurine);
  }

  @Test
  void removeFigurineEvent_shouldThrowException_whenEventIsMissing() {
    // Arrange
    when(figurineEventRepository.findByIdAndFigurineId(90L, 19L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> figurineEventService.removeFigurineEvent(19L, 90L))
        .isInstanceOf(FigurineEventNotFoundException.class)
        .extracting(ex -> ((FigurineEventNotFoundException) ex).getId())
        .isEqualTo(90L);

    verify(figurineEventRepository, never()).delete(any(FigurineEvent.class));
  }

  @Test
  void removeFigurineEvent_shouldDeleteEvent_whenEventExists() {
    // Arrange
    FigurineEvent existing =
        createEvent(90L, LocalDate.of(2024, 1, 1), "Remove me", createFigurine(19L));
    when(figurineEventRepository.findByIdAndFigurineId(90L, 19L)).thenReturn(Optional.of(existing));

    // Act
    figurineEventService.removeFigurineEvent(19L, 90L);

    // Assert
    verify(figurineEventRepository).delete(existing);
  }

  private FigurineEventReq createRequest(Long figurineId, String description, LocalDate eventDate) {
    FigurineEventReq request = new FigurineEventReq();
    request.setFigurineId(figurineId);
    request.setDescription(description);
    request.setDate(eventDate);
    return request;
  }

  private Figurine createFigurine(Long id) {
    Figurine figurine = new Figurine();
    figurine.setId(id);
    return figurine;
  }

  private FigurineEvent createEvent(
      Long id, LocalDate date, String description, Figurine figurine) {
    FigurineEvent event = new FigurineEvent();
    event.setId(id);
    event.setEventDate(date);
    event.setDescription(description);
    event.setFigurine(figurine);
    event.setRegion(CountryCode.JP);
    return event;
  }
}
