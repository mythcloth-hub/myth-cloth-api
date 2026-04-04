package com.mesofi.mythclothapi.anniversaries;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

class AnniversaryNotFoundExceptionTest {

  @Test
  void constructor_shouldSetIdCorrectly_whenCreatedWithGivenId() {
    // Arrange
    Long id = 42L;

    // Act
    AnniversaryNotFoundException exception = new AnniversaryNotFoundException(id);

    // Assert
    assertThat(exception.getId()).isEqualTo(id);
  }

  @Test
  void constructor_shouldSetMessageCorrectly_whenCreated() {
    // Arrange & Act
    AnniversaryNotFoundException exception = new AnniversaryNotFoundException(1L);

    // Assert
    assertThat(exception.getMessage()).isEqualTo("Anniversary not found");
  }

  @Test
  void constructor_shouldSetCauseDetailCorrectly_whenCreated() {
    // Arrange & Act
    AnniversaryNotFoundException exception = new AnniversaryNotFoundException(1L);

    // Assert
    assertThat(exception.getCauseDetail()).isEqualTo("Anniversary not found");
  }

  @Test
  void getStatus_shouldReturnNotFound_whenCalled() {
    // Arrange
    AnniversaryNotFoundException exception = new AnniversaryNotFoundException(1L);

    // Act
    HttpStatus status = exception.getStatus();

    // Assert
    assertThat(status).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void exception_shouldBeInstanceOfApiException_whenCreated() {
    // Arrange & Act
    AnniversaryNotFoundException exception = new AnniversaryNotFoundException(1L);

    // Assert
    assertThat(exception).isInstanceOf(ApiException.class);
  }

  @Test
  void exception_shouldBeInstanceOfRuntimeException_whenCreated() {
    // Arrange & Act
    AnniversaryNotFoundException exception = new AnniversaryNotFoundException(1L);

    // Assert
    assertThat(exception).isInstanceOf(RuntimeException.class);
  }

  @Test
  void constructor_shouldHandleNullId_whenCreatedWithNullId() {
    // Arrange & Act
    AnniversaryNotFoundException exception = new AnniversaryNotFoundException(null);

    // Assert
    assertThat(exception.getId()).isNull();
  }
}
