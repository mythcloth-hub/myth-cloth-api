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
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectionNotFoundException;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionFigurineRepository;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionRepository;
import com.mesofi.mythclothapi.figurines.exceptions.FigurineNotFoundException;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

  public void assignFigurinesToCollections(Long collectorId, @Valid AssignFigurinesReq request) {
    List<Figurine> existingFigurines = retrieveExistingFigurines(request.figurineIds());
    List<CollectorCollection> existingCollections =
        retrieveExistingCollections(
            request.collectionMode(), collectorId, request.collectionIds(), request.collection());

    // validates that figurine is not in the same collection

    /*
    if (collectorCollectionFigurineRepository
        .findByCollectionAndFigurine(existingCollection, existingFigurine)
        .isPresent()) {
      throw new IllegalArgumentException(
          "Figurine with id " + figurineId + " already in collection with id " + collectionId);
    }

    collectorCollectionFigurineRepository.save(
        buildCollectionFigurine(existingCollection, existingFigurine));
     */

    for (CollectorCollection existingCollection : existingCollections) {
      for (Figurine existingFigurine : existingFigurines) {
        collectorCollectionFigurineRepository.save(
            buildCollectionFigurine(existingCollection, existingFigurine));
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

  private CollectorCollection createCollection(Long collectorId, String name, String description) {
    Collector collector =
        collectorRepository
            .findById(collectorId)
            .orElseThrow(() -> new CollectorNotFoundException(collectorId));

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
