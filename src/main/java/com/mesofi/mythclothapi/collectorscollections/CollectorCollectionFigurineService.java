package com.mesofi.mythclothapi.collectorscollections;

import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.ANNOUNCED;
import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.RELEASED;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.collectors.CollectorRepository;
import com.mesofi.mythclothapi.collectors.exceptions.CollectorNotFoundException;
import com.mesofi.mythclothapi.collectors.mapper.CollectorMapper;
import com.mesofi.mythclothapi.collectorscollections.dto.AssignFigurinesReq;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectionAssignmentMode;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionFigurineDetailResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionFigurineResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionReq;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionResp;
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectionAlreadyExistsException;
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectionNotFoundException;
import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionFigurineRepository;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionRepository;
import com.mesofi.mythclothapi.figurines.FigurineFilter;
import com.mesofi.mythclothapi.figurines.FigurineFilterFactory;
import com.mesofi.mythclothapi.figurines.FigurineService;
import com.mesofi.mythclothapi.figurines.exceptions.FigurineNotFoundException;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for managing the relationship between collectors, collections, and figurines.
 *
 * <p>This service handles operations related to assigning figurines to collector collections,
 * retrieving collector collections, and creating collections when required.
 *
 * <p>The assignment workflow supports multiple modes through {@link CollectionAssignmentMode}:
 *
 * <ul>
 *   <li>{@code AUTO}: Creates or uses the collector's default collection.
 *   <li>{@code CREATE}: Creates a new collection using the provided collection information.
 *   <li>{@code EXISTING}: Assigns figurines to existing collections.
 * </ul>
 *
 * <p>Before assigning figurines, this service validates that:
 *
 * <ul>
 *   <li>The requested figurines exist.
 *   <li>The collector exists when creating or retrieving collections.
 *   <li>The target collections exist when using existing collections.
 *   <li>New collection names do not conflict with existing collections.
 * </ul>
 *
 * <p>This service coordinates multiple repositories to maintain the association between collectors,
 * collections, and figurines.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class CollectorCollectionFigurineService {

  private final CollectorCollectionFigurineRepository collectorCollectionFigurineRepository;
  private final CollectorCollectionRepository collectorCollectionRepository;
  private final CollectorRepository collectorRepository;
  private final FigurineRepository figurineRepository;
  private final FigurineService figurineService;
  private final CollectorMapper collectorMapper;

  /**
   * Adds a single figurine to a specific collection.
   *
   * <p>If the figurine already exists in the collection, this method does not create a duplicate
   * relationship.
   *
   * <p>This method is deprecated because it only supports a single figurine and collection
   * assignment. Use {@link #assignFigurinesToCollections(Long, AssignFigurinesReq)} instead, which
   * supports multiple figurines, multiple collections, quantity updates, and collection creation
   * strategies.
   *
   * @param collectionId identifier of the target collection
   * @param figurineId identifier of the figurine to assign
   * @deprecated Use {@link #assignFigurinesToCollections(Long, AssignFigurinesReq)} instead.
   */
  @Deprecated
  public void addFigurineToCollection(Long collectionId, Long figurineId) {

    var existingFigurine =
        figurineRepository
            .findById(figurineId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException("Figurine with id " + figurineId + " not found"));

    var existingCollection =
        collectorCollectionRepository
            .findById(collectionId)
            .orElseGet(() -> createDefaultCollection(collectionId));

    if (collectorCollectionFigurineRepository
        .findByCollectionAndFigurine(existingCollection, existingFigurine)
        .isPresent()) {
      throw new IllegalArgumentException(
          "Figurine with id " + figurineId + " already in collection with id " + collectionId);
    }

    collectorCollectionFigurineRepository.save(
        buildCollectionFigurine(existingCollection, existingFigurine));
  }

  /**
   * Assigns one or more figurines to one or more collector collections.
   *
   * <p>The assignment behavior depends on the requested {@link CollectionAssignmentMode}.
   *
   * <p>This method:
   *
   * <ul>
   *   <li>Validates that all requested figurines exist.
   *   <li>Retrieves existing collections or creates new collections depending on the assignment
   *       mode.
   *   <li>Creates missing figurine-collection relationships.
   *   <li>Increases the quantity when the figurine already exists in a collection.
   * </ul>
   *
   * @param collectorId identifier of the collector performing the assignment
   * @param request assignment request containing figurines, collections, and assignment mode
   * @throws FigurineNotFoundException if any requested figurine does not exist
   * @throws CollectorNotFoundException if the collector does not exist
   * @throws CollectionNotFoundException if an existing collection cannot be found
   * @throws CollectionAlreadyExistsException if creating a collection with an existing name
   */
  public void assignFigurinesToCollections(Long collectorId, @Valid AssignFigurinesReq request) {
    List<Figurine> existingFigurines = retrieveExistingFigurines(request.figurineIds());
    List<CollectorCollection> existingCollections =
        retrieveExistingCollections(
            request.collectionMode(), collectorId, request.collectionIds(), request.collection());

    for (CollectorCollection existingCollection : existingCollections) {
      for (Figurine existingFigurine : existingFigurines) {
        collectorCollectionFigurineRepository
            .findByCollectionAndFigurine(existingCollection, existingFigurine)
            .ifPresentOrElse(
                existing -> {
                  int currentTotal = existing.getQuantity();
                  currentTotal++;
                  existing.setQuantity(currentTotal);
                  collectorCollectionFigurineRepository.save(existing);
                  log.info(
                      "This figurine is already in the collection [{}] - '{}'. It wont be added again, but increase "
                          + "the number of figurines, currently there are {} for figurine [{}] - {}",
                      existingCollection.getId(),
                      existingCollection.getName(),
                      currentTotal,
                      existing.getFigurine().getId(),
                      existing.getFigurine().getNormalizedName());
                },
                () -> {
                  CollectorCollectionFigurine collectionFigurine =
                      buildCollectionFigurine(existingCollection, existingFigurine);
                  collectorCollectionFigurineRepository.save(collectionFigurine);
                  log.info(
                      "Added figurine [{}] - '{}' to the collection [{}] - '{}'",
                      collectionFigurine.getFigurine().getId(),
                      collectionFigurine.getFigurine().getNormalizedName(),
                      existingCollection.getId(),
                      existingCollection.getName());
                });
      }
    }
  }

  /**
   * Retrieves all figurines available for a collector collection.
   *
   * <p>The response includes figurine information together with collection-specific ownership data,
   * such as whether the figurine exists in the collection and the owned quantity.
   *
   * <p>The collection must belong to the authenticated collector.
   *
   * @param collectorId identifier of the collector
   * @param collectionId identifier of the collection
   * @return list of figurines with collection ownership details
   * @throws CollectorNotFoundException if the collector does not exist
   * @throws CollectionNotFoundException if the collection does not exist or does not belong to the
   *     collector
   */
  @Transactional(readOnly = true)
  public List<CollectorCollectionFigurineResp> retrieveCollectionFigurines(
      @Positive Long collectorId, @Positive Long collectionId) {

    var collectorFound =
        collectorRepository
            .findById(collectorId)
            .orElseThrow(() -> new CollectorNotFoundException(collectorId));

    var collectionFound =
        collectorCollectionRepository
            .findById(collectionId)
            .orElseThrow(() -> new CollectionNotFoundException(collectionId));

    // make sure this collector owns the collection to be retrieved.
    collectorFound.getCollections().stream()
        .filter(c -> c.getId().equals(collectionId))
        .findFirst()
        .orElseThrow(() -> new CollectionNotFoundException(collectionId));

    FigurineFilter figurineFilter =
        FigurineFilterFactory.build(
            List.of(), null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null);

    return figurineRepository.findAll(figurineFilter).stream()
        .filter(
            figurine -> {
              ReleaseStatus releaseStatus = figurineService.calculateReleaseStatus(figurine);
              return releaseStatus == RELEASED || releaseStatus == ANNOUNCED;
            })
        .map(
            figurine -> {
              boolean isCollected = false;
              int ownedQuantity = 0;

              for (CollectorCollectionFigurine collectorCollectionFigurine :
                  collectionFound.getFigurines()) {
                if (figurine.getId().equals(collectorCollectionFigurine.getFigurine().getId())) {
                  isCollected = true;
                  ownedQuantity = collectorCollectionFigurine.getQuantity();
                  break;
                }
              }

              ReleaseStatus releaseStatus = figurineService.calculateReleaseStatus(figurine);
              int year = figurine.getDistributors().getFirst().getReleaseDate().getYear();

              return collectorMapper.toCollectorCollectionFigurineResp(
                  figurine, releaseStatus, isCollected, ownedQuantity, year);
            })
        .toList();
  }

  /**
   * Retrieves detailed information about a specific figurine assigned to a collection.
   *
   * @param collectorId identifier of the collector
   * @param collectionId identifier of the collection
   * @param figurineId identifier of the figurine
   * @return detailed figurine information
   * @throws FigurineNotFoundException if the figurine does not exist
   */
  @Transactional(readOnly = true)
  public CollectorCollectionFigurineDetailResp retrieveCollectionFigurine(
      @Positive Long collectorId, @Positive Long collectionId, @Positive Long figurineId) {

    var collectorFound =
        collectorRepository
            .findById(collectorId)
            .orElseThrow(() -> new CollectorNotFoundException(collectorId));

    var collectionFound =
        collectorCollectionRepository
            .findById(collectionId)
            .orElseThrow(() -> new CollectionNotFoundException(collectionId));

    // make sure this collector owns the collection to be retrieved.
    collectorFound.getCollections().stream()
        .filter(c -> c.getId().equals(collectionId))
        .findFirst()
        .orElseThrow(() -> new CollectionNotFoundException(collectionId));

    var figurineFound =
        figurineRepository
            .findById(figurineId)
            .orElseThrow(() -> new FigurineNotFoundException(figurineId));

    return collectorMapper.toCollectorCollectionFigurineDetailResp(
        figurineFound,
        figurineService::createDisplayableName,
        figurineService::calculatePriceWithTax);
  }

  /**
   * Deletes a collector collection and all figurine assignments associated with it.
   *
   * <p>The collection must belong to the specified collector.
   *
   * @param collectorId identifier of the collector
   * @param collectionId identifier of the collection to delete
   * @throws CollectorNotFoundException if the collector does not exist
   * @throws CollectionNotFoundException if the collection does not exist or does not belong to the
   *     collector
   */
  @Transactional
  public void deleteCollectionFigurine(
      @Positive Long collectorId, @Positive Long collectionId, @Positive Long figurineId) {

    var collectorFound =
        collectorRepository
            .findById(collectorId)
            .orElseThrow(() -> new CollectorNotFoundException(collectorId));

    var collectionFound =
        collectorCollectionRepository
            .findById(collectionId)
            .orElseThrow(() -> new CollectionNotFoundException(collectionId));

    // make sure this collector owns the collection to be deleted.
    collectorFound.getCollections().stream()
        .filter(c -> c.getId().equals(collectionId))
        .findFirst()
        .orElseThrow(() -> new CollectionNotFoundException(collectionId));

    var figurineFound =
        figurineRepository
            .findById(figurineId)
            .orElseThrow(() -> new FigurineNotFoundException(figurineId));

    collectorCollectionFigurineRepository
        .findByCollectionAndFigurine(collectionFound, figurineFound)
        .ifPresent(
            ccf -> {
              collectorCollectionFigurineRepository.delete(ccf);
              log.info(
                  "Deleted figurine [{}] - '{}' from collection [{}] - '{}'",
                  figurineFound.getId(),
                  figurineFound.getNormalizedName(),
                  collectionFound.getId(),
                  collectionFound.getName());
            });
  }

  /**
   * Retrieves all collections associated with a collector.
   *
   * @param collectorId identifier of the collector
   * @return list of collector collections
   * @throws CollectorNotFoundException if the collector does not exist
   */
  public List<CollectorCollectionResp> retrieveCollections(Long collectorId) {
    Collector collectorFound =
        collectorRepository
            .findById(collectorId)
            .orElseThrow(() -> new CollectorNotFoundException(collectorId));

    // finds all the collections from the collector and maps them to the response DTOs
    List<CollectorCollection> collectorCollection =
        collectorCollectionRepository.findByCollector(collectorFound);

    return collectorCollection.stream().map(collectorMapper::toCollectorCollectionResp).toList();
  }

  @Transactional
  public void deleteCollection(Long collectorId, Long collectionId) {
    Collector collectorFound =
        collectorRepository
            .findById(collectorId)
            .orElseThrow(() -> new CollectorNotFoundException(collectorId));

    // make sure this collection owns the collection to be removed.
    collectorFound.getCollections().stream()
        .filter(c -> c.getId().equals(collectionId))
        .findFirst()
        .orElseThrow(() -> new CollectionNotFoundException(collectionId));

    // deletes all the figurines associated with the collection.
    int deletedCount =
        collectorCollectionFigurineRepository.deleteByCollectionIdAndCollectorId(
            collectionId, collectorId);

    // Now it deletes the collection itself
    if (!collectorCollectionRepository.existsById(collectionId)) {
      throw new CollectionNotFoundException(collectionId);
    }
    collectorCollectionRepository.deleteCollectionById(collectionId);

    log.info("{} items deleted in collection with id {}", deletedCount, collectionId);
  }

  /**
   * Updates the metadata of an existing collector collection.
   *
   * <p>The collection ownership is validated before applying updates.
   *
   * @param collectorId identifier of the collector
   * @param collectionId identifier of the collection to update
   * @param request updated collection information
   * @return updated collection response
   * @throws CollectorNotFoundException if the collector does not exist
   * @throws CollectionNotFoundException if the collection does not exist or does not belong to the
   *     collector
   */
  @Transactional
  public CollectorCollectionResp updateCollection(
      @Positive Long collectorId,
      @Positive Long collectionId,
      @NotNull @Valid CollectorCollectionReq request) {
    log.info("Updating collection with id '{}'. New name: '{}'", collectionId, request.name());
    Collector collectorFound =
        collectorRepository
            .findById(collectorId)
            .orElseThrow(() -> new CollectorNotFoundException(collectorId));

    // make sure this collection owns the collection to be removed.
    collectorFound.getCollections().stream()
        .filter(c -> c.getId().equals(collectionId))
        .findFirst()
        .orElseThrow(() -> new CollectionNotFoundException(collectionId));

    var existing =
        collectorCollectionRepository
            .findById(collectionId)
            .orElseThrow(() -> new CollectionNotFoundException(collectionId));

    // No need to use MapStruct when properties are too simple. Just update them directly.
    existing.setName(request.name());
    existing.setDescription(request.description());

    var updated = collectorCollectionRepository.save(existing);

    return new CollectorCollectionResp(
        updated.getId(), updated.getName(), updated.getDescription(), 0, List.of());
  }

  private List<Figurine> retrieveExistingFigurines(List<Long> figurineIds) {
    // if any of the passed figurineIds, then report it.
    return figurineIds.stream()
        .map(
            figurineId ->
                figurineRepository
                    .findById(figurineId)
                    .orElseThrow(() -> new FigurineNotFoundException(figurineId)))
        .toList();
  }

  private List<CollectorCollection> retrieveExistingCollections(
      CollectionAssignmentMode mode,
      Long collectorId,
      List<Long> collectionIds,
      CollectorCollectionReq collectionReq) {
    List<CollectorCollection> existingCollections = new ArrayList<>();
    if (mode == CollectionAssignmentMode.AUTO) {
      existingCollections.add(createDefaultCollection(collectorId));
    } else if (mode == CollectionAssignmentMode.CREATE) {
      existingCollections.add(
          createCollection(collectorId, collectionReq.name(), collectionReq.description()));
    } else if (mode == CollectionAssignmentMode.EXISTING) {
      existingCollections.addAll(
          collectionIds.stream()
              .map(
                  collectionId ->
                      collectorCollectionRepository
                          .findById(collectionId)
                          .orElseThrow(() -> new CollectionNotFoundException(collectionId)))
              .toList());
    } else {
      throw new IllegalArgumentException("Unsupported collection assignment mode: " + mode);
    }
    return existingCollections;
  }

  private CollectorCollection createDefaultCollection(Long collectorId) {
    return createCollection(collectorId, "My Myth Collection", null);
  }

  /**
   * Creates a new collection for the specified collector.
   *
   * <p>The collection name must be unique across collections. If another collection already exists
   * with the same name, creation fails.
   *
   * @param collectorId identifier of the collector owning the collection
   * @param name collection name
   * @param description optional collection description
   * @return newly created collection entity
   * @throws CollectorNotFoundException if the collector does not exist
   * @throws CollectionAlreadyExistsException if a collection with the same name already exists
   */
  private CollectorCollection createCollection(Long collectorId, String name, String description) {
    Collector collector =
        collectorRepository
            .findById(collectorId)
            .orElseThrow(() -> new CollectorNotFoundException(collectorId));

    collectorCollectionRepository
        .findByName(name)
        .ifPresent(
            existing -> {
              throw new CollectionAlreadyExistsException(existing.getName());
            });

    CollectorCollection collectorCollection = new CollectorCollection();
    collectorCollection.setCollector(collector);
    collectorCollection.setName(name);
    collectorCollection.setDescription(description);

    return collectorCollectionRepository.save(collectorCollection);
  }

  private CollectorCollectionFigurine buildCollectionFigurine(
      CollectorCollection collection, Figurine figurine) {
    CollectorCollectionFigurine collectorCollectionFigurine = new CollectorCollectionFigurine();
    collectorCollectionFigurine.setCollection(collection);
    collectorCollectionFigurine.setFigurine(figurine);
    return collectorCollectionFigurine;
  }
}
