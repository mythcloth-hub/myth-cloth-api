package com.mesofi.mythclothapi.collectorscollections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
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
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionSummaryProjection;
import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.distributors.model.Distributor;
import com.mesofi.mythclothapi.distributors.model.DistributorName;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurines.FigurineFilter;
import com.mesofi.mythclothapi.figurines.FigurineNotFoundException;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;
import com.mesofi.mythclothapi.figurines.repository.CollectablePageImpl;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;
import com.mesofi.mythclothapi.figurines.repository.projection.FigurineCatalogSummaryProjection;

@ExtendWith(MockitoExtension.class)
class CollectorCollectionFigurineServiceTest {

    @InjectMocks
    private CollectorCollectionFigurineService service;

    @Mock
    private CollectorCollectionFigurineRepository collectorCollectionFigurineRepository;
    @Mock
    private CollectorCollectionRepository collectorCollectionRepository;
    @Mock
    private CollectorRepository collectorRepository;
    @Mock
    private FigurineRepository figurineRepository;
    @Mock
    private CollectorMapper collectorMapper;

    @Test
    void addFigurineToCollection_shouldThrowIllegalArgumentException_whenFigurineDoesNotExist() {
        when(figurineRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addFigurineToCollection(2L, 9L)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Figurine with id 9 not found");

        verify(figurineRepository).findById(9L);
        verifyNoInteractions(collectorCollectionRepository, collectorCollectionFigurineRepository, collectorRepository);
    }

    @Test
    void addFigurineToCollection_shouldCreateDefaultCollectionAndSaveAssignment_whenCollectionDoesNotExist() {
        Collector collector = collector(4L);
        Figurine figurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));
        CollectorCollection collection = collection(22L, collector, "My Myth Collection", null, null);

        when(figurineRepository.findById(9L)).thenReturn(Optional.of(figurine));
        when(collectorCollectionRepository.findById(22L)).thenReturn(Optional.empty());
        when(collectorRepository.findById(22L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findByCollectorAndName(collector, "My Myth Collection"))
                .thenReturn(Optional.empty());
        when(collectorCollectionRepository.save(any())).thenReturn(collection);
        when(collectorCollectionFigurineRepository.findByCollectionAndFigurine(collection, figurine))
                .thenReturn(Optional.empty());

        service.addFigurineToCollection(22L, 9L);

        ArgumentCaptor<CollectorCollection> collectionCaptor = ArgumentCaptor.forClass(CollectorCollection.class);
        verify(collectorCollectionRepository).save(collectionCaptor.capture());
        assertThat(collectionCaptor.getValue().getCollector()).isEqualTo(collector);
        assertThat(collectionCaptor.getValue().getName()).isEqualTo("My Myth Collection");

        ArgumentCaptor<CollectorCollectionFigurine> figurineCaptor = ArgumentCaptor
                .forClass(CollectorCollectionFigurine.class);
        verify(collectorCollectionFigurineRepository).save(figurineCaptor.capture());
        assertThat(figurineCaptor.getValue().getCollection()).isEqualTo(collection);
        assertThat(figurineCaptor.getValue().getFigurine()).isEqualTo(figurine);
    }

    @Test
    void addFigurineToCollection_shouldSaveAssignment_whenCollectionAlreadyExists() {
        CollectorCollection collection = collection(22L, collector(4L), "My Collection", null, null);
        Figurine figurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));

        when(figurineRepository.findById(9L)).thenReturn(Optional.of(figurine));
        when(collectorCollectionRepository.findById(22L)).thenReturn(Optional.of(collection));
        when(collectorCollectionFigurineRepository.findByCollectionAndFigurine(collection, figurine))
                .thenReturn(Optional.empty());

        service.addFigurineToCollection(22L, 9L);

        verify(collectorCollectionRepository, never()).save(any());
        verify(collectorCollectionFigurineRepository).save(any(CollectorCollectionFigurine.class));
    }

    @Test
    void addFigurineToCollection_shouldThrowIllegalArgumentException_whenFigurineAlreadyExistsInCollection() {
        CollectorCollection collection = collection(22L, null, "Collection", null, null);
        Figurine figurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));

        when(figurineRepository.findById(9L)).thenReturn(Optional.of(figurine));
        when(collectorCollectionRepository.findById(22L)).thenReturn(Optional.of(collection));
        when(collectorCollectionFigurineRepository.findByCollectionAndFigurine(collection, figurine))
                .thenReturn(Optional.of(collectionFigurine(collection, figurine, 1, Condition.SEALED)));

        assertThatThrownBy(() -> service.addFigurineToCollection(22L, 9L)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Figurine with id 9 already in collection with id 22");

        verify(collectorCollectionFigurineRepository, never()).save(any());
    }

    @Test
    void assignFigurinesToCollections_shouldThrowFigurineNotFoundException_whenFigurineIsMissing() {
        AssignFigurinesReq request = new AssignFigurinesReq(List.of(9L), CollectionAssignmentMode.AUTO, null, null);

        when(figurineRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignFigurinesToCollections(1L, request))
                .isInstanceOf(FigurineNotFoundException.class).hasMessage("Figurine with id 9 was not found");

        verify(figurineRepository).findById(9L);
        verifyNoInteractions(collectorCollectionRepository, collectorCollectionFigurineRepository, collectorRepository);
    }

    @Test
    void assignFigurinesToCollections_shouldAssignToDefaultCollection_whenModeIsAuto() {
        Collector collector = collector(1L);
        Figurine figurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));
        CollectorCollection collection = collection(77L, collector, "My Myth Collection", null, null);
        AssignFigurinesReq request = new AssignFigurinesReq(List.of(9L), CollectionAssignmentMode.AUTO, null, null);

        when(figurineRepository.findById(9L)).thenReturn(Optional.of(figurine));
        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findByCollectorAndName(collector, "My Myth Collection"))
                .thenReturn(Optional.empty());
        when(collectorCollectionRepository.save(any())).thenReturn(collection);
        when(collectorCollectionFigurineRepository.findByCollectionAndFigurine(collection, figurine))
                .thenReturn(Optional.empty());

        service.assignFigurinesToCollections(1L, request);

        ArgumentCaptor<CollectorCollection> collectionCaptor = ArgumentCaptor.forClass(CollectorCollection.class);
        verify(collectorCollectionRepository).save(collectionCaptor.capture());
        assertThat(collectionCaptor.getValue().getCollector()).isEqualTo(collector);
        assertThat(collectionCaptor.getValue().getName()).isEqualTo("My Myth Collection");
        verify(collectorRepository).findById(1L);
        verify(collectorCollectionRepository).findByCollectorAndName(collector, "My Myth Collection");
        verify(collectorCollectionFigurineRepository).save(any(CollectorCollectionFigurine.class));
    }

    @Test
    void assignFigurinesToCollections_shouldThrowCollectorCollectionAlreadyExistsException_whenModeIsCreateAndNameIsTaken() {
        Collector collector = collector(1L);
        CollectorCollection existing = collection(2L, collector, "Taken", null, null);
        AssignFigurinesReq request = new AssignFigurinesReq(List.of(9L), CollectionAssignmentMode.CREATE, null,
                new CollectorCollectionReq("Taken", null, null));
        Figurine figurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));

        when(figurineRepository.findById(9L)).thenReturn(Optional.of(figurine));
        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findByCollectorAndName(collector, "Taken"))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.assignFigurinesToCollections(1L, request))
                .isInstanceOf(CollectorCollectionAlreadyExistsException.class)
                .hasMessage("Collector collection with name 'Taken' already exists");

        verify(collectorRepository).findById(1L);
        verify(collectorCollectionRepository).findByCollectorAndName(collector, "Taken");
        verify(collectorCollectionFigurineRepository, never()).save(any());
    }

    @Test
    void assignFigurinesToCollections_shouldThrowCollectorNotFoundException_whenModeIsAutoAndCollectorIsMissing() {
        Figurine figurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));
        AssignFigurinesReq request = new AssignFigurinesReq(List.of(9L), CollectionAssignmentMode.AUTO, null, null);

        when(figurineRepository.findById(9L)).thenReturn(Optional.of(figurine));
        when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignFigurinesToCollections(1L, request))
                .isInstanceOf(CollectorNotFoundException.class).hasMessage("Collector with id 1 was not found");

        verify(collectorRepository).findById(1L);
        verifyNoInteractions(collectorCollectionRepository, collectorCollectionFigurineRepository);
    }

    @Test
    void assignFigurinesToCollections_shouldAssignToExistingCollections_andIncrementExistingQuantity() {
        Collector collector = collector(1L);
        CollectorCollection firstCollection = collection(2L, collector, "Team A", null, null);
        CollectorCollection secondCollection = collection(3L, collector, "Team B", null, null);
        Figurine existingFigurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));
        CollectorCollectionFigurine existingAssignment = collectionFigurine(firstCollection, existingFigurine, 2,
                Condition.SEALED);
        AssignFigurinesReq request = new AssignFigurinesReq(List.of(9L), CollectionAssignmentMode.EXISTING,
                List.of(2L, 3L), null);

        when(figurineRepository.findById(9L)).thenReturn(Optional.of(existingFigurine));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(firstCollection));
        when(collectorCollectionRepository.findById(3L)).thenReturn(Optional.of(secondCollection));
        when(collectorCollectionFigurineRepository.findByCollectionAndFigurine(firstCollection, existingFigurine))
                .thenReturn(Optional.of(existingAssignment));
        when(collectorCollectionFigurineRepository.findByCollectionAndFigurine(secondCollection, existingFigurine))
                .thenReturn(Optional.empty());

        service.assignFigurinesToCollections(1L, request);

        ArgumentCaptor<CollectorCollectionFigurine> savedAssignments = ArgumentCaptor
                .forClass(CollectorCollectionFigurine.class);
        verify(collectorCollectionFigurineRepository, org.mockito.Mockito.times(2)).save(savedAssignments.capture());
        assertThat(existingAssignment.getQuantity()).isEqualTo(3);
        assertThat(savedAssignments.getAllValues()).hasSize(2);
        assertThat(savedAssignments.getAllValues().get(0)).isEqualTo(existingAssignment);
        assertThat(savedAssignments.getAllValues().get(1).getCollection()).isEqualTo(secondCollection);
        assertThat(savedAssignments.getAllValues().get(1).getFigurine()).isEqualTo(existingFigurine);
    }

    @Test
    void assignFigurinesToCollections_shouldThrowCollectorCollectionNotFoundException_whenModeIsExistingAndCollectionIsMissing() {
        AssignFigurinesReq request = new AssignFigurinesReq(List.of(9L), CollectionAssignmentMode.EXISTING, List.of(2L),
                null);
        Figurine figurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));

        when(figurineRepository.findById(9L)).thenReturn(Optional.of(figurine));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignFigurinesToCollections(1L, request))
                .isInstanceOf(CollectorCollectionNotFoundException.class)
                .hasMessage("Collector collection with id 2 was not found");

        verify(collectorCollectionRepository).findById(2L);
        verifyNoInteractions(collectorRepository, collectorCollectionFigurineRepository);
    }

    @Test
    void assignFigurinesToCollections_shouldThrowIllegalArgumentException_whenModeIsNull() {
        AssignFigurinesReq request = new AssignFigurinesReq(List.of(9L), null, null, null);
        Figurine figurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));

        when(figurineRepository.findById(9L)).thenReturn(Optional.of(figurine));

        assertThatThrownBy(() -> service.assignFigurinesToCollections(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported collection assignment mode: null");
    }

    @Test
    void retrieveCollectionFigurines_shouldReturnMappedFigurines_whenCollectorOwnsCollection() {
        CollectorCollection collection = collection(2L, null, "Team", null, null);
        Figurine released = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));
        Figurine announced = figurine(10L, "shiryu", ReleaseStatus.ANNOUNCED, LocalDate.of(2023, 12, 1));
        CollectorCollectionFigurine collected = collectionFigurine(collection, released, 2, Condition.SEALED);
        collection.setFigurines(new ArrayList<>(List.of(collected)));
        Collector collector = collectorWithCollections(1L, collection);
        List<Figurine> figurines = List.of(released, announced);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(collection));
        when(figurineRepository.findPaginated(any(FigurineFilter.class), any(PageRequest.class)))
                .thenReturn(new CollectablePageImpl<>(figurines, PageRequest.of(0, 50), 3, 2));
        when(collectorMapper.toCollectorCollectionFigurineResp(any(Figurine.class), any(ReleaseStatus.class),
                anyBoolean(), anyInt())).thenAnswer(invocation -> {
                    Figurine figurine = invocation.getArgument(0);
                    ReleaseStatus releaseStatus = invocation.getArgument(1);
                    boolean isCollected = invocation.getArgument(2);
                    int ownedQuantity = invocation.getArgument(3);
                    return new CollectorCollectionFigurineResp(figurine.getId(), figurine.getNormalizedName(),
                            releaseStatus, null, null, isCollected, ownedQuantity);
                });

        Page<CollectorCollectionFigurineResp> response = service.retrieveCollectionFigurines(1L, 2L, false, 0, 50);

        assertThat(response.getContent()).containsExactly(
                new CollectorCollectionFigurineResp(9L, "seiya", ReleaseStatus.RELEASED, null, null, true, 2),
                new CollectorCollectionFigurineResp(10L, "shiryu", ReleaseStatus.ANNOUNCED, null, null, false, 0));
        verify(collectorMapper).toCollectorCollectionFigurineResp(released, ReleaseStatus.RELEASED, true, 2);
        verify(collectorMapper).toCollectorCollectionFigurineResp(announced, ReleaseStatus.ANNOUNCED, false, 0);
    }

    @Test
    void retrieveCollectionFigurines_shouldUseReleasedAndAnnouncedFilter_whenIncludeRestocksIsTrue() {
        CollectorCollection collection = collection(2L, null, "Team", null, null);
        Collector collector = collectorWithCollections(1L, collection);
        Figurine figurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(collection));
        when(figurineRepository.findPaginated(any(FigurineFilter.class), any(PageRequest.class)))
                .thenReturn(new CollectablePageImpl<>(List.of(figurine), PageRequest.of(0, 10), 1, 1));

        service.retrieveCollectionFigurines(1L, 2L, true, 0, 10);

        ArgumentCaptor<FigurineFilter> filterCaptor = ArgumentCaptor.forClass(FigurineFilter.class);
        verify(figurineRepository).findPaginated(filterCaptor.capture(), any(PageRequest.class));
        assertThat(filterCaptor.getValue().restocks()).isNull();
        assertThat(filterCaptor.getValue().releaseStatuses()).containsExactly("RELEASED", "ANNOUNCED");
    }

    @Test
    void assignFigurinesToCollections_shouldMarkFirstCollectionAsFavorite_whenCollectorHasNoCollections() {
        Collector collector = collector(1L);
        Figurine figurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));
        AssignFigurinesReq request = new AssignFigurinesReq(List.of(9L), CollectionAssignmentMode.AUTO, null, null);
        when(figurineRepository.findById(9L)).thenReturn(Optional.of(figurine));
        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findByCollectorAndName(collector, "My Myth Collection"))
                .thenReturn(Optional.empty());
        when(collectorCollectionRepository.countByCollector(collector)).thenReturn(0L);
        when(collectorCollectionRepository.save(any())).thenAnswer(invocation -> {
            CollectorCollection saved = invocation.getArgument(0);
            saved.setId(77L);
            return saved;
        });

        service.assignFigurinesToCollections(1L, request);

        ArgumentCaptor<CollectorCollection> collectionCaptor = ArgumentCaptor.forClass(CollectorCollection.class);
        verify(collectorCollectionRepository).save(collectionCaptor.capture());
        assertThat(collectionCaptor.getValue().isFavorite()).isTrue();
        assertThat(collectionCaptor.getValue().getName()).isEqualTo("My Myth Collection");
    }

    @Test
    void assignFigurinesToCollections_shouldNotMarkCollectionAsFavorite_whenCollectorAlreadyHasCollections() {
        Collector collector = collector(1L, collection(10L, null, "Existing", null, null));
        Figurine figurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));
        AssignFigurinesReq request = new AssignFigurinesReq(List.of(9L), CollectionAssignmentMode.AUTO, null, null);

        when(figurineRepository.findById(9L)).thenReturn(Optional.of(figurine));
        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findByCollectorAndName(collector, "My Myth Collection"))
                .thenReturn(Optional.empty());
        when(collectorCollectionRepository.countByCollector(collector)).thenReturn(1L);
        when(collectorCollectionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.assignFigurinesToCollections(1L, request);

        ArgumentCaptor<CollectorCollection> collectionCaptor = ArgumentCaptor.forClass(CollectorCollection.class);
        verify(collectorCollectionRepository).save(collectionCaptor.capture());
        assertThat(collectionCaptor.getValue().isFavorite()).isFalse();
        assertThat(collectionCaptor.getValue().getName()).isEqualTo("My Myth Collection");
    }

    @Test
    void createCollection_shouldThrowCollectorCollectionLimitReachedException_whenCollectorHasReachedMaxCollections() {
        Collector collector = collector(1L);
        Figurine figurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));
        AssignFigurinesReq request = new AssignFigurinesReq(List.of(9L), CollectionAssignmentMode.AUTO, null, null);

        when(figurineRepository.findById(9L)).thenReturn(Optional.of(figurine));
        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findByCollectorAndName(collector, "My Myth Collection"))
                .thenReturn(Optional.empty());
        when(collectorCollectionRepository.countByCollector(collector)).thenReturn(3L);

        assertThatThrownBy(() -> service.assignFigurinesToCollections(1L, request))
                .isInstanceOf(CollectorCollectionLimitReachedException.class)
                .hasMessage("Collector account with ID '1' has reached the limit of collector collections: 3");

        verify(collectorCollectionRepository, never()).save(any());
    }

    @Test
    void retrieveCollectionFigurines_shouldThrowCollectorNotFoundException_whenCollectorIsMissing() {
        when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.retrieveCollectionFigurines(1L, 2L, false, 0, 50))
                .isInstanceOf(CollectorNotFoundException.class).hasMessage("Collector with id 1 was not found");

        verify(collectorRepository).findById(1L);
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

        verify(collectorRepository).findById(1L);
        verify(collectorCollectionRepository).findById(2L);
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

        verify(collectorRepository).findById(1L);
        verify(collectorCollectionRepository).findById(2L);
        verifyNoInteractions(figurineRepository, collectorMapper);
    }

    @Test
    void retrieveCollectionSummary_shouldReturnSummary_whenCollectorOwnsCollection() {
        CollectorCollection collection = collection(2L, null, "Team", null, null);
        Collector collector = collectorWithCollections(1L, collection);

        FigurineCatalogSummaryProjection catalogSummary = new FigurineCatalogSummaryProjection() {
            @Override
            public int getTotalFigurines() {
                return 435;
            }

            @Override
            public int getTotalReleased() {
                return 426;
            }

            @Override
            public int getTotalAnnounced() {
                return 9;
            }
        };
        CollectorCollectionSummaryProjection collectionSummary = new CollectorCollectionSummaryProjection() {
            @Override
            public int getPreorderedQuantity() {
                return 3;
            }

            @Override
            public int getReleasedQuantity() {
                return 3;
            }

            @Override
            public int getPreorderedFigurines() {
                return 1;
            }

            @Override
            public int getReleasedFigurines() {
                return 1;
            }
        };

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(figurineRepository.getFigurineCatalogSummary(false)).thenReturn(catalogSummary);
        when(collectorCollectionRepository.getCollectorCollectionSummary(2L, false)).thenReturn(collectionSummary);
        when(collectorMapper.toCollectorCollectionSummaryResp(collectionSummary, 426))
                .thenReturn(new CollectorCollectionSummaryStatsResp(3, 3, 1, 1, 425));

        CollectorCollectionSummaryResp response = service.retrieveCollectionSummary(1L, 2L, false);

        assertThat(response)
                .isEqualTo(new CollectorCollectionSummaryResp(new CollectorCollectionCatalogSummaryResp(435, 9, 426),
                        new CollectorCollectionSummaryStatsResp(3, 3, 1, 1, 425)));
        verify(collectorRepository).findById(1L);
        verify(figurineRepository).getFigurineCatalogSummary(false);
        verify(collectorCollectionRepository).getCollectorCollectionSummary(2L, false);
        verify(collectorMapper).toCollectorCollectionSummaryResp(collectionSummary, 426);
    }

    @Test
    void retrieveCollectionSummary_shouldThrowCollectorNotFoundException_whenCollectorIsMissing() {
        when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.retrieveCollectionSummary(1L, 2L, false))
                .isInstanceOf(CollectorNotFoundException.class).hasMessage("Collector with id 1 was not found");

        verify(collectorRepository).findById(1L);
        verifyNoInteractions(figurineRepository, collectorCollectionRepository, collectorMapper);
    }

    @Test
    void retrieveCollectionSummary_shouldThrowCollectorCollectionNotFoundException_whenCollectorDoesNotOwnCollection() {
        Collector collector = collector(1L, collection(3L, null, "Other", null, null));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));

        assertThatThrownBy(() -> service.retrieveCollectionSummary(1L, 2L, false))
                .isInstanceOf(CollectorCollectionNotFoundException.class)
                .hasMessage("Collector collection with id 2 was not found");

        verify(collectorRepository).findById(1L);
        verifyNoInteractions(figurineRepository, collectorCollectionRepository, collectorMapper);
    }

    @Test
    void retrieveCollectionFigurine_shouldReturnMappedDetail_whenCollectorOwnsCollectionAndFigurineExists() {
        CollectorCollection collection = collection(2L, null, "Team", null, null);
        Collector collector = collectorWithCollections(1L, collection);
        Figurine figurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));
        CollectorCollectionFigurineDetailResp detail = new CollectorCollectionFigurineDetailResp("Seiya", List.of(),
                null, null, null);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(collection));
        when(figurineRepository.findById(9L)).thenReturn(Optional.of(figurine));
        when(collectorMapper.toCollectorCollectionFigurineDetailResp(figurine)).thenReturn(detail);

        CollectorCollectionFigurineDetailResp response = service.retrieveCollectionFigurine(1L, 2L, 9L);

        assertThat(response).isEqualTo(detail);
        verify(collectorMapper).toCollectorCollectionFigurineDetailResp(figurine);
    }

    @Test
    void retrieveCollectionFigurine_shouldThrowCollectorNotFoundException_whenCollectorIsMissing() {
        when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.retrieveCollectionFigurine(1L, 2L, 9L))
                .isInstanceOf(CollectorNotFoundException.class).hasMessage("Collector with id 1 was not found");

        verify(collectorRepository).findById(1L);
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

        verify(collectorRepository).findById(1L);
        verify(collectorCollectionRepository).findById(2L);
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

        verify(collectorRepository).findById(1L);
        verify(collectorCollectionRepository).findById(2L);
        verifyNoInteractions(figurineRepository, collectorMapper);
    }

    @Test
    void retrieveCollectionFigurine_shouldThrowFigurineNotFoundException_whenFigurineIsMissing() {
        CollectorCollection collection = collection(2L, null, "Team", null, null);
        Collector collector = collectorWithCollections(1L, collection);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(collection));
        when(figurineRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.retrieveCollectionFigurine(1L, 2L, 9L))
                .isInstanceOf(FigurineNotFoundException.class).hasMessage("Figurine with id 9 was not found");

        verify(figurineRepository).findById(9L);
        verifyNoInteractions(collectorMapper);
    }

    @Test
    void retrieveLatestFavoriteCollectionFigurines_shouldReturnMappedLatestFigurines_whenFavoriteCollectionExists() {
        CollectorCollection favorite = collection(2L, null, "Favorite", null, null);
        favorite.setFavorite(true);
        Collector collector = collectorWithCollections(1L, favorite);
        CollectorCollectionFigurine first = collectionFigurine(favorite,
                figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1)), 2, Condition.SEALED);
        CollectorCollectionFigurine second = collectionFigurine(favorite,
                figurine(10L, "shiryu", ReleaseStatus.ANNOUNCED, LocalDate.of(2024, 4, 1)), 1, Condition.OPENED);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionFigurineRepository.findByCollectionOrderByAddedAtDesc(favorite, PageRequest.of(0, 20)))
                .thenReturn(List.of(first, second));
        when(collectorMapper.toCollectorCollectionLatestFavoriteResp(first))
                .thenReturn(new CollectorCollectionLatestFavoriteResp(9L, "seiya", "image-1.png", 2));
        when(collectorMapper.toCollectorCollectionLatestFavoriteResp(second))
                .thenReturn(new CollectorCollectionLatestFavoriteResp(10L, "shiryu", "image-2.png", 1));

        List<CollectorCollectionLatestFavoriteResp> response = service.retrieveLatestFavoriteCollectionFigurines(1L,
                20);

        assertThat(response).containsExactly(new CollectorCollectionLatestFavoriteResp(9L, "seiya", "image-1.png", 2),
                new CollectorCollectionLatestFavoriteResp(10L, "shiryu", "image-2.png", 1));
        verify(collectorCollectionFigurineRepository).findByCollectionOrderByAddedAtDesc(favorite,
                PageRequest.of(0, 20));
        verify(collectorMapper).toCollectorCollectionLatestFavoriteResp(first);
        verify(collectorMapper).toCollectorCollectionLatestFavoriteResp(second);
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
    void deleteCollectionFigurine_shouldDeleteRelation_whenAssignmentExists() {
        CollectorCollection collection = collection(2L, null, "Team", null, null);
        Collector collector = collectorWithCollections(1L, collection);
        Figurine figurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));
        CollectorCollectionFigurine assignment = collectionFigurine(collection, figurine, 2, Condition.SEALED);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(collection));
        when(figurineRepository.findById(9L)).thenReturn(Optional.of(figurine));
        when(collectorCollectionFigurineRepository.findByCollectionAndFigurine(collection, figurine))
                .thenReturn(Optional.of(assignment));

        service.deleteCollectionFigurine(1L, 2L, 9L);

        verify(collectorCollectionFigurineRepository).delete(assignment);
    }

    @Test
    void deleteCollectionFigurine_shouldDoNothing_whenAssignmentDoesNotExist() {
        CollectorCollection collection = collection(2L, null, "Team", null, null);
        Collector collector = collectorWithCollections(1L, collection);
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

        verify(collectorRepository).findById(1L);
        verifyNoInteractions(collectorCollectionRepository, figurineRepository, collectorCollectionFigurineRepository);
    }

    @Test
    void deleteCollectionFigurine_shouldThrowCollectorCollectionNotFoundException_whenCollectionIsMissing() {
        Collector collector = collector(1L);
        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteCollectionFigurine(1L, 2L, 9L))
                .isInstanceOf(CollectorCollectionNotFoundException.class)
                .hasMessage("Collector collection with id 2 was not found");

        verify(collectorRepository).findById(1L);
        verify(collectorCollectionRepository).findById(2L);
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

        verify(collectorRepository).findById(1L);
        verify(collectorCollectionRepository).findById(2L);
        verifyNoInteractions(figurineRepository, collectorCollectionFigurineRepository);
    }

    @Test
    void deleteCollectionFigurine_shouldThrowFigurineNotFoundException_whenFigurineIsMissing() {
        CollectorCollection collection = collection(2L, null, "Team", null, null);
        Collector collector = collectorWithCollections(1L, collection);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(collection));
        when(figurineRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteCollectionFigurine(1L, 2L, 9L))
                .isInstanceOf(FigurineNotFoundException.class).hasMessage("Figurine with id 9 was not found");

        verify(figurineRepository).findById(9L);
        verifyNoInteractions(collectorCollectionFigurineRepository);
    }

    @Test
    void retrieveCollections_shouldReturnMappedCollections_whenCollectorExists() {
        CollectorCollection collection = collection(2L, null, "Team", null, null);
        Collector collector = collector(1L, collection);
        CollectorCollectionResp response = new CollectorCollectionResp(2L, "Team", null, null, false, 0, List.of());

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorMapper.toCollectorCollectionResp(collection)).thenReturn(response);

        List<CollectorCollectionResp> result = service.retrieveCollections(1L);

        assertThat(result).containsExactly(response);
        verify(collectorMapper).toCollectorCollectionResp(collection);
    }

    @Test
    void retrieveCollections_shouldThrowCollectorNotFoundException_whenCollectorIsMissing() {
        when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.retrieveCollections(1L)).isInstanceOf(CollectorNotFoundException.class)
                .hasMessage("Collector with id 1 was not found");

        verify(collectorRepository).findById(1L);
        verifyNoInteractions(collectorCollectionRepository, collectorMapper);
    }

    @Test
    void deleteCollection_shouldDeleteCollection_whenCollectorOwnsCollection() {
        CollectorCollection collection = collection(2L, null, "Team", null, null);
        Collector collector = collectorWithCollections(1L, collection);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(collection));
        when(collectorCollectionFigurineRepository.deleteByCollectionIdAndCollectorId(2L, 1L)).thenReturn(1);

        service.deleteCollection(1L, 2L);

        verify(collectorCollectionRepository).findById(2L);
        verify(collectorCollectionFigurineRepository).deleteByCollectionIdAndCollectorId(2L, 1L);
        verify(collectorCollectionRepository).deleteCollectionById(2L);
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

        service.deleteCollection(1L, 2L);

        assertThat(other.isFavorite()).isTrue();
        verify(collectorCollectionRepository).save(other);
        verify(collectorCollectionRepository).deleteCollectionById(2L);
    }

    @Test
    void deleteCollection_shouldThrowCollectorCollectionNotFoundException_whenCollectionIsMissing() {
        Collector collector = collectorWithCollections(1L, collection(2L, null, "Team", null, null));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteCollection(1L, 2L))
                .isInstanceOf(CollectorCollectionNotFoundException.class)
                .hasMessage("Collector collection with id 2 was not found");

        verify(collectorCollectionRepository).findById(2L);
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

        verify(collectorCollectionRepository).findById(2L);
        verifyNoInteractions(collectorCollectionFigurineRepository);
    }

    @Test
    void deleteCollection_shouldThrowCollectorNotFoundException_whenCollectorIsMissing() {
        when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteCollection(1L, 2L)).isInstanceOf(CollectorNotFoundException.class)
                .hasMessage("Collector with id 1 was not found");

        verify(collectorRepository).findById(1L);
        verifyNoInteractions(collectorCollectionFigurineRepository, collectorCollectionRepository);
    }

    @Test
    void updateCollectionAsFavorite_shouldSetTargetCollectionAsFavorite_andUnsetOthers() {
        CollectorCollection current = collection(2L, null, "Current", "current.png", "current description");
        current.setFavorite(false);
        CollectorCollection sibling = collection(3L, null, "Other", null, null);
        sibling.setFavorite(true);
        Collector collector = collector(1L, current, sibling);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findById(2L)).thenReturn(Optional.of(current));
        when(collectorCollectionRepository.save(current)).thenReturn(current);

        service.updateCollectionAsFavorite(1L, 2L);

        assertThat(current.isFavorite()).isTrue();
        assertThat(sibling.isFavorite()).isFalse();
        verify(collectorCollectionRepository).save(current);
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

        verify(collectorCollectionRepository).findById(2L);
        verifyNoInteractions(collectorCollectionFigurineRepository);
    }

    @Test
    void updateCollectionAsFavorite_shouldThrowCollectorNotFoundException_whenCollectorIsMissing() {
        when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCollectionAsFavorite(1L, 2L))
                .isInstanceOf(CollectorNotFoundException.class).hasMessage("Collector with id 1 was not found");

        verify(collectorRepository).findById(1L);
        verifyNoInteractions(collectorCollectionRepository, collectorCollectionFigurineRepository);
    }

    @Test
    void updateCollection_shouldReturnUpdatedCollection_whenNameIsUnique() {
        CollectorCollection current = collection(2L, null, "Old", "old.png", "old description");
        CollectorCollection sibling = collection(3L, null, "Other", null, null);
        Collector collector = collector(1L, current, sibling);
        CollectorCollectionReq request = new CollectorCollectionReq("New", "new.png", "new description");
        CollectorCollection saved = collection(2L, collector, "New", "new.png", "new description");

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.save(current)).thenReturn(saved);

        CollectorCollectionResp response = service.updateCollection(1L, 2L, request);

        assertThat(response)
                .isEqualTo(new CollectorCollectionResp(2L, "New", "new.png", "new description", false, 0, List.of()));
        assertThat(current.getName()).isEqualTo("New");
        assertThat(current.getImageUrl()).isEqualTo("new.png");
        assertThat(current.getDescription()).isEqualTo("new description");
        verify(collectorCollectionRepository).save(current);
    }

    @Test
    void updateCollection_shouldAllowCurrentCollectionName_whenCheckingForDuplicateNames() {
        CollectorCollection current = collection(2L, null, "Old", "old.png", "old description");
        Collector collector = collector(1L, current);
        CollectorCollectionReq request = new CollectorCollectionReq("Old", "new.png", "new description");
        CollectorCollection saved = collection(2L, collector, "Old", "new.png", "new description");

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.save(current)).thenReturn(saved);

        CollectorCollectionResp response = service.updateCollection(1L, 2L, request);

        assertThat(response)
                .isEqualTo(new CollectorCollectionResp(2L, "Old", "new.png", "new description", false, 0, List.of()));
        assertThat(current.getImageUrl()).isEqualTo("new.png");
        assertThat(current.getDescription()).isEqualTo("new description");
        verify(collectorCollectionRepository).save(current);
    }

    @Test
    void updateCollection_shouldThrowCollectorCollectionAlreadyExistsException_whenAnotherCollectionUsesRequestedName() {
        CollectorCollection current = collection(2L, null, "Old", "old.png", "old description");
        CollectorCollection sibling = collection(3L, null, "New", null, null);
        Collector collector = collector(1L, current, sibling);

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));

        assertThatThrownBy(
                () -> service.updateCollection(1L, 2L, new CollectorCollectionReq("New", "new.png", "new description")))
                .isInstanceOf(CollectorCollectionAlreadyExistsException.class)
                .hasMessage("Collector collection with name 'New' already exists");

        verifyNoInteractions(collectorCollectionRepository);
    }

    @Test
    void updateCollection_shouldThrowCollectorCollectionNotFoundException_whenCollectorDoesNotOwnCollection() {
        Collector collector = collector(1L, collection(3L, null, "Other", null, null));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));

        assertThatThrownBy(
                () -> service.updateCollection(1L, 2L, new CollectorCollectionReq("New", "new.png", "new description")))
                .isInstanceOf(CollectorCollectionNotFoundException.class)
                .hasMessage("Collector collection with id 2 was not found");

        verifyNoInteractions(collectorCollectionRepository);
    }

    @Test
    void updateCollection_shouldThrowCollectorNotFoundException_whenCollectorIsMissing() {
        when(collectorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.updateCollection(1L, 2L, new CollectorCollectionReq("New", "new.png", "new description")))
                .isInstanceOf(CollectorNotFoundException.class).hasMessage("Collector with id 1 was not found");
    }

    @Test
    void duplicateCollection_shouldReturnNewCollectionId_andCopyFigurines_whenSourceCollectionHasDescriptionAndImage() {
        CollectorCollection source = collection(2L, null, "Seiya", "image.png", "original description");
        Figurine figurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));
        CollectorCollectionFigurine assignment = collectionFigurine(source, figurine, 2, Condition.OPENED);
        source.setFigurines(new ArrayList<>(List.of(assignment)));
        Collector collector = collector(1L);
        List<CollectorCollection> collections = new ArrayList<>(List.of(source));
        CollectorCollection duplicate = collection(99L, collector, "Seiya copy", "image.png",
                "original description copy");
        duplicate.setFigurines(new ArrayList<>(List.of(collectionFigurine(duplicate, figurine, 2, Condition.OPENED))));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findByCollector(collector)).thenReturn(collections);
        when(collectorCollectionRepository.findByCollectorAndName(collector, "Seiya copy"))
                .thenReturn(Optional.empty());
        when(collectorCollectionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(collectorCollectionRepository.saveAllAndFlush(any())).thenAnswer(invocation -> {
            List<CollectorCollection> savedCollections = invocation.getArgument(0);
            savedCollections.get(1).setId(99L);
            return savedCollections;
        });

        long newId = service.duplicateCollection(1L, 2L);

        ArgumentCaptor<List<CollectorCollection>> collectionsCaptor = ArgumentCaptor.forClass(List.class);
        verify(collectorCollectionRepository).saveAllAndFlush(collectionsCaptor.capture());
        List<CollectorCollection> savedCollections = collectionsCaptor.getValue();
        assertThat(savedCollections).hasSize(2);
        CollectorCollection duplicated = savedCollections.get(1);
        assertThat(duplicated.getName()).isEqualTo("Seiya copy");
        assertThat(duplicated.getImageUrl()).isEqualTo("image.png");
        assertThat(duplicated.getDescription()).isEqualTo("original description copy");
        assertThat(duplicated.getFigurines()).hasSize(1);
        assertThat(duplicated.getFigurines().get(0).getCollection()).isEqualTo(duplicated);
        assertThat(duplicated.getFigurines().get(0).getFigurine()).isEqualTo(figurine);
        assertThat(duplicated.getFigurines().get(0).getQuantity()).isEqualTo(2);
        assertThat(duplicated.getFigurines().get(0).getCondition()).isEqualTo(Condition.OPENED);
        assertThat(newId).isEqualTo(99L);
    }

    @Test
    void duplicateCollection_shouldReturnNewCollectionId_andPreserveNullMetadata_whenSourceCollectionMetadataIsMissing() {
        CollectorCollection source = collection(2L, null, "Seiya", null, null);
        Figurine figurine = figurine(9L, "seiya", ReleaseStatus.RELEASED, LocalDate.of(2024, 3, 1));
        CollectorCollectionFigurine assignment = collectionFigurine(source, figurine, 1, Condition.SEALED);
        source.setFigurines(new ArrayList<>(List.of(assignment)));
        Collector collector = collector(1L);
        List<CollectorCollection> collections = new ArrayList<>(List.of(source));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findByCollector(collector)).thenReturn(collections);
        when(collectorCollectionRepository.findByCollectorAndName(collector, "Seiya copy"))
                .thenReturn(Optional.empty());
        when(collectorCollectionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(collectorCollectionRepository.saveAllAndFlush(any())).thenAnswer(invocation -> {
            List<CollectorCollection> savedCollections = invocation.getArgument(0);
            savedCollections.get(1).setId(42L);
            return savedCollections;
        });

        long newId = service.duplicateCollection(1L, 2L);

        assertThat(newId).isEqualTo(42L);
        ArgumentCaptor<List<CollectorCollection>> collectionsCaptor = ArgumentCaptor.forClass(List.class);
        verify(collectorCollectionRepository).saveAllAndFlush(collectionsCaptor.capture());
        List<CollectorCollection> savedCollections = collectionsCaptor.getValue();
        assertThat(savedCollections).hasSize(2);
        CollectorCollection duplicated = savedCollections.get(1);
        assertThat(duplicated.getName()).isEqualTo("Seiya copy");
        assertThat(duplicated.getImageUrl()).isNull();
        assertThat(duplicated.getDescription()).isNull();
        assertThat(duplicated.getFigurines()).hasSize(1);
        assertThat(duplicated.getFigurines().get(0).getCollection()).isEqualTo(duplicated);
        assertThat(duplicated.getFigurines().get(0).getFigurine()).isEqualTo(figurine);
        assertThat(duplicated.getFigurines().get(0).getQuantity()).isEqualTo(1);
        assertThat(duplicated.getFigurines().get(0).getCondition()).isEqualTo(Condition.SEALED);
    }

    @Test
    void duplicateCollection_shouldThrowCollectorCollectionNotFoundException_whenSavedCollectionsDoNotContainDuplicate() {
        CollectorCollection source = collection(2L, null, "Seiya", null, null);
        Collector collector = collector(1L);
        List<CollectorCollection> collections = new ArrayList<>(List.of(source));

        when(collectorRepository.findById(1L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findByCollector(collector)).thenReturn(collections);
        when(collectorCollectionRepository.findByCollectorAndName(collector, "Seiya copy"))
                .thenReturn(Optional.empty());
        when(collectorCollectionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(collectorCollectionRepository.saveAllAndFlush(any())).thenReturn(List.of(source));

        assertThatThrownBy(() -> service.duplicateCollection(1L, 2L))
                .isInstanceOf(CollectorCollectionNotFoundException.class)
                .hasMessage("Collector collection with id 0 was not found");
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

    private Collector collector(long id, CollectorCollection... collections) {
        Collector collector = new Collector();
        collector.setId(id);
        collector.setCollections(new ArrayList<>(List.of(collections)));
        return collector;
    }

    private Collector collectorWithCollections(long id, CollectorCollection... collections) {
        return collector(id, collections);
    }

    private CollectorCollection collection(long id, Collector owner, String name, String imageUrl, String description) {
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
            int quantity, Condition condition) {
        CollectorCollectionFigurine collectorCollectionFigurine = new CollectorCollectionFigurine();
        collectorCollectionFigurine.setCollection(collection);
        collectorCollectionFigurine.setFigurine(figurine);
        collectorCollectionFigurine.setQuantity(quantity);
        collectorCollectionFigurine.setCondition(condition);
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
