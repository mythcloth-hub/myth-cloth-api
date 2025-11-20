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
  private Map<String, Function<ReferencePairRequest, DescriptiveEntity>> converters;

  @PostConstruct
  void init() {
    converters =
        Map.of(
            "groups", mapper::toGroupEntity,
            "series", mapper::toSeriesEntity,
            "lineups", mapper::toLineUpEntity,
            "distributions", mapper::toDistributionEntityEntity);
  }

  @Transactional
  public ReferencePairResponse create(String catalogName, ReferencePairRequest request) {
    DescriptiveEntity saved = save(catalogName, convert(catalogName, request));
    return mapper.toCatalogResponse(saved);
  }

  private DescriptiveEntity convert(String catalogName, ReferencePairRequest request) {
    return Optional.ofNullable(converters.get(catalogName))
        .map($ -> $.apply(request))
        .orElseThrow(() -> new IllegalArgumentException("Unknown catalog: " + catalogName));
  }

  @SuppressWarnings("unchecked")
  private <T> T save(String catalogName, T entity) {
    IdDescPairRepository<T, Long> repo =
        (IdDescPairRepository<T, Long>) repositories.get(catalogName);

    if (repo == null) {
      throw new IllegalArgumentException("Unknown catalog: " + catalogName);
    }

    return repo.save(entity);
  }
}
