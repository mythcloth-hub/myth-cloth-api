package com.mesofi.mythclothapi.collectorscollections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionFigurineRepository;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionRepository;
import com.mesofi.mythclothapi.figurines.exceptions.FigurineNotFoundException;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;

@ExtendWith(MockitoExtension.class)
class CollectorCollectionFigurineServiceTest {

  @InjectMocks private CollectorCollectionFigurineService service;

  @Mock private CollectorCollectionFigurineRepository collectorCollectionFigurineRepository;
  @Mock private CollectorCollectionRepository collectorCollectionRepository;
  @Mock private CollectorRepository collectorRepository;
  @Mock private FigurineRepository figurineRepository;
  @Mock private CollectorMapper collectorMapper;

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
    verifyNoInteractions(collectorRepository);
  }

  @Test
  void
      addFigurineToCollection_shouldThrowIllegalArgumentException_whenFigurineAlreadyInCollection() {
    Figurine figurine = figurine(10L);
    CollectorCollection collection =
        collection(20L, null, "Existing Collection", "Existing Collection");
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
    CollectorCollection collection =
        collection(20L, null, "Existing Collection", "Existing Collection");

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
    assertThat(persisted.getTotalFigurines()).isEqualTo(1);
  }

  @Test
  void addFigurineToCollection_shouldCreateDefaultCollectionAndLink_whenCollectionDoesNotExist() {
    Figurine figurine = figurine(10L);
    Collector collector = collector(20L);

    when(figurineRepository.findById(10L)).thenReturn(Optional.of(figurine));
    when(collectorCollectionRepository.findById(20L)).thenReturn(Optional.empty());
    when(collectorRepository.findById(20L)).thenReturn(Optional.of(collector));

    CollectorCollection savedCollection = collection(99L, collector, "My Myth Collection", null);
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
    assertThat(createdCollection.getName()).isEqualTo("My Myth Collection");
    assertThat(createdCollection.getDescription()).isNull();

    ArgumentCaptor<CollectorCollectionFigurine> linkCaptor =
        ArgumentCaptor.forClass(CollectorCollectionFigurine.class);
    verify(collectorCollectionFigurineRepository).save(linkCaptor.capture());
    assertThat(linkCaptor.getValue().getCollection()).isEqualTo(savedCollection);
    assertThat(linkCaptor.getValue().getFigurine()).isEqualTo(figurine);
  }

  @Test
  void
      addFigurineToCollection_shouldThrowCollectorNotFoundException_whenDefaultCollectionOwnerDoesNotExist() {
    Figurine figurine = figurine(10L);

    when(figurineRepository.findById(10L)).thenReturn(Optional.of(figurine));
    when(collectorCollectionRepository.findById(20L)).thenReturn(Optional.empty());
    when(collectorRepository.findById(20L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.addFigurineToCollection(20L, 10L))
        .isInstanceOf(CollectorNotFoundException.class)
        .hasMessage("Collector with id 20 was not found");

    verify(collectorCollectionRepository, never()).save(any(CollectorCollection.class));
    verify(collectorCollectionFigurineRepository, never())
        .save(any(CollectorCollectionFigurine.class));
  }

  @Test
  void assignFigurinesToCollections_shouldSaveLinks_whenModeIsAuto() {
    Figurine figurine = figurine(10L);
    Collector collector = collector(1L);
    CollectorCollection collection = collection(20L, collector, "My Myth Collection", null);
    AssignFigurinesReq request =
        new AssignFigurinesReq(List.of(10L), CollectionAssignmentMode.AUTO, List.of(), null);

    when(figurineRepository.findById(10L)).thenReturn(Optional.of(figurine));
    when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
    when(collectorCollectionRepository.findByName("My Myth Collection"))
        .thenReturn(Optional.empty());
    when(collectorCollectionRepository.save(any(CollectorCollection.class))).thenReturn(collection);
    when(collectorCollectionFigurineRepository.findByCollectionAndFigurine(collection, figurine))
        .thenReturn(Optional.empty());
    when(collectorCollectionFigurineRepository.save(any(CollectorCollectionFigurine.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    service.assignFigurinesToCollections(1L, request);

    ArgumentCaptor<CollectorCollection> collectionCaptor =
        ArgumentCaptor.forClass(CollectorCollection.class);
    verify(collectorCollectionRepository).save(collectionCaptor.capture());
    assertThat(collectionCaptor.getValue().getCollector()).isEqualTo(collector);
    assertThat(collectionCaptor.getValue().getName()).isEqualTo("My Myth Collection");
    assertThat(collectionCaptor.getValue().getDescription()).isNull();

    ArgumentCaptor<CollectorCollectionFigurine> linkCaptor =
        ArgumentCaptor.forClass(CollectorCollectionFigurine.class);
    verify(collectorCollectionFigurineRepository).save(linkCaptor.capture());
    assertThat(linkCaptor.getValue().getCollection()).isEqualTo(collection);
    assertThat(linkCaptor.getValue().getFigurine()).isEqualTo(figurine);
    assertThat(linkCaptor.getValue().getTotalFigurines()).isEqualTo(1);
  }

  @Test
  void assignFigurinesToCollections_shouldIncrementExistingLink_whenLinkAlreadyExists() {
    Figurine figurine = figurine(10L);
    Collector collector = collector(1L);
    CollectorCollection collection = collection(20L, collector, "Existing", null);
    CollectorCollectionFigurine existingLink = link(collection, figurine);
    existingLink.setTotalFigurines(2);
    AssignFigurinesReq request =
        new AssignFigurinesReq(List.of(10L), CollectionAssignmentMode.EXISTING, List.of(20L), null);

    when(figurineRepository.findById(10L)).thenReturn(Optional.of(figurine));
    when(collectorCollectionRepository.findById(20L)).thenReturn(Optional.of(collection));
    when(collectorCollectionFigurineRepository.findByCollectionAndFigurine(collection, figurine))
        .thenReturn(Optional.of(existingLink));

    service.assignFigurinesToCollections(1L, request);

    ArgumentCaptor<CollectorCollectionFigurine> linkCaptor =
        ArgumentCaptor.forClass(CollectorCollectionFigurine.class);
    verify(collectorCollectionFigurineRepository).save(linkCaptor.capture());
    assertThat(linkCaptor.getValue()).isSameAs(existingLink);
    assertThat(linkCaptor.getValue().getTotalFigurines()).isEqualTo(3);
  }

  @Test
  void assignFigurinesToCollections_shouldCreateCollection_whenModeIsCreate() {
    Figurine figurine = figurine(10L);
    Collector collector = collector(1L);
    CollectorCollection collection = collection(20L, collector, "New Collection", "Description");
    AssignFigurinesReq request =
        new AssignFigurinesReq(
            List.of(10L),
            CollectionAssignmentMode.CREATE,
            List.of(),
            new CollectorCollectionReq("New Collection", "Description"));

    when(figurineRepository.findById(10L)).thenReturn(Optional.of(figurine));
    when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
    when(collectorCollectionRepository.findByName("New Collection")).thenReturn(Optional.empty());
    when(collectorCollectionRepository.save(any(CollectorCollection.class))).thenReturn(collection);
    when(collectorCollectionFigurineRepository.findByCollectionAndFigurine(collection, figurine))
        .thenReturn(Optional.empty());
    when(collectorCollectionFigurineRepository.save(any(CollectorCollectionFigurine.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    service.assignFigurinesToCollections(1L, request);

    ArgumentCaptor<CollectorCollection> collectionCaptor =
        ArgumentCaptor.forClass(CollectorCollection.class);
    verify(collectorCollectionRepository).save(collectionCaptor.capture());
    assertThat(collectionCaptor.getValue().getCollector()).isEqualTo(collector);
    assertThat(collectionCaptor.getValue().getName()).isEqualTo("New Collection");
    assertThat(collectionCaptor.getValue().getDescription()).isEqualTo("Description");
  }

  @Test
  void
      assignFigurinesToCollections_shouldThrowCollectionAlreadyExistsException_whenCollectionNameExists() {
    Figurine figurine = figurine(10L);
    Collector collector = collector(1L);
    AssignFigurinesReq request =
        new AssignFigurinesReq(
            List.of(10L),
            CollectionAssignmentMode.CREATE,
            List.of(),
            new CollectorCollectionReq("Existing Collection", "Description"));
    CollectorCollection existing = collection(20L, collector, "Existing Collection", "Description");

    when(figurineRepository.findById(10L)).thenReturn(Optional.of(figurine));
    when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
    when(collectorCollectionRepository.findByName("Existing Collection"))
        .thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> service.assignFigurinesToCollections(1L, request))
        .isInstanceOf(CollectionAlreadyExistsException.class)
        .hasMessage("Collection with name 'Existing Collection' already exists");
  }

  @Test
  void assignFigurinesToCollections_shouldThrowCollectorNotFoundException_whenCollectorMissing() {
    Figurine figurine = figurine(10L);
    AssignFigurinesReq request =
        new AssignFigurinesReq(List.of(10L), CollectionAssignmentMode.AUTO, List.of(), null);

    when(figurineRepository.findById(10L)).thenReturn(Optional.of(figurine));
    when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.assignFigurinesToCollections(1L, request))
        .isInstanceOf(CollectorNotFoundException.class)
        .hasMessage("Collector with id 1 was not found");
  }

  @Test
  void
      assignFigurinesToCollections_shouldThrowCollectionNotFoundException_whenExistingCollectionMissing() {
    Figurine figurine = figurine(10L);
    AssignFigurinesReq request =
        new AssignFigurinesReq(List.of(10L), CollectionAssignmentMode.EXISTING, List.of(20L), null);

    when(figurineRepository.findById(10L)).thenReturn(Optional.of(figurine));
    when(collectorCollectionRepository.findById(20L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.assignFigurinesToCollections(1L, request))
        .isInstanceOf(CollectionNotFoundException.class)
        .hasMessage("Collection with id 20 was not found");
  }

  @Test
  void assignFigurinesToCollections_shouldThrowFigurineNotFoundException_whenFigurineMissing() {
    AssignFigurinesReq request =
        new AssignFigurinesReq(List.of(10L), CollectionAssignmentMode.AUTO, List.of(), null);

    when(figurineRepository.findById(10L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.assignFigurinesToCollections(1L, request))
        .isInstanceOf(FigurineNotFoundException.class)
        .hasMessage("Figurine not found");
  }

  @Test
  void assignFigurinesToCollections_shouldThrowIllegalArgumentException_whenModeIsUnsupported() {
    Figurine figurine = figurine(10L);
    AssignFigurinesReq request = new AssignFigurinesReq(List.of(10L), null, List.of(), null);

    when(figurineRepository.findById(10L)).thenReturn(Optional.of(figurine));

    assertThatThrownBy(() -> service.assignFigurinesToCollections(1L, request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unsupported collection assignment mode: null");
  }

  @Test
  void retrieveCollections_shouldReturnMappedCollections_whenCollectorExists() {
    Collector collector = collector(1L);
    CollectorCollection collection = collection(20L, collector, "Collection", "Desc");
    CollectorCollectionResp response =
        new CollectorCollectionResp(20L, "Collection", "Desc", 1, List.of(10L));

    when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
    when(collectorCollectionRepository.findByCollector(collector)).thenReturn(List.of(collection));
    when(collectorMapper.toCollectorCollectionResp(collection)).thenReturn(response);

    List<CollectorCollectionResp> result = service.retrieveCollections(1L);

    assertThat(result).containsExactly(response);
    verify(collectorMapper).toCollectorCollectionResp(collection);
  }

  @Test
  void retrieveCollections_shouldThrowCollectorNotFoundException_whenCollectorMissing() {
    when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.retrieveCollections(1L))
        .isInstanceOf(CollectorNotFoundException.class)
        .hasMessage("Collector with id 1 was not found");
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

  private CollectorCollection collection(
      Long id, Collector collector, String name, String description) {
    CollectorCollection collection = new CollectorCollection();
    collection.setId(id);
    collection.setCollector(collector);
    collection.setName(name);
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
