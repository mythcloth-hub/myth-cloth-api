package com.mesofi.mythclothapi.collectorscollections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.collectors.CollectorRepository;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionFigurineRepository;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionRepository;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;

@ExtendWith(MockitoExtension.class)
class CollectorCollectionFigurineServiceTest {

  @InjectMocks private CollectorCollectionFigurineService service;

  @Mock private CollectorCollectionFigurineRepository collectorCollectionFigurineRepository;
  @Mock private CollectorCollectionRepository collectorCollectionRepository;
  @Mock private CollectorRepository collectorRepository;
  @Mock private FigurineRepository figurineRepository;

  @Test
  void addFigurineToCollection_shouldThrowIllegalArgumentException_whenFigurineDoesNotExist() {
    when(figurineRepository.findById(10L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.addFigurineToCollection(20L, 10L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Figurine with id 10 not found");

    verify(figurineRepository).findById(10L);
    verify(collectorCollectionRepository, never()).findById(20L);
    verify(collectorCollectionFigurineRepository, never())
        .findByCollectionAndFigurine(any(), any());
    verify(collectorCollectionFigurineRepository, never()).save(any());
  }

  @Test
  void
      addFigurineToCollection_shouldThrowIllegalArgumentException_whenFigurineAlreadyInCollection() {
    Figurine figurine = figurine(10L);
    CollectorCollection collection = collection(20L, null, "Existing Collection");
    CollectorCollectionFigurine existingLink = link(collection, figurine);

    when(figurineRepository.findById(10L)).thenReturn(Optional.of(figurine));
    when(collectorCollectionRepository.findById(20L)).thenReturn(Optional.of(collection));
    when(collectorCollectionFigurineRepository.findByCollectionAndFigurine(collection, figurine))
        .thenReturn(Optional.of(existingLink));

    assertThatThrownBy(() -> service.addFigurineToCollection(20L, 10L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Figurine with id 10 already in collection with id 20");

    verify(collectorCollectionFigurineRepository, never()).save(any());
  }

  @Test
  void addFigurineToCollection_shouldCreateLink_whenCollectionExistsAndFigurineIsNotPresent() {
    Figurine figurine = figurine(10L);
    CollectorCollection collection = collection(20L, null, "Existing Collection");

    when(figurineRepository.findById(10L)).thenReturn(Optional.of(figurine));
    when(collectorCollectionRepository.findById(20L)).thenReturn(Optional.of(collection));
    when(collectorCollectionFigurineRepository.findByCollectionAndFigurine(collection, figurine))
        .thenReturn(Optional.empty());
    when(collectorCollectionFigurineRepository.save(any(CollectorCollectionFigurine.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    service.addFigurineToCollection(20L, 10L);

    ArgumentCaptor<CollectorCollectionFigurine> captor =
        ArgumentCaptor.forClass(CollectorCollectionFigurine.class);
    verify(collectorCollectionFigurineRepository).save(captor.capture());

    CollectorCollectionFigurine persisted = captor.getValue();
    assertThat(persisted.getCollection()).isEqualTo(collection);
    assertThat(persisted.getFigurine()).isEqualTo(figurine);
  }

  @Test
  void addFigurineToCollection_shouldCreateDefaultCollectionAndLink_whenCollectionDoesNotExist() {
    Figurine figurine = figurine(10L);
    Collector collector = collector(1L);

    when(figurineRepository.findById(10L)).thenReturn(Optional.of(figurine));
    when(collectorCollectionRepository.findById(20L)).thenReturn(Optional.empty());
    when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));

    CollectorCollection savedCollection = collection(99L, collector, "My Myth Collection");
    when(collectorCollectionRepository.save(any(CollectorCollection.class)))
        .thenReturn(savedCollection);

    when(collectorCollectionFigurineRepository.findByCollectionAndFigurine(
            savedCollection, figurine))
        .thenReturn(Optional.empty());
    when(collectorCollectionFigurineRepository.save(any(CollectorCollectionFigurine.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    service.addFigurineToCollection(20L, 10L);

    ArgumentCaptor<CollectorCollection> collectionCaptor =
        ArgumentCaptor.forClass(CollectorCollection.class);
    verify(collectorCollectionRepository).save(collectionCaptor.capture());

    CollectorCollection createdCollection = collectionCaptor.getValue();
    assertThat(createdCollection.getCollector()).isEqualTo(collector);
    assertThat(createdCollection.getDescription()).isEqualTo("My Myth Collection");

    ArgumentCaptor<CollectorCollectionFigurine> linkCaptor =
        ArgumentCaptor.forClass(CollectorCollectionFigurine.class);
    verify(collectorCollectionFigurineRepository).save(linkCaptor.capture());
    assertThat(linkCaptor.getValue().getCollection()).isEqualTo(savedCollection);
    assertThat(linkCaptor.getValue().getFigurine()).isEqualTo(figurine);
  }

  @Test
  void
      addFigurineToCollection_shouldThrowIllegalArgumentException_whenDefaultCollectorDoesNotExist() {
    Figurine figurine = figurine(10L);

    when(figurineRepository.findById(10L)).thenReturn(Optional.of(figurine));
    when(collectorCollectionRepository.findById(20L)).thenReturn(Optional.empty());
    when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.addFigurineToCollection(20L, 10L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Collector with id 1 not found");

    verify(collectorCollectionRepository, never()).save(any(CollectorCollection.class));
    verify(collectorCollectionFigurineRepository, never())
        .save(any(CollectorCollectionFigurine.class));
  }

  private Figurine figurine(Long id) {
    Figurine figurine = new Figurine();
    figurine.setId(id);
    return figurine;
  }

  private Collector collector(Long id) {
    Collector collector = new Collector();
    collector.setId(id);
    return collector;
  }

  private CollectorCollection collection(Long id, Collector collector, String description) {
    CollectorCollection collection = new CollectorCollection();
    collection.setId(id);
    collection.setCollector(collector);
    collection.setDescription(description);
    return collection;
  }

  private CollectorCollectionFigurine link(CollectorCollection collection, Figurine figurine) {
    CollectorCollectionFigurine link = new CollectorCollectionFigurine();
    link.setCollection(collection);
    link.setFigurine(figurine);
    return link;
  }
}
