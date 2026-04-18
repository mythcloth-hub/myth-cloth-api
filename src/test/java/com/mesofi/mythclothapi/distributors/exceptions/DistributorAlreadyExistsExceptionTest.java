package com.mesofi.mythclothapi.distributors.exceptions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

class DistributorAlreadyExistsExceptionTest {

  @Test
  void constructor_shouldSetNameCorrectly_whenCreatedWithGivenNameAndCountry() {
    // Arrange
    String name = "Bandai";
    String country = "Japan";

    // Act
    DistributorAlreadyExistsException exception =
        new DistributorAlreadyExistsException(name, country);

    // Assert
    assertThat(exception.getName()).isEqualTo(name);
  }

  @Test
  void constructor_shouldSetCountryCorrectly_whenCreatedWithGivenNameAndCountry() {
    // Arrange
    String name = "Bandai";
    String country = "Japan";

    // Act
    DistributorAlreadyExistsException exception =
        new DistributorAlreadyExistsException(name, country);

    // Assert
    assertThat(exception.getCountry()).isEqualTo(country);
  }

  @Test
  void constructor_shouldSetMessageCorrectly_whenCreated() {
    // Arrange & Act
    DistributorAlreadyExistsException exception =
        new DistributorAlreadyExistsException("Bandai", "Japan");

    // Assert
    assertThat(exception.getMessage()).isEqualTo("Distributor already exists");
  }

  @Test
  void constructor_shouldSetCauseDetailCorrectly_whenCreated() {
    // Arrange & Act
    DistributorAlreadyExistsException exception =
        new DistributorAlreadyExistsException("Bandai", "Japan");

    // Assert
    assertThat(exception.getCauseDetail()).isEqualTo("Distributor already exists: Bandai - Japan");
  }

  @Test
  void getStatus_shouldReturnConflict_whenCalled() {
    // Arrange
    DistributorAlreadyExistsException exception =
        new DistributorAlreadyExistsException("Bandai", "Japan");

    // Act
    HttpStatus status = exception.getStatus();

    // Assert
    assertThat(status).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void exception_shouldBeInstanceOfApiException_whenCreated() {
    // Arrange & Act
    DistributorAlreadyExistsException exception =
        new DistributorAlreadyExistsException("Bandai", "Japan");

    // Assert
    assertThat(exception).isInstanceOf(ApiException.class);
  }

  @Test
  void exception_shouldBeInstanceOfRuntimeException_whenCreated() {
    // Arrange & Act
    DistributorAlreadyExistsException exception =
        new DistributorAlreadyExistsException("Bandai", "Japan");

    // Assert
    assertThat(exception).isInstanceOf(RuntimeException.class);
  }

  @Test
  void constructor_shouldHandleNullArguments_whenCreatedWithNullNameAndCountry() {
    // Arrange & Act
    DistributorAlreadyExistsException exception = new DistributorAlreadyExistsException(null, null);

    // Assert
    assertThat(exception.getName()).isNull();
    assertThat(exception.getCountry()).isNull();
    assertThat(exception.getCauseDetail()).isEqualTo("Distributor already exists: null - null");
  }
}
