package com.mesofi.mythclothapi.distributors;

import com.mesofi.mythclothapi.distributors.exceptions.DistributorAlreadyExistsException;
import com.mesofi.mythclothapi.distributors.exceptions.DistributorNotFoundException;
import com.mesofi.mythclothapi.distributors.model.DistributorRequest;
import com.mesofi.mythclothapi.distributors.model.DistributorResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributorService {

  private final DistributorRepository repository;
  private final DistributorMapper mapper;

  public DistributorResponse createDistributor(DistributorRequest request) {
    DistributorEntity entity = mapper.toDistributorEntity(request);

    // Validate unique constraint manually before hitting DB
    if (repository.existsByNameAndCountry(entity.getName(), entity.getCountry())) {
      throw new DistributorAlreadyExistsException(request.name(), request.country());
    }

    var saved = repository.save(entity);
    return mapper.toDistributorResponse(saved);
  }

  public DistributorResponse retrieveDistributor(Long id) {
    return repository
        .findById(id)
        .map(mapper::toDistributorResponse)
        .orElseThrow(() -> new DistributorNotFoundException(id));
  }

  public List<DistributorResponse> retrieveDistributors() {
    return repository.findAll().stream().map(mapper::toDistributorResponse).toList();
  }

  public DistributorResponse updateDistributor(Long id, DistributorRequest request) {
    var existing = repository.findById(id).orElseThrow(() -> new DistributorNotFoundException(id));

    DistributorEntity entity = mapper.toDistributorEntity(request);

    // Check unique name+country but allow same record to stay unchanged
    if (repository.existsByNameAndCountry(entity.getName(), entity.getCountry())
        && !(existing.getName().name().equals(request.name())
            && existing.getCountry().name().equals(request.country()))) {

      throw new DistributorAlreadyExistsException(request.name(), request.country());
    }

    // Ask MapStruct to update only the changed fields
    mapper.updateDistributorEntity(request, existing);

    var saved = repository.save(existing);
    return mapper.toDistributorResponse(saved);
  }

  public void removeDistributor(Long id) {
    if (!repository.existsById(id)) {
      throw new DistributorNotFoundException(id);
    }
    repository.deleteById(id);
  }
}
