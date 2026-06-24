package com.mesofi.mythclothapi.collectorscollections;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.collectors.CollectorRepository;
import com.mesofi.mythclothapi.collectors.exceptions.CollectorNotFoundException;
import com.mesofi.mythclothapi.collectors.mapper.CollectorMapper;
import com.mesofi.mythclothapi.collectorscollections.dto.AssignFigurinesReq;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectionAssignmentMode;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionReq;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionResp;
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectionAlreadyExistsException;
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectionNotFoundException;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionFigurineRepository;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionRepository;
import com.mesofi.mythclothapi.figurines.exceptions.FigurineNotFoundException;
import com.mesofi.mythclothapi.figurines.model.Figurine;
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
  private final CollectorMapper collectorMapper;

  /**
   * Adds a single figurine to a specific collection.
   *
   * <p>This method is deprecated because it only supports assigning one figurine to one collection.
   * Use {@link #assignFigurinesToCollections(Long, AssignFigurinesReq)} instead, which provides a
   * unified workflow supporting multiple figurines, multiple collections, and collection creation
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
   *   <li>Retrieves and validates all requested figurines.
   *   <li>Retrieves existing collections or creates a new collection depending on the assignment
   *       mode.
   *   <li>Creates missing figurine-collection relationships.
   *   <li>Updates existing relationships when the figurine is already assigned.
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
                  int currentTotal = existing.getTotalFigurines();
                  currentTotal++;
                  existing.setTotalFigurines(currentTotal);
                  collectorCollectionFigurineRepository.save(existing);
                },
                () ->
                    collectorCollectionFigurineRepository.save(
                        buildCollectionFigurine(existingCollection, existingFigurine)));
      }
    }
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

    List<CollectorCollection> collectorCollection =
        collectorCollectionRepository.findByCollector(collectorFound);

    return collectorCollection.stream().map(collectorMapper::toCollectorCollectionResp).toList();
  }

  private CollectorCollection createDefaultCollection(Long collectorId) {
    return createCollection(collectorId, "My Myth Collection", null);
  }

  /**
   * Creates a new collection for the specified collector.
   *
   * <p>The collection name must be unique. If another collection already exists with the same name,
   * the operation fails.
   *
   * @param collectorId identifier of the collector owning the collection
   * @param name collection name
   * @param description optional collection description
   * @return the created collection entity
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
