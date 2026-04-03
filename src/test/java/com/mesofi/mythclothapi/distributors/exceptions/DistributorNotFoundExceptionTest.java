package com.mesofi.mythclothapi.distributors.exceptions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

class DistributorNotFoundExceptionTest {

  @Test
  void constructor_shouldSetIdCorrectly_whenCreatedWithGivenId() {
    // Arrange
    Long id = 42L;

    // Act
    DistributorNotFoundException exception = new DistributorNotFoundException(id);

    // Assert
    assertThat(exception.getId()).isEqualTo(id);
  }

  @Test
  void constructor_shouldSetMessageCorrectly_whenCreated() {
    // Arrange & Act
    DistributorNotFoundException exception = new DistributorNotFoundException(1L);

    // Assert
    assertThat(exception.getMessage()).isEqualTo("Distributor not found");
  }

  @Test
  void constructor_shouldSetCauseDetailCorrectly_whenCreated() {
    // Arrange & Act
    DistributorNotFoundException exception = new DistributorNotFoundException(1L);

    // Assert
    assertThat(exception.getCauseDetail()).isEqualTo("Distributor not found");
  }

  @Test
  void getStatus_shouldReturnNotFound_whenCalled() {
    // Arrange
    DistributorNotFoundException exception = new DistributorNotFoundException(1L);

    // Act
    HttpStatus status = exception.getStatus();

    // Assert
    assertThat(status).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void exception_shouldBeInstanceOfApiException_whenCreated() {
    // Arrange & Act
    DistributorNotFoundException exception = new DistributorNotFoundException(1L);

    // Assert
    assertThat(exception).isInstanceOf(ApiException.class);
  }

  @Test
  void exception_shouldBeInstanceOfRuntimeException_whenCreated() {
    // Arrange & Act
    DistributorNotFoundException exception = new DistributorNotFoundException(1L);

    // Assert
    assertThat(exception).isInstanceOf(RuntimeException.class);
  }

  @Test
  void constructor_shouldHandleNullId_whenCreatedWithNullId() {
    // Arrange & Act
    DistributorNotFoundException exception = new DistributorNotFoundException(null);

    // Assert
    assertThat(exception.getId()).isNull();
  }
}
