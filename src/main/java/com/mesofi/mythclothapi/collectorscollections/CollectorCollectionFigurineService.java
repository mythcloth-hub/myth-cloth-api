package com.mesofi.mythclothapi.collectorscollections;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.collectors.CollectorRepository;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionFigurineRepository;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionRepository;
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
            .orElseGet(this::createDefaultCollection);

    collectorCollectionFigurineRepository
        .findByCollectionAndFigurine(existingCollection, existingFigurine)
        .<CollectorCollectionFigurine>map(
            ignored -> {
              throw new IllegalArgumentException(
                  "Figurine with id "
                      + figurineId
                      + " already in collection with id "
                      + collectionId);
            })
        .orElseGet(
            () ->
                collectorCollectionFigurineRepository.save(
                    buildCollectionFigurine(existingCollection, existingFigurine)));
  }

  private CollectorCollection createDefaultCollection() {
    Collector collector =
        collectorRepository
            .findById(1L)
            .orElseThrow(() -> new IllegalArgumentException("Collector with id 1 not found"));

    CollectorCollection collectorCollection = new CollectorCollection();
    collectorCollection.setCollector(collector);
    collectorCollection.setDescription("My Myth Collection");

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
