package com.mesofi.mythclothapi.collectors.exceptions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

class CollectorNotFoundExceptionTest {

  @Test
  void constructor_shouldSetIdCorrectly_whenCreatedWithGivenId() {
    // Arrange
    Long id = 42L;

    // Act
    CollectorNotFoundException exception = new CollectorNotFoundException(id);

    // Assert
    assertThat(exception.getId()).isEqualTo(id);
  }

  @Test
  void constructor_shouldSetMessageCorrectly_whenCreated() {
    // Arrange & Act
    CollectorNotFoundException exception = new CollectorNotFoundException(1L);

    // Assert
    assertThat(exception.getMessage()).isEqualTo("Collector with id 1 was not found");
  }

  @Test
  void constructor_shouldSetCauseDetailCorrectly_whenCreated() {
    // Arrange & Act
    CollectorNotFoundException exception = new CollectorNotFoundException(1L);

    // Assert
    assertThat(exception.getCauseDetail()).isEqualTo("Collector with id 1 was not found");
  }

  @Test
  void getStatus_shouldReturnNotFound_whenCalled() {
    // Arrange
    CollectorNotFoundException exception = new CollectorNotFoundException(1L);

    // Act
    HttpStatus status = exception.getStatus();

    // Assert
    assertThat(status).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void exception_shouldBeInstanceOfApiException_whenCreated() {
    // Arrange & Act
    CollectorNotFoundException exception = new CollectorNotFoundException(1L);

    // Assert
    assertThat(exception).isInstanceOf(ApiException.class);
  }

  @Test
  void exception_shouldBeInstanceOfRuntimeException_whenCreated() {
    // Arrange & Act
    CollectorNotFoundException exception = new CollectorNotFoundException(1L);

    // Assert
    assertThat(exception).isInstanceOf(RuntimeException.class);
  }

  @Test
  void constructor_shouldHandleNullId_whenCreatedWithNullId() {
    // Arrange & Act
    CollectorNotFoundException exception = new CollectorNotFoundException(null);

    // Assert
    assertThat(exception.getId()).isNull();
    assertThat(exception.getMessage()).isEqualTo("Collector with id null was not found");
    assertThat(exception.getCauseDetail()).isEqualTo("Collector with id null was not found");
  }
}
