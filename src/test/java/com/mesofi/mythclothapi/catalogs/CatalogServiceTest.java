package com.mesofi.mythclothapi.catalogs;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.util.Map;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.mesofi.mythclothapi.catalogs.dto.CatalogReq;
import com.mesofi.mythclothapi.catalogs.exceptions.RepositoryNotFoundException;
import com.mesofi.mythclothapi.catalogs.repository.IdDescRepository;
import com.mesofi.mythclothapi.config.MapperTestConfig;
import com.mesofi.mythclothapi.config.MethodValidationTestConfig;

@SpringBootTest(
    classes = {CatalogService.class, MapperTestConfig.class, MethodValidationTestConfig.class})
class CatalogServiceTest {

  @Autowired private CatalogService catalogService;

  @MockitoBean Map<String, IdDescRepository<?, Long>> repositories;

  @Test
  void createCatalog_shouldThrowConstraintViolation_whenParamsAreNull() {
    // Act
    assertThatThrownBy(() -> catalogService.createCatalog(null, null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("createCatalog.catalogName: must not be empty")
        .hasMessageContaining("createCatalog.request: must not be null");
  }

  @Test
  void createCatalog_shouldThrowConstraintViolation_whenRequestIsNull() {
    // Act
    assertThatThrownBy(() -> catalogService.createCatalog("groups", null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("createCatalog.request: must not be null");
  }

  @Test
  void createCatalog_shouldThrowRepositoryNotFoundException_whenCatalogTypeIsUnsupported() {
    // Arrange
    CatalogReq request = new CatalogReq("Gold Saints");

    // Act
    assertThatThrownBy(() -> catalogService.createCatalog("groups", request))
        .isInstanceOf(RepositoryNotFoundException.class)
        .hasMessageContaining("Repository not found: groups");
  }
}
