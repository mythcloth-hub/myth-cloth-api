package com.mesofi.mythclothapi.catalogs;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.mesofi.mythclothapi.catalogs.dto.CatalogReq;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.catalogs.dto.CatalogType;
import com.mesofi.mythclothapi.catalogs.exceptions.CatalogNotFoundException;
import com.mesofi.mythclothapi.catalogs.exceptions.RepositoryNotFoundException;
import com.mesofi.mythclothapi.catalogs.repository.IdDescRepository;
import com.mesofi.mythclothapi.common.Descriptive;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class CatalogService {

  private final Map<String, IdDescRepository<?, Long>> repositories;
  private final CatalogMapper mapper;

  /** Maps resource names to entity-conversion functions. */
  private Map<CatalogType, Function<CatalogReq, Descriptive>> entityFactories;

  @PostConstruct
  void init() {
    entityFactories =
        Map.of(
            CatalogType.anniversaries, mapper::toAnniversary,
            CatalogType.groups, mapper::toGroup,
            CatalogType.series, mapper::toSeries,
            CatalogType.lineups, mapper::toLineUp,
            CatalogType.distributions, mapper::toDistribution);
  }

  @Transactional
  public CatalogResp createCatalog(@NotNull String catalogName, @NotNull CatalogReq request) {
    Descriptive saved = saveEntry(catalogName, mapToEntity(catalogName, request));
    return mapper.toCatalogResp(saved);
  }

  @Transactional(readOnly = true)
  public CatalogResp retrieveCatalog(@NotNull String catalogName, @NotNull Long id) {
    Descriptive found = findByIdEntry(catalogName, id);
    return mapper.toCatalogResp(found);
  }

  @Transactional(readOnly = true)
  public Descriptive retrieveCatalogWithDescription(
      @NotNull String catalogName, @NotNull String description) {
    return findByDescription(catalogName, description);
  }

  @Transactional
  public CatalogResp updateCatalog(
      @NotNull String catalogName, @NotNull Long id, @NotNull CatalogReq request) {
    Descriptive existing = findByIdEntry(catalogName, id);
    // updates the description
    existing.setDescription(request.description());

    return mapper.toCatalogResp(saveEntry(catalogName, existing));
  }

  @Transactional
  public void deleteCatalog(@NotNull String catalogName, @NotNull Long id) {
    Descriptive existing = findByIdEntry(catalogName, id);
    deleteEntry(catalogName, existing);
  }

  private Descriptive mapToEntity(String catalogName, CatalogReq request) {
    return Optional.ofNullable(entityFactories.get(CatalogType.valueOf(catalogName)))
        .map($ -> $.apply(request))
        .orElseThrow(() -> new CatalogNotFoundException(catalogName));
  }

  @SuppressWarnings("unchecked")
  private <T> T saveEntry(String catalogName, T entity) {
    return Optional.ofNullable(repositories.get(catalogName))
        .map(repo -> (IdDescRepository<T, Long>) repo)
        .map(repo -> repo.save(entity))
        .orElseThrow(() -> new RepositoryNotFoundException(catalogName));
  }

  @SuppressWarnings("unchecked")
  private <T> T findByIdEntry(String catalogName, Long id) {
    return Optional.ofNullable(repositories.get(catalogName))
        .map(repo -> (IdDescRepository<T, Long>) repo)
        .map(
            repo ->
                repo.findById(id)
                    .orElseThrow(
                        () ->
                            new CatalogNotFoundException(
                                "ID %d not found in catalog '%s'".formatted(id, catalogName))))
        .orElseThrow(() -> new RepositoryNotFoundException(catalogName));
  }

  @SuppressWarnings("unchecked")
  private <T> T findByDescription(String catalogName, String description) {
    return Optional.ofNullable(repositories.get(catalogName))
        .map(repo -> (IdDescRepository<T, Long>) repo)
        .map(
            repo ->
                repo.findByDescription(description)
                    .orElseThrow(
                        () ->
                            new CatalogNotFoundException(
                                "Description '%s' not found in catalog '%s'"
                                    .formatted(description, catalogName))))
        .orElseThrow(() -> new RepositoryNotFoundException(catalogName));
  }

  @SuppressWarnings("unchecked")
  private <T> void deleteEntry(String catalogName, T entity) {
    IdDescRepository<T, Long> repo =
        Optional.ofNullable(repositories.get(catalogName))
            .map(r -> (IdDescRepository<T, Long>) r)
            .orElseThrow(() -> new RepositoryNotFoundException(catalogName));

    repo.delete(entity);
  }
}
