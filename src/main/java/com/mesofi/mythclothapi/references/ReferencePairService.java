package com.mesofi.mythclothapi.references;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.mesofi.mythclothapi.entity.DescriptiveEntity;
import com.mesofi.mythclothapi.references.exceptions.ReferencePairNotFoundException;
import com.mesofi.mythclothapi.references.exceptions.RepositoryNotFoundException;
import com.mesofi.mythclothapi.references.model.ReferencePairRequest;
import com.mesofi.mythclothapi.references.model.ReferencePairResponse;
import com.mesofi.mythclothapi.references.model.ReferencePairType;
import com.mesofi.mythclothapi.references.repository.IdDescPairRepository;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class ReferencePairService {

  private final Map<String, IdDescPairRepository<?, Long>> repositories;
  private final ReferencePairMapper mapper;

  /** Maps resource names to entity-conversion functions. */
  private Map<ReferencePairType, Function<ReferencePairRequest, DescriptiveEntity>> entityFactories;

  @PostConstruct
  void init() {
    entityFactories =
        Map.of(
            ReferencePairType.groups, mapper::toGroupEntity,
            ReferencePairType.series, mapper::toSeriesEntity,
            ReferencePairType.lineups, mapper::toLineUpEntity,
            ReferencePairType.distributions, mapper::toDistributionEntity);
  }

  @Transactional
  public ReferencePairResponse createReference(
      @NotNull String referenceName, @NotNull ReferencePairRequest request) {
    DescriptiveEntity saved = saveEntry(referenceName, mapToEntity(referenceName, request));
    return mapper.toCatalogResponse(saved);
  }

  @Transactional(readOnly = true)
  public ReferencePairResponse retrieveReference(@NotNull String referenceName, @NotNull Long id) {
    DescriptiveEntity found = findByIdEntry(referenceName, id);
    return mapper.toCatalogResponse(found);
  }

  @Transactional
  public ReferencePairResponse updateReference(
      @NotNull String referenceName, @NotNull Long id, @NotNull ReferencePairRequest request) {
    DescriptiveEntity existing = findByIdEntry(referenceName, id);
    // updates the description
    existing.setDescription(request.description());

    return mapper.toCatalogResponse(saveEntry(referenceName, existing));
  }

  @Transactional
  public void deleteReference(@NotNull String referenceName, @NotNull Long id) {
    DescriptiveEntity existing = findByIdEntry(referenceName, id);
    deleteEntry(referenceName, existing);
  }

  private DescriptiveEntity mapToEntity(String referenceName, ReferencePairRequest request) {
    return Optional.ofNullable(entityFactories.get(ReferencePairType.valueOf(referenceName)))
        .map($ -> $.apply(request))
        .orElseThrow(() -> new ReferencePairNotFoundException(referenceName));
  }

  @SuppressWarnings("unchecked")
  private <T> T saveEntry(String referenceName, T entity) {
    return Optional.ofNullable(repositories.get(referenceName))
        .map(repo -> (IdDescPairRepository<T, Long>) repo)
        .map(repo -> repo.save(entity))
        .orElseThrow(() -> new RepositoryNotFoundException(referenceName));
  }

  @SuppressWarnings("unchecked")
  private <T> T findByIdEntry(String referenceName, Long id) {
    return Optional.ofNullable(repositories.get(referenceName))
        .map(repo -> (IdDescPairRepository<T, Long>) repo)
        .map(
            repo ->
                repo.findById(id)
                    .orElseThrow(
                        () ->
                            new ReferencePairNotFoundException(
                                "ID %d not found in reference '%s'".formatted(id, referenceName))))
        .orElseThrow(() -> new RepositoryNotFoundException(referenceName));
  }

  @SuppressWarnings("unchecked")
  private <T> void deleteEntry(String referenceName, T entity) {
    IdDescPairRepository<T, Long> repo =
        Optional.ofNullable(repositories.get(referenceName))
            .map(r -> (IdDescPairRepository<T, Long>) r)
            .orElseThrow(() -> new RepositoryNotFoundException(referenceName));

    repo.delete(entity);
  }
}
