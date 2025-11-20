package com.mesofi.mythclothapi.references;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesofi.mythclothapi.entity.DescriptiveEntity;
import com.mesofi.mythclothapi.references.model.ReferencePairRequest;
import com.mesofi.mythclothapi.references.model.ReferencePairResponse;
import com.mesofi.mythclothapi.references.repository.IdDescPairRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReferencePairService {

  private final Map<String, IdDescPairRepository<?, Long>> repositories;
  private final ReferencePairMapper mapper;

  /** Maps resource names to entity-conversion functions. */
  private Map<String, Function<ReferencePairRequest, DescriptiveEntity>> entityFactories;

  @PostConstruct
  void init() {
    entityFactories =
        Map.of(
            "groups", mapper::toGroupEntity,
            "series", mapper::toSeriesEntity,
            "lineups", mapper::toLineUpEntity,
            "distributions", mapper::toDistributionEntity);
  }

  @Transactional
  public ReferencePairResponse createEntry(String referenceName, ReferencePairRequest request) {
    DescriptiveEntity saved = saveEntry(referenceName, mapToEntity(referenceName, request));
    return mapper.toCatalogResponse(saved);
  }

  private DescriptiveEntity mapToEntity(String referenceName, ReferencePairRequest request) {
    return Optional.ofNullable(entityFactories.get(referenceName))
        .map($ -> $.apply(request))
        .orElseThrow(() -> new ReferencePairNotFoundException(referenceName));
  }

  @SuppressWarnings("unchecked")
  private <T> T saveEntry(String referenceName, T entity) {
    return Optional.ofNullable(repositories.get(referenceName))
        .map($ -> (IdDescPairRepository<T, Long>) $)
        .map(repo -> repo.save(entity))
        .orElseThrow(() -> new ReferencePairNotFoundException(referenceName));
  }
}
