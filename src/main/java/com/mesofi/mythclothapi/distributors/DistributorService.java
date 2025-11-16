package com.mesofi.mythclothapi.distributors;

import com.mesofi.mythclothapi.distributors.exceptions.DistributorAlreadyExistsException;
import com.mesofi.mythclothapi.distributors.exceptions.DistributorNotFoundException;
import com.mesofi.mythclothapi.distributors.model.DistributorRequest;
import com.mesofi.mythclothapi.distributors.model.DistributorResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer responsible for managing {@link DistributorEntity} records.
 *
 * <p>Provides CRUD operations for distributors, including validation of unique constraints and
 * transformation between request/response DTOs and persistence entities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributorService {

  private final DistributorRepository repository;
  private final DistributorMapper mapper;

  /**
   * Creates a new distributor.
   *
   * <p>This method converts the incoming {@link DistributorRequest} into a {@link
   * DistributorEntity}, validates the uniqueness of the distributor by name and country, and
   * persists it in the repository.
   *
   * @param request the distributor data to create
   * @return the created distributor as a {@link DistributorResponse}
   * @throws DistributorAlreadyExistsException if a distributor with the same name and country
   *     already exists
   */
  @Transactional
  public DistributorResponse createDistributor(DistributorRequest request) {
    log.info("Creating distributor: {} - {}", request.name(), request.country());

    DistributorEntity entity = mapper.toDistributorEntity(request);

    // Validate unique constraint manually before hitting DB
    if (repository.existsByNameAndCountry(entity.getName(), entity.getCountry())) {
      throw new DistributorAlreadyExistsException(request.name(), request.country());
    }

    var saved = repository.save(entity);
    return mapper.toDistributorResponse(saved);
  }

  /**
   * Retrieves a distributor by its unique ID.
   *
   * @param id the distributor ID
   * @return the distributor mapped as a {@link DistributorResponse}
   * @throws DistributorNotFoundException if no distributor with the given ID exists
   */
  @Transactional(readOnly = true)
  public DistributorResponse retrieveDistributor(Long id) {
    return repository
        .findById(id)
        .map(mapper::toDistributorResponse)
        .orElseThrow(() -> new DistributorNotFoundException(id));
  }

  /**
   * Retrieves all distributors stored in the system.
   *
   * @return a list of all distributors as {@link DistributorResponse}
   */
  @Transactional(readOnly = true)
  public List<DistributorResponse> retrieveDistributors() {
    return repository.findAll().stream().map(mapper::toDistributorResponse).toList();
  }

  /**
   * Updates an existing distributor with new information.
   *
   * <p>This method retrieves the existing distributor, validates that updating the name and country
   * does not conflict with another distributor, and applies the updates via MapStruct.
   *
   * @param id the ID of the distributor to update
   * @param request the updated distributor information
   * @return the updated distributor as a {@link DistributorResponse}
   * @throws DistributorNotFoundException if the distributor to update does not exist
   * @throws DistributorAlreadyExistsException if the update results in a name+country conflict with
   *     another distributor
   */
  @Transactional
  public DistributorResponse updateDistributor(Long id, DistributorRequest request) {
    log.info("Updating distributor {} to {} - {}", id, request.name(), request.country());
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

  /**
   * Removes a distributor by ID.
   *
   * @param id the ID of the distributor to delete
   * @throws DistributorNotFoundException if no distributor with the given ID exists
   */
  public void removeDistributor(Long id) {
    log.warn("Removing distributor {}", id);

    if (!repository.existsById(id)) {
      throw new DistributorNotFoundException(id);
    }
    repository.deleteById(id);
  }
}
