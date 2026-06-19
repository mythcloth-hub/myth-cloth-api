package com.mesofi.mythclothapi.collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.collectors.exceptions.CollectorInvalidTokenException;
import com.mesofi.mythclothapi.error.ApiException;

class CollectorInvalidTokenExceptionTest {

  @Test
  void constructor_shouldSetMessageCorrectly_whenCreated() {
    // Arrange & Act
    CollectorInvalidTokenException exception =
        new CollectorInvalidTokenException("Facebook token is invalid");

    // Assert
    assertThat(exception.getMessage()).isEqualTo("Facebook token is invalid");
  }

  @Test
  void constructor_shouldSetCauseDetailCorrectly_whenCreated() {
    // Arrange & Act
    CollectorInvalidTokenException exception =
        new CollectorInvalidTokenException("Google token is expired");

    // Assert
    assertThat(exception.getCauseDetail()).isEqualTo("Google token is expired");
  }

  @Test
  void getStatus_shouldReturnUnauthorized_whenCalled() {
    // Arrange
    CollectorInvalidTokenException exception =
        new CollectorInvalidTokenException("Token validation failed");

    // Act
    HttpStatus status = exception.getStatus();

    // Assert
    assertThat(status).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void exception_shouldBeInstanceOfApiException_whenCreated() {
    // Arrange & Act
    CollectorInvalidTokenException exception =
        new CollectorInvalidTokenException("Token validation failed");

    // Assert
    assertThat(exception).isInstanceOf(ApiException.class);
  }

  @Test
  void exception_shouldBeInstanceOfRuntimeException_whenCreated() {
    // Arrange & Act
    CollectorInvalidTokenException exception =
        new CollectorInvalidTokenException("Token validation failed");

    // Assert
    assertThat(exception).isInstanceOf(RuntimeException.class);
  }

  @Test
  void constructor_shouldHandleNullMessage_whenCreatedWithNullMessage() {
    // Arrange & Act
    CollectorInvalidTokenException exception = new CollectorInvalidTokenException(null);

    // Assert
    assertThat(exception.getMessage()).isNull();
    assertThat(exception.getCauseDetail()).isNull();
  }
}
