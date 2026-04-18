package com.mesofi.mythclothapi.catalogs.exceptions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

class CatalogNotFoundExceptionTest {

  @Test
  void constructor_shouldSetNameCorrectly_whenCreatedWithGivenName() {
    // Arrange
    String name = "saint-seiya-catalog";

    // Act
    CatalogNotFoundException exception = new CatalogNotFoundException(name);

    // Assert
    assertThat(exception.getName()).isEqualTo(name);
  }

  @Test
  void constructor_shouldSetMessageCorrectly_whenCreated() {
    // Arrange & Act
    CatalogNotFoundException exception = new CatalogNotFoundException("saint-seiya-catalog");

    // Assert
    assertThat(exception.getMessage()).isEqualTo("Catalog not found: saint-seiya-catalog");
  }

  @Test
  void constructor_shouldSetCauseDetailCorrectly_whenCreated() {
    // Arrange & Act
    CatalogNotFoundException exception = new CatalogNotFoundException("saint-seiya-catalog");

    // Assert
    assertThat(exception.getCauseDetail()).isEqualTo("Catalog not found: saint-seiya-catalog");
  }

  @Test
  void getStatus_shouldReturnNotFound_whenCalled() {
    // Arrange
    CatalogNotFoundException exception = new CatalogNotFoundException("saint-seiya-catalog");

    // Act
    HttpStatus status = exception.getStatus();

    // Assert
    assertThat(status).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void exception_shouldBeInstanceOfApiException_whenCreated() {
    // Arrange & Act
    CatalogNotFoundException exception = new CatalogNotFoundException("saint-seiya-catalog");

    // Assert
    assertThat(exception).isInstanceOf(ApiException.class);
  }

  @Test
  void exception_shouldBeInstanceOfRuntimeException_whenCreated() {
    // Arrange & Act
    CatalogNotFoundException exception = new CatalogNotFoundException("saint-seiya-catalog");

    // Assert
    assertThat(exception).isInstanceOf(RuntimeException.class);
  }

  @Test
  void constructor_shouldHandleNullName_whenCreatedWithNullName() {
    // Arrange & Act
    CatalogNotFoundException exception = new CatalogNotFoundException(null);

    // Assert
    assertThat(exception.getName()).isNull();
    assertThat(exception.getMessage()).isEqualTo("Catalog not found: null");
    assertThat(exception.getCauseDetail()).isEqualTo("Catalog not found: null");
  }
}
