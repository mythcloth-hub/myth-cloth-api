package com.mesofi.mythclothapi.figurineevents;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.mesofi.mythclothapi.figurineevents.dto.FigurineEventReq;
import com.mesofi.mythclothapi.figurineevents.dto.FigurineEventResp;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEvent;
import com.mesofi.mythclothapi.figurines.FigurineRepository;
import com.mesofi.mythclothapi.figurines.exceptions.FigurineNotFoundException;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.utils.MethodValidationTestConfig;

import jakarta.validation.ConstraintViolationException;

@SpringBootTest(
    classes = {
      FigurineEventService.class,
      MethodValidationTestConfig.class,
      FigurineEventMapperImpl.class
    })
public class FigurineEventServiceTest {

  @MockitoBean private FigurineEventRepository repository;
  @MockitoBean private FigurineRepository figurineRepository;
  @Autowired private FigurineEventMapper mapper;

  @Autowired private FigurineEventService service;

  @Test
  void createFigurineEvent_shouldThrowException_whenEventIsNull() {
    // Act + Assert
    assertThatThrownBy(() -> service.createFigurineEvent(null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("createFigurineEvent.request")
        .hasMessageContaining("must not be null");
  }

  @Test
  void createFigurineEvent_shouldThrowException_whenAllFieldsAreNull() {
    // Arrange
    FigurineEventReq req = new FigurineEventReq();

    // Act + Assert
    assertThatThrownBy(() -> service.createFigurineEvent(req))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("createFigurineEvent.request.eventDate")
        .hasMessageContaining("event date must be provided")
        .hasMessageContaining("createFigurineEvent.request.description")
        .hasMessageContaining("description must not be blank")
        .hasMessageContaining("createFigurineEvent.request.figurineId")
        .hasMessageContaining("must not be null");
  }

  @Test
  void createFigurineEvent_shouldThrowException_whenEventDateAndFigurineIdAreNull() {
    // Arrange
    FigurineEventReq req = new FigurineEventReq();
    req.setDescription("The description");

    // Act + Assert
    assertThatThrownBy(() -> service.createFigurineEvent(req))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("createFigurineEvent.request.eventDate")
        .hasMessageContaining("event date must be provided")
        .hasMessageContaining("createFigurineEvent.request.figurineId")
        .hasMessageContaining("must not be null");
  }

  @Test
  void createFigurineEvent_shouldThrowException_whenEventDateIsFutureAndFigurineIdIsNull() {
    // Arrange
    FigurineEventReq req = new FigurineEventReq();
    req.setDescription("The description");
    req.setEventDate(LocalDate.of(2050, 2, 3));

    // Act + Assert
    assertThatThrownBy(() -> service.createFigurineEvent(req))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("createFigurineEvent.request.eventDate")
        .hasMessageContaining("must be a past date")
        .hasMessageContaining("createFigurineEvent.request.figurineId")
        .hasMessageContaining("must not be null");
  }

  @Test
  void createFigurineEvent_shouldThrowException_whenFigurineIdIsNull() {
    // Arrange
    FigurineEventReq req = new FigurineEventReq();
    req.setDescription("The description");
    req.setEventDate(LocalDate.of(2021, 2, 3));

    // Act + Assert
    assertThatThrownBy(() -> service.createFigurineEvent(req))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("createFigurineEvent.request.figurineId")
        .hasMessageContaining("must not be null");
  }

  @Test
  void createFigurineEvent_shouldThrowException_whenFigurineIdIsNegative() {
    // Arrange
    FigurineEventReq req = new FigurineEventReq();
    req.setDescription("The description");
    req.setEventDate(LocalDate.of(2021, 2, 3));
    req.setFigurineId(-3L);

    // Act + Assert
    assertThatThrownBy(() -> service.createFigurineEvent(req))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("createFigurineEvent.request.figurineId")
        .hasMessageContaining("must be greater than 0");
  }

  @Test
  void createFigurineEvent_shouldThrowException_whenFigurineIdDoesNotExist() {
    // Arrange
    FigurineEventReq req = new FigurineEventReq();
    req.setDescription("The description");
    req.setEventDate(LocalDate.of(2021, 2, 3));
    req.setFigurineId(99L);
    when(figurineRepository.findById(99L)).thenReturn(Optional.empty());

    // Act
    assertThatThrownBy(() -> service.createFigurineEvent(req))
        .isInstanceOf(FigurineNotFoundException.class)
        .hasMessageContaining("Figurine not found");

    //  Assert
    verify(figurineRepository).findById(99L);
  }

  @Test
  void createFigurineEvent_shouldThrowException_whenFigurineIdDoesNotExist_() {
    // Arrange
    FigurineEventReq req = new FigurineEventReq();
    req.setDescription("The description");
    req.setEventDate(LocalDate.of(2021, 2, 3));
    req.setFigurineId(99L);

    FigurineEvent saved = new FigurineEvent();
    saved.setId(34L);
    saved.setDescription("The description");
    saved.setEventDate(LocalDate.of(2021, 2, 3));

    Figurine figurine = new Figurine();
    figurine.setId(99L);
    figurine.setNormalizedName("Seiya");

    when(figurineRepository.findById(99L)).thenReturn(Optional.of(figurine));
    when(repository.save(any(FigurineEvent.class))).thenReturn(saved);

    // Act
    FigurineEventResp figurineEventResp = service.createFigurineEvent(req);

    //  Assert
    assertThat(figurineEventResp)
        .isNotNull()
        .extracting(
            FigurineEventResp::id,
            FigurineEventResp::description,
            FigurineEventResp::eventDate,
            FigurineEventResp::figurine)
        .containsExactly(34L, "The description", LocalDate.of(2021, 2, 3), null);

    verify(figurineRepository).findById(99L);
    verify(repository).save(any(FigurineEvent.class));
  }
}
