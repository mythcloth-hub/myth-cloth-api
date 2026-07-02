package com.mesofi.mythclothapi.collectorscollections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
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
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionFigurineDetailResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionFigurineResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionReq;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionResp;
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectionAlreadyExistsException;
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectionNotFoundException;
import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionFigurineRepository;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionRepository;
import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.distributors.model.Distributor;
import com.mesofi.mythclothapi.distributors.model.DistributorName;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurines.FigurineFilter;
import com.mesofi.mythclothapi.figurines.FigurineService;
import com.mesofi.mythclothapi.figurines.exceptions.FigurineNotFoundException;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;

@ExtendWith(MockitoExtension.class)
class CollectorCollectionFigurineServiceTest {

  @InjectMocks private CollectorCollectionFigurineService service;

  @Mock private CollectorCollectionFigurineRepository collectorCollectionFigurineRepository;
  @Mock private CollectorCollectionRepository collectorCollectionRepository;
  @Mock private CollectorRepository collectorRepository;
  @Mock private FigurineRepository figurineRepository;
  @Mock private FigurineService figurineService;
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
    assertThat(persisted.getQuantity()).isEqualTo(1);
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
    assertThat(linkCaptor.getValue().getQuantity()).isEqualTo(1);
  }

  @Test
  void assignFigurinesToCollections_shouldIncrementExistingLink_whenLinkAlreadyExists() {
    Figurine figurine = figurine(10L);
    Collector collector = collector(1L);
    CollectorCollection collection = collection(20L, collector, "Existing", null);
    CollectorCollectionFigurine existingLink = link(collection, figurine);
    existingLink.setQuantity(2);
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
    assertThat(linkCaptor.getValue().getQuantity()).isEqualTo(3);
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

  @Test
  void retrieveCollectionFigurines_shouldReturnMappedFigurines_whenCollectorOwnsCollection() {
    Collector collector = collectorWithCollections(1L);
    CollectorCollection collection = collection(20L, collector, "Collection", "Desc");
    collector.setCollections(new ArrayList<>(List.of(collection)));

    Figurine releasedFigurine = figurineWithReleaseDate(10L, "Seiya", LocalDate.of(1991, 4, 20));
    Figurine announcedFigurine = figurineWithReleaseDate(11L, "Shiryu", LocalDate.of(1992, 5, 21));
    Figurine unreleasedFigurine = figurineWithReleaseDate(12L, "Hyoga", LocalDate.of(1993, 6, 22));
    CollectorCollectionFigurine ownedLink = link(collection, releasedFigurine);
    ownedLink.setQuantity(2);
    collection.getFigurines().add(ownedLink);

    CollectorCollectionFigurineResp response =
        new CollectorCollectionFigurineResp(
            10L, "Seiya", ReleaseStatus.RELEASED, null, null, true, 2, 1991);
    CollectorCollectionFigurineResp announcedResponse =
        new CollectorCollectionFigurineResp(
            11L, "Shiryu", ReleaseStatus.ANNOUNCED, null, null, false, 0, 1992);

    when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
    when(collectorCollectionRepository.findById(20L)).thenReturn(Optional.of(collection));
    when(figurineRepository.findAll(any(FigurineFilter.class)))
        .thenReturn(List.of(releasedFigurine, announcedFigurine, unreleasedFigurine));
    when(figurineService.calculateReleaseStatus(releasedFigurine))
        .thenReturn(ReleaseStatus.RELEASED, ReleaseStatus.RELEASED);
    when(figurineService.calculateReleaseStatus(announcedFigurine))
        .thenReturn(ReleaseStatus.ANNOUNCED, ReleaseStatus.ANNOUNCED);
    when(figurineService.calculateReleaseStatus(unreleasedFigurine))
        .thenReturn(ReleaseStatus.UNRELEASED);
    when(collectorMapper.toCollectorCollectionFigurineResp(
            eq(releasedFigurine), eq(ReleaseStatus.RELEASED), eq(true), eq(2), eq(1991)))
        .thenReturn(response);
    when(collectorMapper.toCollectorCollectionFigurineResp(
            eq(announcedFigurine), eq(ReleaseStatus.ANNOUNCED), eq(false), eq(0), eq(1992)))
        .thenReturn(announcedResponse);

    List<CollectorCollectionFigurineResp> result = service.retrieveCollectionFigurines(1L, 20L);

    assertThat(result).containsExactly(response, announcedResponse);
    verify(collectorMapper)
        .toCollectorCollectionFigurineResp(releasedFigurine, ReleaseStatus.RELEASED, true, 2, 1991);
    verify(collectorMapper)
        .toCollectorCollectionFigurineResp(
            announcedFigurine, ReleaseStatus.ANNOUNCED, false, 0, 1992);
  }

  @Test
  void
      retrieveCollectionFigurines_shouldThrowCollectionNotFoundException_whenCollectorDoesNotOwnCollection() {
    Collector collector = collectorWithCollections(1L);
    CollectorCollection ownCollection = collection(99L, collector, "Other", null);
    collector.setCollections(new ArrayList<>(List.of(ownCollection)));

    when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
    when(collectorCollectionRepository.findById(20L))
        .thenReturn(Optional.of(collection(20L, collector, "Collection", "Desc")));

    assertThatThrownBy(() -> service.retrieveCollectionFigurines(1L, 20L))
        .isInstanceOf(CollectionNotFoundException.class)
        .hasMessage("Collection with id 20 was not found");
  }

  @Test
  void retrieveCollectionFigurines_shouldThrowCollectorNotFoundException_whenCollectorMissing() {
    when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.retrieveCollectionFigurines(1L, 20L))
        .isInstanceOf(CollectorNotFoundException.class)
        .hasMessage("Collector with id 1 was not found");
  }

  @Test
  void retrieveCollectionFigurines_shouldThrowCollectionNotFoundException_whenCollectionMissing() {
    Collector collector = collectorWithCollections(1L);
    collector.setCollections(new ArrayList<>(List.of(collection(99L, collector, "Other", null))));

    when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
    when(collectorCollectionRepository.findById(20L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.retrieveCollectionFigurines(1L, 20L))
        .isInstanceOf(CollectionNotFoundException.class)
        .hasMessage("Collection with id 20 was not found");
  }

  @Test
  void retrieveCollectionFigurine_shouldReturnMappedFigurine_whenFigurineExists() {
    Collector collector = collectorWithCollections(1L);
    CollectorCollection collection = collection(20L, collector, "Collection", "Desc");
    collector.setCollections(new ArrayList<>(List.of(collection)));

    Figurine figurine = figurine(10L);
    CollectorCollectionFigurineDetailResp response =
        new CollectorCollectionFigurineDetailResp("Seiya SSG", List.of(), null, null, null);

    when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
    when(collectorCollectionRepository.findById(20L)).thenReturn(Optional.of(collection));
    when(figurineRepository.findById(10L)).thenReturn(Optional.of(figurine));
    when(collectorMapper.toCollectorCollectionFigurineDetailResp(eq(figurine), any(), any()))
        .thenReturn(response);

    CollectorCollectionFigurineDetailResp result = service.retrieveCollectionFigurine(1L, 20L, 10L);

    assertThat(result).isEqualTo(response);
  }

  @Test
  void retrieveCollectionFigurine_shouldThrowCollectorNotFoundException_whenCollectorMissing() {
    when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.retrieveCollectionFigurine(1L, 20L, 10L))
        .isInstanceOf(CollectorNotFoundException.class)
        .hasMessage("Collector with id 1 was not found");
  }

  @Test
  void retrieveCollectionFigurine_shouldThrowCollectionNotFoundException_whenCollectionMissing() {
    Collector collector = collectorWithCollections(1L);

    when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
    when(collectorCollectionRepository.findById(20L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.retrieveCollectionFigurine(1L, 20L, 10L))
        .isInstanceOf(CollectionNotFoundException.class)
        .hasMessage("Collection with id 20 was not found");
  }

  @Test
  void
      retrieveCollectionFigurine_shouldThrowCollectionNotFoundException_whenCollectorDoesNotOwnCollection() {
    Collector collector = collectorWithCollections(1L);
    collector.setCollections(new ArrayList<>(List.of(collection(99L, collector, "Other", null))));

    when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
    when(collectorCollectionRepository.findById(20L))
        .thenReturn(Optional.of(collection(20L, collector, "Collection", "Desc")));

    assertThatThrownBy(() -> service.retrieveCollectionFigurine(1L, 20L, 10L))
        .isInstanceOf(CollectionNotFoundException.class)
        .hasMessage("Collection with id 20 was not found");
  }

  @Test
  void retrieveCollectionFigurine_shouldThrowFigurineNotFoundException_whenFigurineMissing() {
    Collector collector = collectorWithCollections(1L);
    CollectorCollection collection = collection(20L, collector, "Collection", "Desc");
    collector.setCollections(new ArrayList<>(List.of(collection)));

    when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
    when(collectorCollectionRepository.findById(20L)).thenReturn(Optional.of(collection));
    when(figurineRepository.findById(10L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.retrieveCollectionFigurine(1L, 20L, 10L))
        .isInstanceOf(FigurineNotFoundException.class)
        .hasMessage("Figurine not found");
  }

  @Test
  void deleteCollectionFigurine_shouldThrowCollectorNotFoundException_whenCollectorMissing() {
    when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.deleteCollectionFigurine(1L, 20L, 10L))
        .isInstanceOf(CollectorNotFoundException.class)
        .hasMessage("Collector with id 1 was not found");
  }

  @Test
  void deleteCollectionFigurine_shouldThrowCollectionNotFoundException_whenCollectionMissing() {
    Collector collector = collectorWithCollections(1L);

    when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
    when(collectorCollectionRepository.findById(20L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.deleteCollectionFigurine(1L, 20L, 10L))
        .isInstanceOf(CollectionNotFoundException.class)
        .hasMessage("Collection with id 20 was not found");
  }

  @Test
  void
      deleteCollectionFigurine_shouldThrowCollectionNotFoundException_whenCollectorDoesNotOwnCollection() {
    Collector collector = collectorWithCollections(1L);
    collector.setCollections(new ArrayList<>(List.of(collection(99L, collector, "Other", null))));
    CollectorCollection collection = collection(20L, collector, "Collection", "Desc");

    when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
    when(collectorCollectionRepository.findById(20L)).thenReturn(Optional.of(collection));

    assertThatThrownBy(() -> service.deleteCollectionFigurine(1L, 20L, 10L))
        .isInstanceOf(CollectionNotFoundException.class)
        .hasMessage("Collection with id 20 was not found");
  }

  @Test
  void deleteCollectionFigurine_shouldThrowFigurineNotFoundException_whenFigurineMissing() {
    Collector collector = collectorWithCollections(1L);
    CollectorCollection collection = collection(20L, collector, "Collection", "Desc");
    collector.setCollections(new ArrayList<>(List.of(collection)));

    when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
    when(collectorCollectionRepository.findById(20L)).thenReturn(Optional.of(collection));
    when(figurineRepository.findById(10L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.deleteCollectionFigurine(1L, 20L, 10L))
        .isInstanceOf(FigurineNotFoundException.class)
        .hasMessage("Figurine not found");
  }

  @Test
  void deleteCollectionFigurine_shouldDeleteLink_whenFigurineExistsInCollection() {
    Collector collector = collectorWithCollections(1L);
    CollectorCollection collection = collection(20L, collector, "Collection", "Desc");
    collector.setCollections(new ArrayList<>(List.of(collection)));
    Figurine figurine = figurine(10L);
    CollectorCollectionFigurine existing = link(collection, figurine);

    when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
    when(collectorCollectionRepository.findById(20L)).thenReturn(Optional.of(collection));
    when(figurineRepository.findById(10L)).thenReturn(Optional.of(figurine));
    when(collectorCollectionFigurineRepository.findByCollectionAndFigurine(collection, figurine))
        .thenReturn(Optional.of(existing));

    service.deleteCollectionFigurine(1L, 20L, 10L);

    verify(collectorCollectionFigurineRepository).delete(existing);
  }

  @Test
  void deleteCollectionFigurine_shouldDoNothing_whenFigurineIsNotInCollection() {
    Collector collector = collectorWithCollections(1L);
    CollectorCollection collection = collection(20L, collector, "Collection", "Desc");
    collector.setCollections(new ArrayList<>(List.of(collection)));
    Figurine figurine = figurine(10L);

    when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
    when(collectorCollectionRepository.findById(20L)).thenReturn(Optional.of(collection));
    when(figurineRepository.findById(10L)).thenReturn(Optional.of(figurine));
    when(collectorCollectionFigurineRepository.findByCollectionAndFigurine(collection, figurine))
        .thenReturn(Optional.empty());

    service.deleteCollectionFigurine(1L, 20L, 10L);

    verify(collectorCollectionFigurineRepository, never()).delete(any());
  }

  @Test
  void deleteCollection_shouldDeleteCollection_whenCollectorOwnsCollection() {
    Collector collector = collectorWithCollections(1L);
    CollectorCollection collection = collection(20L, collector, "Collection", null);
    collector.setCollections(new ArrayList<>(List.of(collection)));

    when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
    when(collectorCollectionFigurineRepository.deleteByCollectionIdAndCollectorId(20L, 1L))
        .thenReturn(2);
    lenient().when(collectorCollectionRepository.existsById(20L)).thenReturn(true);

    service.deleteCollection(1L, 20L);

    verify(collectorCollectionFigurineRepository).deleteByCollectionIdAndCollectorId(20L, 1L);
    verify(collectorCollectionRepository).deleteCollectionById(20L);
  }

  @Test
  void deleteCollection_shouldThrowCollectorNotFoundException_whenCollectorMissing() {
    when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.deleteCollection(1L, 20L))
        .isInstanceOf(CollectorNotFoundException.class)
        .hasMessage("Collector with id 1 was not found");
  }

  @Test
  void deleteCollection_shouldThrowCollectionNotFoundException_whenCollectorDoesNotOwnCollection() {
    Collector collector = collectorWithCollections(1L);
    collector.setCollections(new ArrayList<>(List.of(collection(99L, collector, "Other", null))));

    when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
    lenient().when(collectorCollectionRepository.existsById(20L)).thenReturn(true);

    assertThatThrownBy(() -> service.deleteCollection(1L, 20L))
        .isInstanceOf(CollectionNotFoundException.class)
        .hasMessage("Collection with id 20 was not found");
  }

  @Test
  void deleteCollection_shouldThrowCollectionNotFoundException_whenCollectionNoLongerExists() {
    Collector collector = collectorWithCollections(1L);
    CollectorCollection collection = collection(20L, collector, "Collection", null);
    collector.setCollections(new ArrayList<>(List.of(collection)));

    when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
    when(collectorCollectionFigurineRepository.deleteByCollectionIdAndCollectorId(20L, 1L))
        .thenReturn(1);
    when(collectorCollectionRepository.existsById(20L)).thenReturn(false);

    assertThatThrownBy(() -> service.deleteCollection(1L, 20L))
        .isInstanceOf(CollectionNotFoundException.class)
        .hasMessage("Collection with id 20 was not found");

    verify(collectorCollectionRepository, never()).deleteCollectionById(20L);
  }

  @Test
  void updateCollection_shouldUpdateCollection_whenCollectorOwnsCollection() {
    Collector collector = collectorWithCollections(1L);
    CollectorCollection collection = collection(20L, collector, "Old", "Old desc");
    collector.setCollections(new ArrayList<>(List.of(collection)));
    CollectorCollectionReq request = new CollectorCollectionReq("New", "New desc");

    when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
    lenient().when(collectorCollectionRepository.findById(20L)).thenReturn(Optional.of(collection));
    when(collectorCollectionRepository.save(collection)).thenReturn(collection);

    CollectorCollectionResp result = service.updateCollection(1L, 20L, request);

    assertThat(result.id()).isEqualTo(20L);
    assertThat(result.name()).isEqualTo("New");
    assertThat(result.description()).isEqualTo("New desc");
    verify(collectorCollectionRepository).save(collection);
  }

  @Test
  void updateCollection_shouldThrowCollectorNotFoundException_whenCollectorMissing() {
    when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> service.updateCollection(1L, 20L, new CollectorCollectionReq("New", "New desc")))
        .isInstanceOf(CollectorNotFoundException.class)
        .hasMessage("Collector with id 1 was not found");
  }

  @Test
  void updateCollection_shouldThrowCollectionNotFoundException_whenCollectorDoesNotOwnCollection() {
    Collector collector = collectorWithCollections(1L);
    collector.setCollections(new ArrayList<>(List.of(collection(99L, collector, "Other", null))));
    CollectorCollection collection = collection(20L, collector, "Old", "Old desc");

    when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
    lenient().when(collectorCollectionRepository.findById(20L)).thenReturn(Optional.of(collection));

    assertThatThrownBy(
            () -> service.updateCollection(1L, 20L, new CollectorCollectionReq("New", "New desc")))
        .isInstanceOf(CollectionNotFoundException.class)
        .hasMessage("Collection with id 20 was not found");
  }

  @Test
  void updateCollection_shouldThrowCollectionNotFoundException_whenCollectionMissingInRepository() {
    Collector collector = collectorWithCollections(1L);
    collector.setCollections(
        new ArrayList<>(List.of(collection(20L, collector, "Old", "Old desc"))));

    when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
    when(collectorCollectionRepository.findById(20L)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> service.updateCollection(1L, 20L, new CollectorCollectionReq("New", "New desc")))
        .isInstanceOf(CollectionNotFoundException.class)
        .hasMessage("Collection with id 20 was not found");
  }

  private Figurine figurine(Long id) {
    Figurine figurine = new Figurine();
    figurine.setId(id);
    return figurine;
  }

  private Figurine figurineWithReleaseDate(Long id, String normalizedName, LocalDate releaseDate) {
    Figurine figurine = figurine(id);
    figurine.setNormalizedName(normalizedName);

    Distributor distributor = new Distributor();
    distributor.setId(id + 100);
    distributor.setName(DistributorName.BANDAI);
    distributor.setCountry(CountryCode.JP);

    FigurineDistributor figurineDistributor = new FigurineDistributor();
    figurineDistributor.setFigurine(figurine);
    figurineDistributor.setDistributor(distributor);
    figurineDistributor.setCurrency(CurrencyCode.JPY);
    figurineDistributor.setReleaseDate(releaseDate);

    figurine.setDistributors(new ArrayList<>(List.of(figurineDistributor)));
    return figurine;
  }

  private Collector collector(Long id) {
    Collector collector = new Collector();
    collector.setId(id);
    return collector;
  }

  private Collector collectorWithCollections(Long id) {
    Collector collector = collector(id);
    collector.setCollections(new ArrayList<>());
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
