package com.mesofi.mythclothapi.catalogs;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import com.mesofi.mythclothapi.catalogs.dto.CatalogReq;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.catalogs.exceptions.CatalogNotFoundException;
import com.mesofi.mythclothapi.catalogs.exceptions.RepositoryNotFoundException;
import com.mesofi.mythclothapi.catalogs.model.Group;
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
  void createCatalog_shouldThrowConstraintViolation_whenCatalogNameIsEmpty() {
    // Arrange
    CatalogReq request = new CatalogReq("Gold Saints");

    // Act
    assertThatThrownBy(() -> catalogService.createCatalog("", request))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("createCatalog.catalogName: must not be empty");
  }

  @Test
  void createCatalog_shouldThrowConstraintViolation_whenRequestIsNull() {
    // Act
    assertThatThrownBy(() -> catalogService.createCatalog("groups", null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("createCatalog.request: must not be null");
  }

  @Test
  void createCatalog_shouldThrowIllegalArgumentException_whenCatalogTypeIsInvalid() {
    // Arrange
    CatalogReq request = new CatalogReq("Gold Saints");

    // Act
    assertThatThrownBy(() -> catalogService.createCatalog("invalid-type", request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("No enum constant");
  }

  @Test
  void createCatalog_shouldThrowCatalogNotFoundException_whenCatalogTypeFactoryIsMissing() {
    // Arrange
    CatalogReq request = new CatalogReq("Gold Saints");
    ReflectionTestUtils.setField(catalogService, "entityFactories", new HashMap<>());

    // Act
    assertThatThrownBy(() -> catalogService.createCatalog("groups", request))
        .isInstanceOf(CatalogNotFoundException.class)
        .hasMessageContaining("Catalog not found: groups");
  }

  @Test
  void createCatalog_shouldThrowRepositoryNotFoundException_whenRepositoryIsMissing() {
    // Arrange
    CatalogReq request = new CatalogReq("Gold Saints");

    // Act
    assertThatThrownBy(() -> catalogService.createCatalog("groups", request))
        .isInstanceOf(RepositoryNotFoundException.class)
        .hasMessageContaining("Repository not found: groups");
  }

  @Test
  void createCatalog_shouldReturnCatalogResponse_whenRequestIsValid() {
    // Arrange
    IdDescRepository<Group, Long> repository = mockRepository();
    CatalogReq request = new CatalogReq("Gold Saints");

    setRepository("groups", repository);
    when(repository.save(any(Group.class)))
        .thenAnswer(
            invocation -> {
              Group saved = invocation.getArgument(0);
              saved.setId(10L);
              return saved;
            });

    // Act
    CatalogResp response = catalogService.createCatalog("groups", request);

    // Assert
    assertThat(response).isEqualTo(new CatalogResp(10L, "Gold Saints"));
    verify(repository).save(any(Group.class));
  }

  @Test
  void retrieveCatalog_shouldThrowConstraintViolation_whenParamsAreNull() {
    // Act
    assertThatThrownBy(() -> catalogService.retrieveCatalog(null, null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("retrieveCatalog.catalogName: must not be empty")
        .hasMessageContaining("retrieveCatalog.id: must not be null");
  }

  @Test
  void retrieveCatalog_shouldThrowRepositoryNotFoundException_whenRepositoryIsMissing() {
    // Act
    assertThatThrownBy(() -> catalogService.retrieveCatalog("groups", 1L))
        .isInstanceOf(RepositoryNotFoundException.class)
        .hasMessageContaining("Repository not found: groups");
  }

  @Test
  void retrieveCatalog_shouldThrowCatalogNotFoundException_whenIdIsMissing() {
    // Arrange
    IdDescRepository<Group, Long> repository = mockRepository();

    setRepository("groups", repository);
    when(repository.findById(7L)).thenReturn(Optional.empty());

    // Act
    assertThatThrownBy(() -> catalogService.retrieveCatalog("groups", 7L))
        .isInstanceOf(CatalogNotFoundException.class)
        .hasMessageContaining("ID 7 not found in catalog 'groups'");
  }

  @Test
  void retrieveCatalog_shouldReturnCatalogResponse_whenEntryExists() {
    // Arrange
    IdDescRepository<Group, Long> repository = mockRepository();
    Group group = group(7L, "Asgard");

    setRepository("groups", repository);
    when(repository.findById(7L)).thenReturn(Optional.of(group));

    // Act
    CatalogResp response = catalogService.retrieveCatalog("groups", 7L);

    // Assert
    assertThat(response).isEqualTo(new CatalogResp(7L, "Asgard"));
  }

  @Test
  void retrieveCatalogs_shouldThrowConstraintViolation_whenCatalogNameIsNull() {
    // Act
    assertThatThrownBy(() -> catalogService.retrieveCatalogs(null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("retrieveCatalogs.catalogName: must not be empty");
  }

  @Test
  void retrieveCatalogs_shouldThrowConstraintViolation_whenCatalogNameIsEmpty() {
    // Act
    assertThatThrownBy(() -> catalogService.retrieveCatalogs(""))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("retrieveCatalogs.catalogName: must not be empty");
  }

  @Test
  void retrieveCatalogs_shouldThrowRepositoryNotFoundException_whenRepositoryIsMissing() {
    // Act
    assertThatThrownBy(() -> catalogService.retrieveCatalogs("groups"))
        .isInstanceOf(RepositoryNotFoundException.class)
        .hasMessageContaining("Repository not found: groups");
  }

  @Test
  void retrieveCatalogs_shouldReturnMappedResponses_whenEntriesExist() {
    // Arrange
    IdDescRepository<Group, Long> repository = mockRepository();
    Group bronze = group(1L, "Bronze Saints");
    Group asgard = group(2L, "Asgard");

    setRepository("groups", repository);
    when(repository.findAll()).thenReturn(List.of(bronze, asgard));

    // Act
    List<CatalogResp> response = catalogService.retrieveCatalogs("groups");

    // Assert
    assertThat(response)
        .isEqualTo(List.of(new CatalogResp(1L, "Bronze Saints"), new CatalogResp(2L, "Asgard")));
    verify(repository).findAll();
  }

  @Test
  void retrieveCatalogs_shouldReturnEmptyList_whenNoEntriesExist() {
    // Arrange
    IdDescRepository<Group, Long> repository = mockRepository();

    setRepository("groups", repository);
    when(repository.findAll()).thenReturn(List.of());

    // Act
    List<CatalogResp> response = catalogService.retrieveCatalogs("groups");

    // Assert
    assertThat(response).isEqualTo(List.of());
    verify(repository).findAll();
  }

  @Test
  void retrieveCatalogWithDescription_shouldThrowConstraintViolation_whenDescriptionIsNull() {
    // Act
    assertThatThrownBy(() -> catalogService.retrieveCatalogWithDescription("groups", null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("retrieveCatalogWithDescription.description: must not be null");
  }

  @Test
  void
      retrieveCatalogWithDescription_shouldThrowRepositoryNotFoundException_whenRepositoryIsMissing() {
    // Act
    assertThatThrownBy(() -> catalogService.retrieveCatalogWithDescription("groups", "Asgard"))
        .isInstanceOf(RepositoryNotFoundException.class)
        .hasMessageContaining("Repository not found: groups");
  }

  @Test
  void
      retrieveCatalogWithDescription_shouldThrowCatalogNotFoundException_whenDescriptionIsMissing() {
    // Arrange
    IdDescRepository<Group, Long> repository = mockRepository();

    setRepository("groups", repository);
    when(repository.findByDescription("Asgard")).thenReturn(Optional.empty());

    // Act
    assertThatThrownBy(() -> catalogService.retrieveCatalogWithDescription("groups", "Asgard"))
        .isInstanceOf(CatalogNotFoundException.class)
        .hasMessageContaining("Description 'Asgard' not found in catalog 'groups'");
  }

  @Test
  void retrieveCatalogWithDescription_shouldReturnCatalog_whenDescriptionExists() {
    // Arrange
    IdDescRepository<Group, Long> repository = mockRepository();
    Group group = group(3L, "Asgard");

    setRepository("groups", repository);
    when(repository.findByDescription("Asgard")).thenReturn(Optional.of(group));

    // Act
    Group found = (Group) catalogService.retrieveCatalogWithDescription("groups", "Asgard");

    // Assert
    assertThat(found).isSameAs(group);
  }

  @Test
  void updateCatalog_shouldThrowConstraintViolation_whenParamsAreNull() {
    // Act
    assertThatThrownBy(() -> catalogService.updateCatalog(null, null, null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("updateCatalog.catalogName: must not be empty")
        .hasMessageContaining("updateCatalog.id: must not be null")
        .hasMessageContaining("updateCatalog.request: must not be null");
  }

  @Test
  void updateCatalog_shouldThrowRepositoryNotFoundException_whenRepositoryIsMissing() {
    // Arrange
    CatalogReq request = new CatalogReq("Updated");

    // Act
    assertThatThrownBy(() -> catalogService.updateCatalog("groups", 5L, request))
        .isInstanceOf(RepositoryNotFoundException.class)
        .hasMessageContaining("Repository not found: groups");
  }

  @Test
  void updateCatalog_shouldThrowCatalogNotFoundException_whenIdIsMissing() {
    // Arrange
    IdDescRepository<Group, Long> repository = mockRepository();
    CatalogReq request = new CatalogReq("Updated");

    setRepository("groups", repository);
    when(repository.findById(5L)).thenReturn(Optional.empty());

    // Act
    assertThatThrownBy(() -> catalogService.updateCatalog("groups", 5L, request))
        .isInstanceOf(CatalogNotFoundException.class)
        .hasMessageContaining("ID 5 not found in catalog 'groups'");
  }

  @Test
  void updateCatalog_shouldUpdateDescriptionAndReturnResponse_whenEntryExists() {
    // Arrange
    IdDescRepository<Group, Long> repository = mockRepository();
    CatalogReq request = new CatalogReq("Athena Army");
    Group existing = group(11L, "Bronze Saints");

    setRepository("groups", repository);
    when(repository.findById(11L)).thenReturn(Optional.of(existing));
    when(repository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    CatalogResp response = catalogService.updateCatalog("groups", 11L, request);

    // Assert
    assertThat(response).isEqualTo(new CatalogResp(11L, "Athena Army"));

    ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
    verify(repository).save(groupCaptor.capture());
    assertThat(groupCaptor.getValue().getDescription()).isEqualTo("Athena Army");
  }

  @Test
  void deleteCatalog_shouldThrowConstraintViolation_whenParamsAreNull() {
    // Act
    assertThatThrownBy(() -> catalogService.deleteCatalog(null, null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("deleteCatalog.catalogName: must not be empty")
        .hasMessageContaining("deleteCatalog.id: must not be null");
  }

  @Test
  void deleteCatalog_shouldThrowRepositoryNotFoundException_whenRepositoryIsMissing() {
    // Act
    assertThatThrownBy(() -> catalogService.deleteCatalog("groups", 1L))
        .isInstanceOf(RepositoryNotFoundException.class)
        .hasMessageContaining("Repository not found: groups");
  }

  @Test
  void deleteCatalog_shouldThrowCatalogNotFoundException_whenIdIsMissing() {
    // Arrange
    IdDescRepository<Group, Long> repository = mockRepository();

    setRepository("groups", repository);
    when(repository.findById(99L)).thenReturn(Optional.empty());

    // Act
    assertThatThrownBy(() -> catalogService.deleteCatalog("groups", 99L))
        .isInstanceOf(CatalogNotFoundException.class)
        .hasMessageContaining("ID 99 not found in catalog 'groups'");

    verify(repository, never()).delete(any(Group.class));
  }

  @Test
  void deleteCatalog_shouldDeleteEntry_whenCatalogExists() {
    // Arrange
    IdDescRepository<Group, Long> repository = mockRepository();
    Group existing = group(20L, "Athena Army");

    setRepository("groups", repository);
    when(repository.findById(20L)).thenReturn(Optional.of(existing));

    // Act
    catalogService.deleteCatalog("groups", 20L);

    // Assert
    verify(repository).delete(existing);
  }

  @Test
  void deleteCatalog_shouldThrowRepositoryNotFoundException_whenRepositoryMissingAtDeleteStep() {
    // Arrange
    IdDescRepository<Group, Long> repository = mockRepository();
    Group existing = group(21L, "Athena Army");

    doReturn(repository).doReturn(null).when(repositories).get("groups");
    when(repository.findById(21L)).thenReturn(Optional.of(existing));

    // Act
    assertThatThrownBy(() -> catalogService.deleteCatalog("groups", 21L))
        .isInstanceOf(RepositoryNotFoundException.class)
        .hasMessageContaining("Repository not found: groups");

    verify(repository, never()).delete(any(Group.class));
  }

  private Group group(Long id, String description) {
    Group group = new Group();
    group.setId(id);
    group.setDescription(description);
    return group;
  }

  @SuppressWarnings("unchecked")
  private IdDescRepository<Group, Long> mockRepository() {
    return mock(IdDescRepository.class);
  }

  private void setRepository(String catalogName, IdDescRepository<?, Long> repository) {
    doReturn(repository).when(repositories).get(catalogName);
  }
}
