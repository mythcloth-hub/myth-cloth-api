package com.mesofi.mythclothapi.collectorscollections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.collectors.CollectorRepository;
import com.mesofi.mythclothapi.collectors.exceptions.CollectorNotFoundException;
import com.mesofi.mythclothapi.collectors.mapper.CollectorMapper;
import com.mesofi.mythclothapi.collectorscollections.dto.AssignFigurinesReq;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectionAssignmentMode;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionCatalogSummaryResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionFigurineDetailResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionFigurineResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionLatestFavoriteResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionReq;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionSummaryResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionSummaryStatsResp;
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectorCollectionAlreadyExistsException;
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectorCollectionLimitReachedException;
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectorCollectionNotFoundException;
import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine;
import com.mesofi.mythclothapi.collectorscollections.model.Condition;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionFigurineRepository;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionRepository;
import com.mesofi.mythclothapi.collectorscollections.repository.projection.CollectorCollectionCatalogProjection;
import com.mesofi.mythclothapi.collectorscollections.repository.projection.CollectorCollectionSummaryProjection;
import com.mesofi.mythclothapi.collectorspurchases.CollectorPurchaseRepository;
import com.mesofi.mythclothapi.collectorspurchases.model.CollectorPurchase;
import com.mesofi.mythclothapi.common.CurrencyCode;
import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.distributors.model.Distributor;
import com.mesofi.mythclothapi.distributors.model.DistributorName;
import com.mesofi.mythclothapi.figurinedistributions.FigurineDistributor;
import com.mesofi.mythclothapi.figurines.FigurineFilter;
import com.mesofi.mythclothapi.figurines.FigurineNotFoundException;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.model.FigurineWithCollectionId;
import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;
import com.mesofi.mythclothapi.figurines.repository.CollectablePageImpl;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;

@ExtendWith(MockitoExtension.class)
class CollectorCollectionFigurineServiceTest {

    private static final Instant PAST_INSTANT = Instant.parse("2024-03-10T10:15:30Z");

    @InjectMocks
    private CollectorCollectionFigurineService service;

    @Mock
    private CollectorCollectionFigurineRepository collectorCollectionFigurineRepository;
    @Mock
    private CollectorCollectionRepository collectorCollectionRepository;
    @Mock
    private CollectorPurchaseRepository collectorPurchaseRepository;
    @Mock
    private CollectorRepository collectorRepository;
    @Mock
    private FigurineRepository figurineRepository;
    @Mock
    private CollectorMapper collectorMapper;

    @Test
    void assignFigurinesToCollections_shouldCreateDefaultCollectionWithOwnedAndMissingItems_whenModeIsAuto() {
        stubCollectionMapping();

        Collector collector = collector(1L);
        AssignFigurinesReq request = new AssignFigurinesReq(List.of(9L), CollectionAssignmentMode.AUTO, null, null);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findByCollectorAndName(collector, "My Myth Collection"))
                .thenReturn(Optional.empty());
        when(collectorCollectionRepository.countByCollector(collector)).thenReturn(0L);
        when(collectorCollectionRepository.save(any(CollectorCollection.class))).thenAnswer(invocation -> {
            CollectorCollection saved = invocation.getArgument(0);
            saved.setId(77L);
            return saved;
        });
        when(figurineRepository.findAllFigurineIdsWithReleasedOrAnnouncedStatus()).thenReturn(List.of(9L, 10L));
        when(collectorCollectionFigurineRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.assignFigurinesToCollections(1L, request);

        ArgumentCaptor<CollectorCollection> collectionCaptor = ArgumentCaptor.forClass(CollectorCollection.class);
        verify(collectorCollectionRepository).save(collectionCaptor.capture());
        CollectorCollection savedCollection = collectionCaptor.getValue();
        assertThat(savedCollection.getName()).isEqualTo("My Myth Collection");
        assertThat(savedCollection.getDescription()).isEqualTo("This collection was automatically created for you.");
        assertThat(savedCollection.isFavorite()).isTrue();
        assertThat(savedCollection.getCollector()).isEqualTo(collector);

        ArgumentCaptor<List<CollectorCollectionFigurine>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(collectorCollectionFigurineRepository).saveAll(itemsCaptor.capture());
        assertThat(itemsCaptor.getValue()).hasSize(2);

        CollectorCollectionFigurine ownedItem = itemsCaptor.getValue().get(0);
        assertThat(ownedItem.getCollection()).isEqualTo(savedCollection);
        assertThat(ownedItem.getFigurine().getId()).isEqualTo(9L);
        assertThat(ownedItem.isOwned()).isTrue();
        assertThat(ownedItem.getQuantity()).isEqualTo(1);
        assertThat(ownedItem.getCondition()).isEqualTo(Condition.SEALED);
        assertThat(ownedItem.getAddedAt()).isNotNull();

        CollectorCollectionFigurine missingItem = itemsCaptor.getValue().get(1);
        assertThat(missingItem.getCollection()).isEqualTo(savedCollection);
        assertThat(missingItem.getFigurine().getId()).isEqualTo(10L);
        assertThat(missingItem.isOwned()).isFalse();
        assertThat(missingItem.getQuantity()).isEqualTo(0);
        assertThat(missingItem.getCondition()).isEqualTo(Condition.SEALED);
        assertThat(missingItem.getAddedAt()).isNull();
    }

    @Test
    void assignFigurinesToCollections_shouldNotMarkAutoCreatedCollectionAsFavorite_whenCollectorAlreadyHasCollections() {
        stubCollectionMapping();

        Collector collector = collector(1L);
        AssignFigurinesReq request = new AssignFigurinesReq(List.of(9L), CollectionAssignmentMode.AUTO, null, null);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findByCollectorAndName(collector, "My Myth Collection"))
                .thenReturn(Optional.empty());
        when(collectorCollectionRepository.countByCollector(collector)).thenReturn(1L);
        when(collectorCollectionRepository.save(any(CollectorCollection.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(figurineRepository.findAllFigurineIdsWithReleasedOrAnnouncedStatus()).thenReturn(List.of(9L));
        when(collectorCollectionFigurineRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.assignFigurinesToCollections(1L, request);

        ArgumentCaptor<CollectorCollection> collectionCaptor = ArgumentCaptor.forClass(CollectorCollection.class);
        verify(collectorCollectionRepository).save(collectionCaptor.capture());
        assertThat(collectionCaptor.getValue().isFavorite()).isFalse();
    }

    @Test
    void assignFigurinesToCollections_shouldThrowCollectorCollectionLimitReachedException_whenAutoCreationReachesLimit() {
        Collector collector = collector(1L);
        AssignFigurinesReq request = new AssignFigurinesReq(List.of(9L), CollectionAssignmentMode.AUTO, null, null);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findByCollectorAndName(collector, "My Myth Collection"))
                .thenReturn(Optional.empty());
        when(collectorCollectionRepository.countByCollector(collector)).thenReturn(6L);

        assertThatThrownBy(() -> service.assignFigurinesToCollections(1L, request))
                .isInstanceOf(CollectorCollectionLimitReachedException.class)
                .hasMessage("Collector account with ID '1' has reached the limit of collector collections: 6");

        verify(collectorCollectionRepository, never()).save(any());
        verifyNoInteractions(collectorCollectionFigurineRepository);
    }

    @Test
    void assignFigurinesToCollections_shouldThrowCollectorCollectionAlreadyExistsException_whenCreateNameIsTaken() {
        Collector collector = collector(1L);
        CollectorCollection existing = collection(2L, collector, "Taken", null, null);
        AssignFigurinesReq request = new AssignFigurinesReq(List.of(9L), CollectionAssignmentMode.CREATE, null,
                new CollectorCollectionReq(true, "Taken", null, null));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findByCollectorAndName(collector, "Taken"))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.assignFigurinesToCollections(1L, request))
                .isInstanceOf(CollectorCollectionAlreadyExistsException.class)
                .hasMessage("Collector collection with name 'Taken' already exists");

        verify(collectorCollectionRepository, never()).save(any());
        verifyNoInteractions(collectorCollectionFigurineRepository);
    }

    @Test
    void assignFigurinesToCollections_shouldCreateSubCollectionItemsAsNotOwned_whenModeIsCreate() {
        stubCollectionMapping();

        Collector collector = collector(1L);
        AssignFigurinesReq request = new AssignFigurinesReq(List.of(9L, 10L), CollectionAssignmentMode.CREATE, null,
                new CollectorCollectionReq(true, "Bronze Saints", "bronze.png", "Only bronzes"));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findByCollectorAndName(collector, "Bronze Saints"))
                .thenReturn(Optional.empty());
        when(collectorCollectionRepository.countByCollector(collector)).thenReturn(1L);
        when(collectorCollectionRepository.save(any(CollectorCollection.class))).thenAnswer(invocation -> {
            CollectorCollection saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });
        when(collectorCollectionFigurineRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.assignFigurinesToCollections(1L, request);

        ArgumentCaptor<List<CollectorCollectionFigurine>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(collectorCollectionFigurineRepository).saveAll(itemsCaptor.capture());
        assertThat(itemsCaptor.getValue()).hasSize(2);
        assertThat(itemsCaptor.getValue()).allSatisfy(item -> {
            assertThat(item.isOwned()).isFalse();
            assertThat(item.getQuantity()).isEqualTo(0);
            assertThat(item.getCondition()).isEqualTo(Condition.SEALED);
            assertThat(item.getAddedAt()).isNull();
        });
        assertThat(itemsCaptor.getValue()).extracting(item -> item.getFigurine().getId()).containsExactly(9L, 10L);
        verify(figurineRepository, never()).findAllFigurineIdsWithReleasedOrAnnouncedStatus();
    }

    @Test
    void assignFigurinesToCollections_shouldUpdateExistingAssignments_whenModeIsExisting() {
        Collector collector = collector(1L);
        CollectorCollection collection = collection(2L, collector, "Team A", null, null);
        Figurine ownedFigurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));
        Figurine missingOwnedFigurine = figurine(10L, "shiryu", ReleaseStatus.ANNOUNCED, LocalDate.of(2024, 4, 1));
        CollectorCollectionFigurine ownedItem = collectionFigurine(collection, ownedFigurine, 2, Condition.OPENED, true,
                PAST_INSTANT);
        CollectorCollectionFigurine unownedItem = collectionFigurine(collection, missingOwnedFigurine, 0,
                Condition.SEALED, false, null);
        collection.setFigurines(new ArrayList<>(List.of(ownedItem, unownedItem)));
        collector.setCollections(new ArrayList<>(List.of(collection)));
        AssignFigurinesReq request = new AssignFigurinesReq(new ArrayList<>(List.of(9L, 10L, 11L)),
                CollectionAssignmentMode.EXISTING, List.of(2L), null);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(collection));

        service.assignFigurinesToCollections(1L, request);

        assertThat(ownedItem.getQuantity()).isEqualTo(3);
        assertThat(ownedItem.getAddedAt()).isNotNull().isNotEqualTo(PAST_INSTANT);
        assertThat(unownedItem.isOwned()).isTrue();
        assertThat(unownedItem.getQuantity()).isEqualTo(1);
        assertThat(unownedItem.getAddedAt()).isNotNull();
        assertThat(collection.getFigurines()).hasSize(3);

        CollectorCollectionFigurine newItem = collection.getFigurines().get(2);
        assertThat(newItem.getFigurine().getId()).isEqualTo(11L);
        assertThat(newItem.isOwned()).isFalse();
        assertThat(newItem.getQuantity()).isEqualTo(0);
        assertThat(newItem.getCondition()).isEqualTo(Condition.SEALED);
        assertThat(newItem.getAddedAt()).isNull();
    }

    @Test
    void assignFigurinesToCollections_shouldThrowCollectorCollectionNotFoundException_whenExistingCollectionIsNotOwned() {
        Collector collector = collector(1L);
        AssignFigurinesReq request = new AssignFigurinesReq(new ArrayList<>(List.of(9L)),
                CollectionAssignmentMode.EXISTING, List.of(2L), null);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));

        assertThatThrownBy(() -> service.assignFigurinesToCollections(1L, request))
                .isInstanceOf(CollectorCollectionNotFoundException.class)
                .hasMessage("Collector collection with id 2 was not found");

        verify(collectorCollectionRepository, never()).findById(any());
    }

    @Test
    void assignFigurinesToCollections_shouldThrowNullPointerException_whenModeIsNull() {
        AssignFigurinesReq request = new AssignFigurinesReq(List.of(9L), null, null, null);

        assertThatThrownBy(() -> service.assignFigurinesToCollections(1L, request))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(collectorRepository, collectorCollectionRepository, collectorCollectionFigurineRepository,
                figurineRepository, collectorMapper);
    }

    @Test
    void addFigurineToFavoriteCollection_shouldCreateDefaultCollection_whenCollectorHasNoCollections() {
        stubCollectionMapping();

        Collector collector = collector(1L);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findByCollectorAndName(collector, "My Myth Collection"))
                .thenReturn(Optional.empty());
        when(collectorCollectionRepository.countByCollector(collector)).thenReturn(0L);
        when(collectorCollectionRepository.save(any(CollectorCollection.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(figurineRepository.findAllFigurineIdsWithReleasedOrAnnouncedStatus()).thenReturn(List.of(9L));
        when(collectorCollectionFigurineRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.addFigurineToFavoriteCollection(1L, 9L);

        verify(collectorCollectionRepository).save(any(CollectorCollection.class));
        verify(collectorCollectionFigurineRepository).saveAll(any());
    }

    @Test
    void addFigurineToFavoriteCollection_shouldAddToExistingFavoriteCollection_whenFavoriteExists() {
        CollectorCollection favorite = collection(2L, null, "Favorite", null, null);
        favorite.setFavorite(true);
        Figurine figurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));
        CollectorCollectionFigurine item = collectionFigurine(favorite, figurine, 1, Condition.SEALED, true,
                PAST_INSTANT);
        favorite.setFigurines(new ArrayList<>(List.of(item)));
        Collector collector = collector(1L, favorite);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(favorite));

        service.addFigurineToFavoriteCollection(1L, 9L);

        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getAddedAt()).isNotNull().isNotEqualTo(PAST_INSTANT);
    }

    @Test
    void addFigurineToFavoriteCollection_shouldDoNothing_whenCollectorHasNoFavoriteCollection() {
        Collector collector = collector(1L, collection(2L, null, "Team", null, null));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));

        service.addFigurineToFavoriteCollection(1L, 9L);

        verifyNoInteractions(collectorCollectionRepository, collectorCollectionFigurineRepository, figurineRepository);
    }

    @Test
    void retrieveCollectionSummary_shouldReturnMappedSummary_whenCollectorOwnsCollection() {
        CollectorCollection collection = collection(2L, null, "Team", null, null);
        Collector collector = collector(1L, collection);
        CollectorCollectionCatalogProjection catalogProjection = mock(CollectorCollectionCatalogProjection.class);
        CollectorCollectionSummaryProjection collectionProjection = mock(CollectorCollectionSummaryProjection.class);
        CollectorCollectionSummaryStatsResp stats = new CollectorCollectionSummaryStatsResp(2, 5, 1, 4, 3);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.getCollectorCollectionCatalog(2L, true)).thenReturn(catalogProjection);
        when(collectorCollectionRepository.getCollectorCollectionSummary(2L, true)).thenReturn(collectionProjection);
        when(catalogProjection.getTotalFigurines()).thenReturn(12);
        when(catalogProjection.getTotalAnnounced()).thenReturn(5);
        when(catalogProjection.getTotalReleased()).thenReturn(7);
        when(collectorMapper.toCollectorCollectionSummaryResp(collectionProjection, 7)).thenReturn(stats);

        CollectorCollectionSummaryResp response = service.retrieveCollectionSummary(1L, 2L, true);

        assertThat(response).isEqualTo(
                new CollectorCollectionSummaryResp(new CollectorCollectionCatalogSummaryResp(12, 5, 7), stats));
        verify(collectorCollectionRepository).getCollectorCollectionCatalog(2L, true);
        verify(collectorCollectionRepository).getCollectorCollectionSummary(2L, true);
        verify(collectorMapper).toCollectorCollectionSummaryResp(collectionProjection, 7);
    }

    @Test
    void retrieveCollectionSummary_shouldThrowCollectorNotFoundException_whenCollectorIsMissing() {
        when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.retrieveCollectionSummary(1L, 2L, false))
                .isInstanceOf(CollectorNotFoundException.class).hasMessage("Collector with id 1 was not found");

        verifyNoInteractions(collectorCollectionRepository, collectorMapper);
    }

    @Test
    void retrieveCollectionSummary_shouldThrowCollectorCollectionNotFoundException_whenCollectorDoesNotOwnCollection() {
        Collector collector = collector(1L, collection(3L, null, "Other", null, null));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));

        assertThatThrownBy(() -> service.retrieveCollectionSummary(1L, 2L, false))
                .isInstanceOf(CollectorCollectionNotFoundException.class)
                .hasMessage("Collector collection with id 2 was not found");

        verifyNoInteractions(collectorCollectionRepository, collectorMapper);
    }

    @Test
    void retrieveCollectionFigurines_shouldReturnMappedFigurines_whenCollectorOwnsCollection() {
        CollectorCollection collection = collection(2L, null, "Team", null, null);
        Figurine released = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));
        Figurine announced = figurine(10L, "shiryu", ReleaseStatus.ANNOUNCED, LocalDate.of(2023, 12, 1));
        CollectorCollectionFigurine collected = collectionFigurine(collection, released, 2, Condition.SEALED, true,
                PAST_INSTANT);
        CollectorCollectionFigurine unowned = collectionFigurine(collection, announced, 0, Condition.SEALED, false,
                null);
        collection.setFigurines(new ArrayList<>(List.of(collected, unowned)));
        Collector collector = collector(1L, collection);
        List<FigurineWithCollectionId> figurines = List.of(new FigurineWithCollectionId(released, 101L),
                new FigurineWithCollectionId(announced, 102L));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(collection));
        when(figurineRepository.findPaginated(any(FigurineFilter.class), any(PageRequest.class), eq(2L)))
                .thenReturn(new CollectablePageImpl<>(figurines, PageRequest.of(0, 50), 2, 3));
        when(collectorMapper.toCollectorCollectionFigurineResp(anyLong(), any(Figurine.class), any(ReleaseStatus.class),
                anyBoolean(), anyInt())).thenAnswer(invocation -> {
                    long collectionFigurineId = invocation.getArgument(0);
                    Figurine figurine = invocation.getArgument(1);
                    ReleaseStatus releaseStatus = invocation.getArgument(2);
                    boolean isCollected = invocation.getArgument(3);
                    int ownedQuantity = invocation.getArgument(4);
                    return new CollectorCollectionFigurineResp(collectionFigurineId, figurine.getId(),
                            figurine.getNormalizedName(), releaseStatus, null, null, isCollected, ownedQuantity);
                });

        Page<CollectorCollectionFigurineResp> response = service.retrieveCollectionFigurines(1L, 2L, false, 0, 50);

        assertThat(response.getContent()).containsExactly(
                new CollectorCollectionFigurineResp(101L, 9L, "seiya", ReleaseStatus.RELEASED, null, null, true, 2),
                new CollectorCollectionFigurineResp(102L, 10L, "shiryu", ReleaseStatus.ANNOUNCED, null, null, false,
                        0));

        ArgumentCaptor<FigurineFilter> filterCaptor = ArgumentCaptor.forClass(FigurineFilter.class);
        verify(figurineRepository).findPaginated(filterCaptor.capture(), eq(PageRequest.of(0, 50)), eq(2L));
        assertThat(filterCaptor.getValue().restocks()).isFalse();
        assertThat(filterCaptor.getValue().releaseStatuses()).containsExactly("RELEASED", "ANNOUNCED");
    }

    @Test
    void retrieveCollectionFigurines_shouldUseReleasedAndAnnouncedFilterWithoutRestocks_whenIncludeRestocksIsTrue() {
        CollectorCollection collection = collection(2L, null, "Team", null, null);
        Collector collector = collector(1L, collection);
        Figurine figurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(collection));
        when(figurineRepository.findPaginated(any(FigurineFilter.class), any(PageRequest.class), eq(2L)))
                .thenReturn(new CollectablePageImpl<>(List.of(new FigurineWithCollectionId(figurine, 101L)),
                        PageRequest.of(0, 10), 1, 1));
        when(collectorMapper.toCollectorCollectionFigurineResp(anyLong(), any(Figurine.class), any(ReleaseStatus.class),
                anyBoolean(), anyInt()))
                .thenReturn(new CollectorCollectionFigurineResp(101L, 9L, "seiya", ReleaseStatus.RELEASED, null, null,
                        false, 0));

        service.retrieveCollectionFigurines(1L, 2L, true, 0, 10);

        ArgumentCaptor<FigurineFilter> filterCaptor = ArgumentCaptor.forClass(FigurineFilter.class);
        verify(figurineRepository).findPaginated(filterCaptor.capture(), eq(PageRequest.of(0, 10)), eq(2L));
        assertThat(filterCaptor.getValue().restocks()).isNull();
        assertThat(filterCaptor.getValue().releaseStatuses()).containsExactly("RELEASED", "ANNOUNCED");
    }

    @Test
    void retrieveCollectionFigurines_shouldThrowCollectorNotFoundException_whenCollectorIsMissing() {
        when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.retrieveCollectionFigurines(1L, 2L, false, 0, 50))
                .isInstanceOf(CollectorNotFoundException.class).hasMessage("Collector with id 1 was not found");

        verifyNoInteractions(collectorCollectionRepository, figurineRepository, collectorMapper);
    }

    @Test
    void retrieveCollectionFigurines_shouldThrowCollectorCollectionNotFoundException_whenCollectionIsMissing() {
        Collector collector = collector(1L);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.retrieveCollectionFigurines(1L, 2L, false, 0, 50))
                .isInstanceOf(CollectorCollectionNotFoundException.class)
                .hasMessage("Collector collection with id 2 was not found");

        verifyNoInteractions(figurineRepository, collectorMapper);
    }

    @Test
    void retrieveCollectionFigurines_shouldThrowCollectorCollectionNotFoundException_whenCollectorDoesNotOwnCollection() {
        CollectorCollection targetCollection = collection(2L, null, "Team", null, null);
        Collector collector = collector(1L, collection(3L, null, "Other", null, null));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(targetCollection));

        assertThatThrownBy(() -> service.retrieveCollectionFigurines(1L, 2L, false, 0, 50))
                .isInstanceOf(CollectorCollectionNotFoundException.class)
                .hasMessage("Collector collection with id 2 was not found");

        verifyNoInteractions(figurineRepository, collectorMapper);
    }

    @Test
    void retrieveCollectionFigurine_shouldReturnMappedDetail_whenCollectorOwnsCollectionAndFigurineExists() {
        CollectorCollection collection = collection(2L, null, "Team", null, null);
        Collector collector = collector(1L, collection);
        Figurine figurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));
        CollectorCollectionFigurineDetailResp detail = new CollectorCollectionFigurineDetailResp("Seiya", List.of(),
                null, null, null);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(collection));
        when(figurineRepository.findById(9L)).thenReturn(Optional.of(figurine));
        when(collectorMapper.toCollectorCollectionFigurineDetailResp(figurine)).thenReturn(detail);

        CollectorCollectionFigurineDetailResp response = service.retrieveCollectionFigurine(1L, 2L, 9L);

        assertThat(response).isEqualTo(detail);
    }

    @Test
    void retrieveCollectionFigurine_shouldThrowCollectorNotFoundException_whenCollectorIsMissing() {
        when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.retrieveCollectionFigurine(1L, 2L, 9L))
                .isInstanceOf(CollectorNotFoundException.class).hasMessage("Collector with id 1 was not found");

        verifyNoInteractions(collectorCollectionRepository, figurineRepository, collectorMapper);
    }

    @Test
    void retrieveCollectionFigurine_shouldThrowCollectorCollectionNotFoundException_whenCollectionIsMissing() {
        Collector collector = collector(1L);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.retrieveCollectionFigurine(1L, 2L, 9L))
                .isInstanceOf(CollectorCollectionNotFoundException.class)
                .hasMessage("Collector collection with id 2 was not found");

        verifyNoInteractions(figurineRepository, collectorMapper);
    }

    @Test
    void retrieveCollectionFigurine_shouldThrowCollectorCollectionNotFoundException_whenCollectorDoesNotOwnCollection() {
        CollectorCollection targetCollection = collection(2L, null, "Team", null, null);
        Collector collector = collector(1L, collection(3L, null, "Other", null, null));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(targetCollection));

        assertThatThrownBy(() -> service.retrieveCollectionFigurine(1L, 2L, 9L))
                .isInstanceOf(CollectorCollectionNotFoundException.class)
                .hasMessage("Collector collection with id 2 was not found");

        verifyNoInteractions(figurineRepository, collectorMapper);
    }

    @Test
    void retrieveCollectionFigurine_shouldThrowFigurineNotFoundException_whenFigurineIsMissing() {
        CollectorCollection collection = collection(2L, null, "Team", null, null);
        Collector collector = collector(1L, collection);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(collection));
        when(figurineRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.retrieveCollectionFigurine(1L, 2L, 9L))
                .isInstanceOf(FigurineNotFoundException.class).hasMessage("Figurine with id 9 was not found");

        verifyNoInteractions(collectorMapper);
    }

    @Test
    void retrieveLatestFavoriteCollectionFigurines_shouldReturnMappedLatestFigurines_whenFavoriteCollectionExists() {
        CollectorCollection favorite = collection(2L, null, "Favorite", null, null);
        favorite.setFavorite(true);
        Collector collector = collector(1L, favorite);
        CollectorCollectionFigurine first = collectionFigurine(favorite,
                figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1)), 2, Condition.SEALED, true,
                PAST_INSTANT);
        CollectorCollectionFigurine second = collectionFigurine(favorite,
                figurine(10L, "shiryu", ReleaseStatus.ANNOUNCED, LocalDate.of(2024, 4, 1)), 1, Condition.OPENED, true,
                Instant.parse("2024-04-01T10:15:30Z"));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionFigurineRepository.findByCollectionAndOwnedTrueOrderByAddedAtDesc(favorite,
                PageRequest.of(0, 20))).thenReturn(List.of(first, second));
        when(collectorMapper.toCollectorCollectionLatestFavoriteResp(first))
                .thenReturn(new CollectorCollectionLatestFavoriteResp(9L, "seiya", "image-1.png", 2));
        when(collectorMapper.toCollectorCollectionLatestFavoriteResp(second))
                .thenReturn(new CollectorCollectionLatestFavoriteResp(10L, "shiryu", "image-2.png", 1));

        List<CollectorCollectionLatestFavoriteResp> response = service.retrieveLatestFavoriteCollectionFigurines(1L,
                20);

        assertThat(response).containsExactly(new CollectorCollectionLatestFavoriteResp(9L, "seiya", "image-1.png", 2),
                new CollectorCollectionLatestFavoriteResp(10L, "shiryu", "image-2.png", 1));
    }

    @Test
    void retrieveLatestFavoriteCollectionFigurines_shouldReturnEmptyList_whenFavoriteCollectionIsMissing() {
        Collector collector = collector(1L, collection(2L, null, "Team", null, null));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));

        List<CollectorCollectionLatestFavoriteResp> response = service.retrieveLatestFavoriteCollectionFigurines(1L,
                20);

        assertThat(response).isEmpty();
        verifyNoInteractions(collectorCollectionFigurineRepository, collectorMapper);
    }

    @Test
    void findLatestFavoriteCollectionFigurines_shouldReturnAssignmentsFromFavoriteCollection() {
        CollectorCollection favorite = collection(2L, null, "Favorite", null, null);
        favorite.setFavorite(true);
        Collector collector = collector(1L, favorite);
        CollectorCollectionFigurine assignment = collectionFigurine(favorite,
                figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1)), 2, Condition.SEALED, true,
                PAST_INSTANT);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionFigurineRepository.findByCollectionAndOwnedTrueOrderByAddedAtDesc(favorite,
                PageRequest.of(0, 5))).thenReturn(List.of(assignment));

        assertThat(service.findLatestFavoriteCollectionFigurines(1L, 5)).containsExactly(assignment);
    }

    @Test
    void deleteCollectionFigurine_shouldResetAssignmentState_whenAssignmentExists() {
        CollectorCollection collection = collection(2L, null, "Team", null, null);
        Collector collector = collector(1L, collection);
        Figurine figurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));
        CollectorCollectionFigurine assignment = collectionFigurine(collection, figurine, 2, Condition.SEALED, true,
                PAST_INSTANT);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(collection));
        when(figurineRepository.findById(9L)).thenReturn(Optional.of(figurine));
        when(collectorCollectionFigurineRepository.findByCollectionAndFigurine(collection, figurine))
                .thenReturn(Optional.of(assignment));

        service.deleteCollectionFigurine(1L, 2L, 9L);

        assertThat(assignment.getQuantity()).isEqualTo(0);
        assertThat(assignment.isOwned()).isFalse();
        assertThat(assignment.getAddedAt()).isNull();
        verify(collectorCollectionFigurineRepository, never()).delete(any());
    }

    @Test
    void deleteCollectionFigurine_shouldDoNothing_whenAssignmentDoesNotExist() {
        CollectorCollection collection = collection(2L, null, "Team", null, null);
        Collector collector = collector(1L, collection);
        Figurine figurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(collection));
        when(figurineRepository.findById(9L)).thenReturn(Optional.of(figurine));
        when(collectorCollectionFigurineRepository.findByCollectionAndFigurine(collection, figurine))
                .thenReturn(Optional.empty());

        service.deleteCollectionFigurine(1L, 2L, 9L);

        verify(collectorCollectionFigurineRepository, never()).delete(any());
    }

    @Test
    void deleteCollectionFigurine_shouldThrowCollectorNotFoundException_whenCollectorIsMissing() {
        when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteCollectionFigurine(1L, 2L, 9L))
                .isInstanceOf(CollectorNotFoundException.class).hasMessage("Collector with id 1 was not found");
    }

    @Test
    void deleteCollectionFigurine_shouldThrowCollectorCollectionNotFoundException_whenCollectionIsMissing() {
        Collector collector = collector(1L);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteCollectionFigurine(1L, 2L, 9L))
                .isInstanceOf(CollectorCollectionNotFoundException.class)
                .hasMessage("Collector collection with id 2 was not found");

        verifyNoInteractions(figurineRepository, collectorCollectionFigurineRepository);
    }

    @Test
    void deleteCollectionFigurine_shouldThrowCollectorCollectionNotFoundException_whenCollectorDoesNotOwnCollection() {
        CollectorCollection targetCollection = collection(2L, null, "Team", null, null);
        Collector collector = collector(1L, collection(3L, null, "Other", null, null));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(targetCollection));

        assertThatThrownBy(() -> service.deleteCollectionFigurine(1L, 2L, 9L))
                .isInstanceOf(CollectorCollectionNotFoundException.class)
                .hasMessage("Collector collection with id 2 was not found");

        verifyNoInteractions(figurineRepository, collectorCollectionFigurineRepository);
    }

    @Test
    void deleteCollectionFigurine_shouldThrowFigurineNotFoundException_whenFigurineIsMissing() {
        CollectorCollection collection = collection(2L, null, "Team", null, null);
        Collector collector = collector(1L, collection);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(collection));
        when(figurineRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteCollectionFigurine(1L, 2L, 9L))
                .isInstanceOf(FigurineNotFoundException.class).hasMessage("Figurine with id 9 was not found");

        verifyNoInteractions(collectorCollectionFigurineRepository);
    }

    @Test
    void retrieveCollections_shouldReturnMappedCollections_whenCollectorExists() {
        CollectorCollection collection = collection(2L, null, "Team", null, null);
        Collector collector = collector(1L, collection);
        CollectorCollectionResp response = new CollectorCollectionResp(2L, "Team", null, null, false, 0, 0, List.of());

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorMapper.toCollectorCollectionResp(collection)).thenReturn(response);

        assertThat(service.retrieveCollections(1L)).containsExactly(response);
    }

    @Test
    void retrieveCollections_shouldThrowCollectorNotFoundException_whenCollectorIsMissing() {
        when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.retrieveCollections(1L)).isInstanceOf(CollectorNotFoundException.class)
                .hasMessage("Collector with id 1 was not found");

        verifyNoInteractions(collectorCollectionRepository, collectorMapper);
    }

    @Test
    void deleteCollection_shouldDeleteCollectionAndAssignments_whenCollectorOwnsCollection() {
        CollectorCollection collection = collection(2L, null, "Team", null, null);
        Collector collector = collector(1L, collection);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(collection));
        when(collectorPurchaseRepository.findAllByCollectorAndCollection(collector, collection)).thenReturn(List.of());
        when(collectorCollectionFigurineRepository.deleteByCollectionIdAndCollectorId(2L, 1L)).thenReturn(1);

        service.deleteCollection(1L, 2L);

        verify(collectorPurchaseRepository).findAllByCollectorAndCollection(collector, collection);
        verify(collectorCollectionFigurineRepository).deleteByCollectionIdAndCollectorId(2L, 1L);
        verify(collectorCollectionRepository).deleteCollectionById(2L);
        verify(collectorCollectionRepository, never()).save(any());
    }

    @Test
    void deleteCollection_shouldPromoteAnotherCollectionToFavorite_whenDeletedCollectionWasFavorite() {
        CollectorCollection favorite = collection(2L, null, "Favorite", null, null);
        favorite.setFavorite(true);
        CollectorCollection other = collection(3L, null, "Other", null, null);
        Collector collector = collector(1L, favorite, other);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(favorite));
        when(collectorCollectionFigurineRepository.deleteByCollectionIdAndCollectorId(2L, 1L)).thenReturn(1);
        when(collectorPurchaseRepository.findAllByCollectorAndCollection(collector, favorite)).thenReturn(List.of());

        service.deleteCollection(1L, 2L);

        assertThat(other.isFavorite()).isTrue();
        verify(collectorCollectionRepository).save(other);
        verify(collectorCollectionRepository).deleteCollectionById(2L);
        verify(collectorPurchaseRepository).findAllByCollectorAndCollection(collector, favorite);
    }

    @Test
    void deleteCollection_shouldDeleteExistingPurchases_beforeRemovingCollection() {
        CollectorCollection collection = collection(2L, null, "Team", null, null);
        Collector collector = collector(1L, collection);
        CollectorPurchase purchase = new CollectorPurchase();

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(collection));
        when(collectorPurchaseRepository.findAllByCollectorAndCollection(collector, collection))
                .thenReturn(List.of(purchase));
        when(collectorCollectionFigurineRepository.deleteByCollectionIdAndCollectorId(2L, 1L)).thenReturn(1);

        service.deleteCollection(1L, 2L);

        verify(collectorPurchaseRepository).deleteAll(List.of(purchase));
        verify(collectorPurchaseRepository).flush();
        verify(collectorCollectionRepository).deleteCollectionById(2L);
    }

    @Test
    void deleteCollection_shouldNotPromoteAnotherFavorite_whenDeletedFavoriteWasTheOnlyCollection() {
        CollectorCollection favorite = collection(2L, null, "Favorite", null, null);
        favorite.setFavorite(true);
        Collector collector = collector(1L, favorite);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(favorite));
        when(collectorCollectionFigurineRepository.deleteByCollectionIdAndCollectorId(2L, 1L)).thenReturn(1);
        when(collectorPurchaseRepository.findAllByCollectorAndCollection(collector, favorite)).thenReturn(List.of());

        service.deleteCollection(1L, 2L);

        verify(collectorCollectionRepository, never()).save(any());
        verify(collectorCollectionRepository).deleteCollectionById(2L);
        verify(collectorPurchaseRepository).findAllByCollectorAndCollection(collector, favorite);
    }

    @Test
    void deleteCollection_shouldThrowCollectorCollectionNotFoundException_whenCollectionIsMissing() {
        Collector collector = collector(1L, collection(3L, null, "Other", null, null));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteCollection(1L, 2L))
                .isInstanceOf(CollectorCollectionNotFoundException.class)
                .hasMessage("Collector collection with id 2 was not found");

        verifyNoInteractions(collectorCollectionFigurineRepository);
    }

    @Test
    void deleteCollection_shouldThrowCollectorCollectionNotFoundException_whenCollectorDoesNotOwnCollection() {
        CollectorCollection targetCollection = collection(2L, null, "Team", null, null);
        Collector collector = collector(1L, collection(3L, null, "Other", null, null));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(targetCollection));

        assertThatThrownBy(() -> service.deleteCollection(1L, 2L))
                .isInstanceOf(CollectorCollectionNotFoundException.class)
                .hasMessage("Collector collection with id 2 was not found");

        verifyNoInteractions(collectorCollectionFigurineRepository);
    }

    @Test
    void deleteCollection_shouldThrowCollectorNotFoundException_whenCollectorIsMissing() {
        when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteCollection(1L, 2L)).isInstanceOf(CollectorNotFoundException.class)
                .hasMessage("Collector with id 1 was not found");

        verifyNoInteractions(collectorCollectionRepository, collectorCollectionFigurineRepository);
    }

    @Test
    void updateCollectionAsFavorite_shouldSetTargetCollectionAsFavorite_andUnsetOthers() {
        CollectorCollection current = collection(2L, null, "Current", "current.png", "current description");
        CollectorCollection sibling = collection(3L, null, "Other", null, null);
        sibling.setFavorite(true);
        Collector collector = collector(1L, current, sibling);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(current));

        service.updateCollectionAsFavorite(1L, 2L);

        assertThat(current.isFavorite()).isTrue();
        assertThat(sibling.isFavorite()).isFalse();
        verify(collectorCollectionRepository).save(current);
    }

    @Test
    void updateCollectionAsFavorite_shouldThrowCollectorCollectionNotFoundException_whenCollectionIsMissing() {
        Collector collector = collector(1L);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCollectionAsFavorite(1L, 2L))
                .isInstanceOf(CollectorCollectionNotFoundException.class)
                .hasMessage("Collector collection with id 2 was not found");
    }

    @Test
    void updateCollectionAsFavorite_shouldThrowCollectorCollectionNotFoundException_whenCollectorDoesNotOwnCollection() {
        CollectorCollection targetCollection = collection(2L, null, "Target", null, null);
        Collector collector = collector(1L, collection(3L, null, "Other", null, null));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(targetCollection));

        assertThatThrownBy(() -> service.updateCollectionAsFavorite(1L, 2L))
                .isInstanceOf(CollectorCollectionNotFoundException.class)
                .hasMessage("Collector collection with id 2 was not found");
    }

    @Test
    void updateCollectionAsFavorite_shouldThrowCollectorNotFoundException_whenCollectorIsMissing() {
        when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCollectionAsFavorite(1L, 2L))
                .isInstanceOf(CollectorNotFoundException.class).hasMessage("Collector with id 1 was not found");

        verifyNoInteractions(collectorCollectionRepository, collectorCollectionFigurineRepository);
    }

    @Test
    void updateCollection_shouldReturnUpdatedCollection_whenNameIsUnique() {
        CollectorCollection current = collection(2L, null, "Old", "old.png", "old description");
        CollectorCollection sibling = collection(3L, null, "Other", null, null);
        Collector collector = collector(1L, current, sibling);
        CollectorCollectionReq request = new CollectorCollectionReq(false, "New", "new.png", "new description");

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));

        CollectorCollectionResp response = service.updateCollection(1L, 2L, request);

        assertThat(response).isEqualTo(
                new CollectorCollectionResp(2L, "New", "new.png", "new description", false, 0, 0, List.of()));
        assertThat(current.getName()).isEqualTo("New");
        assertThat(current.getImageUrl()).isEqualTo("new.png");
        assertThat(current.getDescription()).isEqualTo("new description");
        verify(collectorCollectionRepository, never()).save(any());
    }

    @Test
    void updateCollection_shouldAllowCurrentCollectionName_whenCheckingForDuplicateNames() {
        CollectorCollection current = collection(2L, null, "Old", "old.png", "old description");
        Collector collector = collector(1L, current);
        CollectorCollectionReq request = new CollectorCollectionReq(false, "Old", "new.png", "new description");

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));

        CollectorCollectionResp response = service.updateCollection(1L, 2L, request);

        assertThat(response).isEqualTo(
                new CollectorCollectionResp(2L, "Old", "new.png", "new description", false, 0, 0, List.of()));
        assertThat(current.getImageUrl()).isEqualTo("new.png");
        assertThat(current.getDescription()).isEqualTo("new description");
    }

    @Test
    void updateCollection_shouldThrowCollectorCollectionAlreadyExistsException_whenAnotherOwnedCollectionHasSameName() {
        CollectorCollection current = collection(2L, null, "Old", "old.png", "old description");
        CollectorCollection sibling = collection(3L, null, "New", null, null);
        Collector collector = collector(1L, current, sibling);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));

        assertThatThrownBy(() -> service.updateCollection(1L, 2L,
                new CollectorCollectionReq(false, "New", "new.png", "new description")))
                .isInstanceOf(CollectorCollectionAlreadyExistsException.class)
                .hasMessage("Collector collection with name 'New' already exists");

        verifyNoInteractions(collectorCollectionRepository);
        assertThat(current.getName()).isEqualTo("Old");
        assertThat(sibling.getName()).isEqualTo("New");
    }

    @Test
    void updateCollection_shouldThrowCollectorCollectionAlreadyExistsException_whenAnotherCollectionUsesRequestedName() {
        CollectorCollection current = collection(2L, null, "Old", "old.png", "old description");
        CollectorCollection sibling = collection(3L, null, "New", null, null);
        Collector collector = collector(1L, current, sibling);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));

        assertThatThrownBy(() -> service.updateCollection(1L, 2L,
                new CollectorCollectionReq(false, "New", "new.png", "new description")))
                .isInstanceOf(CollectorCollectionAlreadyExistsException.class)
                .hasMessage("Collector collection with name 'New' already exists");

        verifyNoInteractions(collectorCollectionRepository);
    }

    @Test
    void updateCollection_shouldThrowCollectorCollectionNotFoundException_whenCollectorDoesNotOwnCollection() {
        Collector collector = collector(1L, collection(3L, null, "Other", null, null));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));

        assertThatThrownBy(() -> service.updateCollection(1L, 2L,
                new CollectorCollectionReq(false, "New", "new.png", "new description")))
                .isInstanceOf(CollectorCollectionNotFoundException.class)
                .hasMessage("Collector collection with id 2 was not found");

        verifyNoInteractions(collectorCollectionRepository);
    }

    @Test
    void updateCollection_shouldThrowCollectorNotFoundException_whenCollectorIsMissing() {
        when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCollection(1L, 2L,
                new CollectorCollectionReq(false, "New", "new.png", "new description")))
                .isInstanceOf(CollectorNotFoundException.class).hasMessage("Collector with id 1 was not found");
    }

    @Test
    void duplicateCollection_shouldReturnNewCollectionId_andCopyFigurines_whenSourceCollectionHasDescriptionAndImage() {
        stubCollectionMapping();
        stubCollectionItemCopy();

        Collector collector = collector(1L);
        CollectorCollection source = collection(2L, collector, "Seiya", "image.png", "original description");
        Figurine figurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));
        CollectorCollectionFigurine assignment = collectionFigurine(source, figurine, 2, Condition.OPENED, true,
                PAST_INSTANT);
        source.setFigurines(new ArrayList<>(List.of(assignment)));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findByCollector(collector)).thenReturn(List.of(source));
        when(collectorCollectionRepository.findByCollectorAndName(collector, "Seiya copy"))
                .thenReturn(Optional.empty());
        when(collectorCollectionRepository.save(any(CollectorCollection.class))).thenAnswer(invocation -> {
            CollectorCollection saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });
        when(collectorCollectionFigurineRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        long newId = service.duplicateCollection(1L, 2L);

        assertThat(newId).isEqualTo(99L);

        ArgumentCaptor<CollectorCollection> collectionCaptor = ArgumentCaptor.forClass(CollectorCollection.class);
        verify(collectorCollectionRepository).save(collectionCaptor.capture());
        CollectorCollection duplicatedCollection = collectionCaptor.getValue();
        assertThat(duplicatedCollection.getName()).isEqualTo("Seiya copy");
        assertThat(duplicatedCollection.getImageUrl()).isEqualTo("image.png");
        assertThat(duplicatedCollection.getDescription()).isEqualTo("original description copy");

        ArgumentCaptor<List<CollectorCollectionFigurine>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(collectorCollectionFigurineRepository).saveAll(itemsCaptor.capture());
        assertThat(itemsCaptor.getValue()).hasSize(1);
        CollectorCollectionFigurine duplicatedItem = itemsCaptor.getValue().getFirst();
        assertThat(duplicatedItem.getCollection()).isEqualTo(duplicatedCollection);
        assertThat(duplicatedItem.getFigurine()).isEqualTo(figurine);
        assertThat(duplicatedItem.getQuantity()).isEqualTo(2);
        assertThat(duplicatedItem.getCondition()).isEqualTo(Condition.OPENED);
        assertThat(duplicatedItem.isOwned()).isTrue();
        assertThat(duplicatedItem.getAddedAt()).isEqualTo(PAST_INSTANT);
    }

    @Test
    void duplicateCollection_shouldReturnNewCollectionId_andPreserveNullMetadata_whenSourceCollectionMetadataIsMissing() {
        stubCollectionMapping();
        stubCollectionItemCopy();

        Collector collector = collector(1L);
        CollectorCollection source = collection(2L, collector, "Seiya", null, null);
        Figurine figurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));
        CollectorCollectionFigurine assignment = collectionFigurine(source, figurine, 1, Condition.SEALED, false, null);
        source.setFigurines(new ArrayList<>(List.of(assignment)));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findByCollector(collector)).thenReturn(List.of(source));
        when(collectorCollectionRepository.findByCollectorAndName(collector, "Seiya copy"))
                .thenReturn(Optional.empty());
        when(collectorCollectionRepository.save(any(CollectorCollection.class))).thenAnswer(invocation -> {
            CollectorCollection saved = invocation.getArgument(0);
            saved.setId(42L);
            return saved;
        });
        when(collectorCollectionFigurineRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        long newId = service.duplicateCollection(1L, 2L);

        assertThat(newId).isEqualTo(42L);

        ArgumentCaptor<CollectorCollection> collectionCaptor = ArgumentCaptor.forClass(CollectorCollection.class);
        verify(collectorCollectionRepository).save(collectionCaptor.capture());
        assertThat(collectionCaptor.getValue().getImageUrl()).isNull();
        assertThat(collectionCaptor.getValue().getDescription()).isNull();
    }

    @Test
    void duplicateCollection_shouldThrowCollectorCollectionAlreadyExistsException_whenDuplicateNameIsTaken() {
        Collector collector = collector(1L);
        CollectorCollection source = collection(2L, collector, "Seiya", null, null);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findByCollector(collector)).thenReturn(List.of(source));
        when(collectorCollectionRepository.findByCollectorAndName(collector, "Seiya copy"))
                .thenReturn(Optional.of(collection(3L, collector, "Seiya copy", null, null)));

        assertThatThrownBy(() -> service.duplicateCollection(1L, 2L))
                .isInstanceOf(CollectorCollectionAlreadyExistsException.class)
                .hasMessage("Collector collection with name 'Seiya copy' already exists");

        verifyNoInteractions(collectorCollectionFigurineRepository);
    }

    @Test
    void duplicateCollection_shouldThrowCollectorCollectionNotFoundException_whenSourceCollectionDoesNotBelongToCollector() {
        Collector collector = collector(1L, collection(3L, null, "Other", null, null));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findByCollector(collector))
                .thenReturn(List.of(collection(3L, null, "Other", null, null)));

        assertThatThrownBy(() -> service.duplicateCollection(1L, 2L))
                .isInstanceOf(CollectorCollectionNotFoundException.class)
                .hasMessage("Collector collection with id 2 was not found");
    }

    @Test
    void duplicateCollection_shouldThrowCollectorNotFoundException_whenCollectorIsMissing() {
        when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.duplicateCollection(1L, 2L)).isInstanceOf(CollectorNotFoundException.class)
                .hasMessage("Collector with id 1 was not found");
    }

    private void stubCollectionMapping() {
        when(collectorMapper.toCollectorCollection(any(CollectorCollectionReq.class), anyBoolean(),
                any(Collector.class))).thenAnswer(invocation -> {
                    CollectorCollectionReq request = invocation.getArgument(0);
                    boolean isFavorite = invocation.getArgument(1);
                    Collector owner = invocation.getArgument(2);

                    CollectorCollection mapped = collection(null, owner, request.name(), request.imageUrl(),
                            request.description());
                    mapped.setFavorite(isFavorite);
                    return mapped;
                });
    }

    private void stubCollectionItemCopy() {
        when(collectorMapper.copy(any(CollectorCollectionFigurine.class))).thenAnswer(invocation -> {
            CollectorCollectionFigurine source = invocation.getArgument(0);
            CollectorCollectionFigurine copy = new CollectorCollectionFigurine();
            copy.setFigurine(source.getFigurine());
            copy.setQuantity(source.getQuantity());
            copy.setCondition(source.getCondition());
            copy.setOwned(source.isOwned());
            copy.setAddedAt(source.getAddedAt());
            return copy;
        });
    }

    private Collector collector(long id, CollectorCollection... collections) {
        Collector collector = new Collector();
        collector.setId(id);
        collector.setCollections(new ArrayList<>(List.of(collections)));
        return collector;
    }

    private CollectorCollection collection(Long id, Collector owner, String name, String imageUrl, String description) {
        CollectorCollection collection = new CollectorCollection();
        collection.setId(id);
        collection.setCollector(owner);
        collection.setName(name);
        collection.setImageUrl(imageUrl);
        collection.setDescription(description);
        collection.setFigurines(new ArrayList<>());
        return collection;
    }

    private CollectorCollectionFigurine collectionFigurine(CollectorCollection collection, Figurine figurine,
            int quantity, Condition condition, boolean owned, Instant addedAt) {
        CollectorCollectionFigurine collectorCollectionFigurine = new CollectorCollectionFigurine();
        collectorCollectionFigurine.setCollection(collection);
        collectorCollectionFigurine.setFigurine(figurine);
        collectorCollectionFigurine.setQuantity(quantity);
        collectorCollectionFigurine.setCondition(condition);
        collectorCollectionFigurine.setOwned(owned);
        collectorCollectionFigurine.setAddedAt(addedAt);
        return collectorCollectionFigurine;
    }

    private Figurine figurine(long id, String normalizedName, ReleaseStatus releaseStatus, LocalDate releaseDate) {
        Figurine figurine = new Figurine();
        figurine.setId(id);
        figurine.setNormalizedName(normalizedName);
        figurine.setDisplayName(normalizedName + " display");
        figurine.setCurrentReleaseStatus(releaseStatus);

        FigurineDistributor figurineDistributor = new FigurineDistributor();
        figurineDistributor.setFigurine(figurine);
        figurineDistributor.setDistributor(distributor());
        figurineDistributor.setCurrency(CurrencyCode.USD);
        figurineDistributor.setReleaseDate(releaseDate);
        figurineDistributor.setReleaseDateConfirmed(true);

        figurine.setDistributors(new ArrayList<>(List.of(figurineDistributor)));
        return figurine;
    }

    private Distributor distributor() {
        Distributor distributor = new Distributor();
        distributor.setName(DistributorName.BANDAI);
        distributor.setCountry(CountryCode.JP);
        return distributor;
    }
}
