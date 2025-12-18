package com.mesofi.mythclothapi.catalogs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mesofi.mythclothapi.catalogs.dto.CatalogReq;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.catalogs.exceptions.CatalogNotFoundException;
import com.mesofi.mythclothapi.catalogs.exceptions.RepositoryNotFoundException;
import com.mesofi.mythclothapi.common.Descriptive;

import jakarta.validation.ConstraintViolationException;

@SpringBootTest
@ActiveProfiles("test")
public class CatalogServiceTest {
  @Autowired CatalogService service;

  @Test
  void createCatalog_shouldThrowException_whenRequiredFieldsAreNull() {
    assertThatThrownBy(() -> service.createCatalog(null, null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("createCatalog.request")
        .hasMessageContaining("createCatalog.catalogName")
        .hasMessageContaining("must not be null");
  }

  @Test
  void createCatalog_shouldThrowException_whenRequestIsNull() {
    assertThatThrownBy(() -> service.createCatalog("some-reference", null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("createCatalog.request")
        .hasMessageContaining("must not be null");
  }

  @Test
  void createCatalog_shouldThrowException_whenRepositoryDoesNotExist() {
    assertThatThrownBy(
            () -> service.createCatalog("some-reference", new CatalogReq("Some description")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(
            "No enum constant com.mesofi.mythclothapi.catalogs.dto.CatalogType.some-reference");
  }

  @Test
  void createCatalog_shouldCreateCatalog_whenGroupIsProvided() {
    CatalogReq request = new CatalogReq("Some description");

    CatalogResp response = service.createCatalog("groups", request);
    assertThat(response).isNotNull();
    assertThat(response.id()).isNotZero();
    assertThat(response.description()).isEqualTo("Some description");
  }

  @Test
  void retrieveCatalog_shouldThrowException_whenRequiredFieldsAreNull() {
    assertThatThrownBy(() -> service.retrieveCatalog(null, null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("retrieveCatalog.id")
        .hasMessageContaining("retrieveCatalog.catalogName")
        .hasMessageContaining("must not be null");
  }

  @Test
  void retrieveReference_shouldThrowException_whenIdIsNull() {
    assertThatThrownBy(() -> service.retrieveCatalog("some-catalog", null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("retrieveCatalog.id")
        .hasMessageContaining("must not be null");
  }

  @Test
  void retrieveCatalog_shouldThrowException_whenRepositoryNotFound() {
    assertThatThrownBy(() -> service.retrieveCatalog("some-catalog", -1L))
        .isInstanceOf(RepositoryNotFoundException.class)
        .hasMessage("Repository not found: some-catalog");
  }

  @Test
  void retrieveCatalog_shouldThrowException_whenIdNotFound() {
    assertThatThrownBy(() -> service.retrieveCatalog("series", -1L))
        .isInstanceOf(CatalogNotFoundException.class)
        .hasMessage("Catalog not found: ID -1 not found in catalog 'series'");
  }

  @Test
  void retrieveCatalog_shouldRetrieveCatalog_whenSeriesIsProvided() {
    CatalogReq request = new CatalogReq("The description");
    CatalogResp resp = service.createCatalog("series", request);

    CatalogResp response = service.retrieveCatalog("series", resp.id());
    assertThat(response).isNotNull();
    assertThat(response.id()).isNotZero();
    assertThat(response.description()).isEqualTo("The description");
  }

  @Test
  void retrieveCatalogWithDescription_shouldThrowException_whenRequiredFieldsAreNull() {
    assertThatThrownBy(() -> service.retrieveCatalogWithDescription(null, null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("retrieveCatalogWithDescription.description")
        .hasMessageContaining("retrieveCatalogWithDescription.catalogName")
        .hasMessageContaining("must not be null");
  }

  @Test
  void retrieveCatalogWithDescription_shouldThrowException_whenIdIsNull() {
    assertThatThrownBy(() -> service.retrieveCatalogWithDescription("some-catalog", null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("retrieveCatalogWithDescription.description")
        .hasMessageContaining("must not be null");
  }

  @Test
  void retrieveCatalogWithDescription_shouldThrowException_whenRepositoryNotFound() {
    assertThatThrownBy(
            () -> service.retrieveCatalogWithDescription("some-catalog", "The description"))
        .isInstanceOf(RepositoryNotFoundException.class)
        .hasMessage("Repository not found: some-catalog");
  }

  @Test
  void retrieveCatalogWithDescription_shouldThrowException_whenDescriptionNotFound() {
    assertThatThrownBy(() -> service.retrieveCatalogWithDescription("series", "Sample description"))
        .isInstanceOf(CatalogNotFoundException.class)
        .hasMessage(
            "Catalog not found: Description 'Sample description' not found in catalog 'series'");
  }

  @Test
  void retrieveCatalogWithDescription_shouldRetrieveCatalog_whenSeriesIsProvided() {
    CatalogReq request = new CatalogReq("Some description");
    CatalogResp resp = service.createCatalog("series", request);

    Descriptive descriptiveEntity =
        service.retrieveCatalogWithDescription("series", resp.description());
    assertThat(descriptiveEntity).isNotNull();
    assertThat(descriptiveEntity.getDescription()).isEqualTo("Some description");
  }

  @Test
  void updateCatalog_shouldThrowException_whenRequiredFieldsAreNull() {
    // Arrange, Act, Assert
    assertThatThrownBy(() -> service.updateCatalog(null, null, null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("updateCatalog.catalogName")
        .hasMessageContaining("updateCatalog.id")
        .hasMessageContaining("updateCatalog.request")
        .hasMessageContaining("must not be null");
  }

  @Test
  void updateCatalog_shouldThrowException_whenIdAndRequestAreNull() {
    // Arrange, Act, Assert
    assertThatThrownBy(() -> service.updateCatalog("some-catalog", null, null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("updateCatalog.id")
        .hasMessageContaining("updateCatalog.request")
        .hasMessageContaining("must not be null");
  }

  @Test
  void updateCatalog_shouldThrowException_whenRequestIsNull() {
    // Arrange, Act, Assert
    assertThatThrownBy(() -> service.updateCatalog("some-catalog", -1L, null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("updateCatalog.request")
        .hasMessageContaining("must not be null");
  }

  @Test
  void updateCatalog_shouldThrowException_whenCatalogNotFound() {
    // Arrange
    CatalogReq request = new CatalogReq("Some description");

    // Act, Assert
    assertThatThrownBy(() -> service.updateCatalog("series", -1L, request))
        .isInstanceOf(CatalogNotFoundException.class)
        .hasMessage("Catalog not found: ID -1 not found in catalog 'series'");
  }

  @Test
  void updateCatalog_shouldUpdateCatalog_whenGroupIsProvided() {
    // Arrange
    CatalogReq request = new CatalogReq("Some description");
    CatalogResp response = service.createCatalog("groups", request);
    CatalogReq updatedRequest = new CatalogReq("Updated description");

    // Act
    CatalogResp updatedResponse = service.updateCatalog("groups", response.id(), updatedRequest);

    // Assert
    assertThat(updatedResponse).isNotNull();
    assertThat(updatedResponse.id()).isNotZero();
    assertThat(updatedResponse.description()).isEqualTo("Updated description");
  }

  @Test
  void deleteCatalog_shouldThrowException_whenRequiredFieldsAreNull() {
    // Arrange, Act, Assert
    assertThatThrownBy(() -> service.deleteCatalog(null, null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("deleteCatalog.catalogName")
        .hasMessageContaining("deleteCatalog.id")
        .hasMessageContaining("must not be null");
  }

  @Test
  void deleteCatalog_shouldThrowException_whenIdIsNull() {
    // Arrange, Act, Assert
    assertThatThrownBy(() -> service.deleteCatalog("some-catalog", null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("deleteCatalog.id")
        .hasMessageContaining("must not be null");
  }

  @Test
  void deleteCatalog_shouldThrowException_whenRepositoryNotFound() {
    // Arrange, Act, Assert
    assertThatThrownBy(() -> service.deleteCatalog("some-catalog", -1L))
        .isInstanceOf(RepositoryNotFoundException.class)
        .hasMessage("Repository not found: some-catalog");
  }

  @Test
  void deleteCatalog_shouldThrowException_whenCatalogNotFound() {
    // Arrange, Act, Assert
    assertThatThrownBy(() -> service.deleteCatalog("series", -1L))
        .isInstanceOf(CatalogNotFoundException.class)
        .hasMessage("Catalog not found: ID -1 not found in catalog 'series'");
  }

  @Test
  void deleteCatalog_shouldDeleteCatalog_whenSeriesIsProvided() {
    // Arrange
    CatalogReq request = new CatalogReq("Some description");
    CatalogResp response = service.createCatalog("series", request);

    // Act
    service.deleteCatalog("series", response.id());

    // Assert
    assertThatThrownBy(() -> service.deleteCatalog("series", response.id()))
        .isInstanceOf(CatalogNotFoundException.class);
  }
}
