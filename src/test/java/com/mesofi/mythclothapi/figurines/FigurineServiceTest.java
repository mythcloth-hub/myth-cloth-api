package com.mesofi.mythclothapi.figurines;

import static com.mesofi.mythclothapi.catalogs.model.LineUpType.MYTH_CLOTH_EX;
import static com.mesofi.mythclothapi.distributors.model.CountryCode.JP;
import static com.mesofi.mythclothapi.distributors.model.CountryCode.MX;
import static com.mesofi.mythclothapi.figurineevents.model.FigurineEventType.ANNOUNCEMENT;
import static com.mesofi.mythclothapi.figurineevents.model.FigurineEventType.PREORDER_OPEN;
import static com.mesofi.mythclothapi.figurineevents.model.FigurineEventType.RELEASE;
import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.ANNOUNCED;
import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.RELEASED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.mesofi.mythclothapi.anniversaries.model.Anniversary;
import com.mesofi.mythclothapi.catalogs.CatalogService;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.catalogs.model.CatalogContext;
import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.catalogs.repository.LineUpRepository;
import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.collectors.CollectorRepository;
import com.mesofi.mythclothapi.collectors.exceptions.CollectorNotFoundException;
import com.mesofi.mythclothapi.collectorscollections.CollectorCollection;
import com.mesofi.mythclothapi.collectorscollections.CollectorCollectionFigurineService;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionRepository;
import com.mesofi.mythclothapi.config.MethodValidationTestConfig;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEvent;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEventType;
import com.mesofi.mythclothapi.figurines.dto.DistributorReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineRecommendationResp;
import com.mesofi.mythclothapi.figurines.dto.FigurineReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.figurines.dto.FigurineRestockResp;
import com.mesofi.mythclothapi.figurines.dto.FigurineSummaryResp;
import com.mesofi.mythclothapi.figurines.mapper.FigurineMapper;
import com.mesofi.mythclothapi.figurines.model.CachedFigurine;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;
import com.mesofi.mythclothapi.figurines.repository.CollectablePageImpl;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;

@ActiveProfiles("test")
@SpringBootTest(classes = {FigurineService.class, MethodValidationTestConfig.class})
public class FigurineServiceTest {

    @Autowired
    private FigurineService figurineService;

    @MockitoBean
    private FigurineMapper figurineMapper;
    @MockitoBean
    private LineUpRepository lineUpRepository;
    @MockitoBean
    private FigurineRepository figurineRepository;
    @MockitoBean
    private CurrencyRegionResolver currencyRegionResolver;
    @MockitoBean
    private CollectorRepository collectorRepository;
    @MockitoBean
    private CollectorCollectionRepository collectorCollectionRepository;
    @MockitoBean
    private CollectorCollectionFigurineService collectorCollectionFigurineService;
    @MockitoBean
    private CacheManager cacheManager;
    @MockitoBean
    private Cache figurineCache;
    @MockitoBean
    private CatalogService catalogService;

    @Test
    void createFigurine_shouldCreateDefaultEventsAndLinkPreviousRelease_whenFigurineIsReleased() {
        Figurine incoming = figurine(10L, "seiya", "Seiya", RELEASED);
        incoming.setDistributors(new ArrayList<>(List.of(distributor(CurrencyCode.JPY, 1000.0, LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1), true))));

        Figurine previousRelease = figurine(9L, "seiya", "Seiya", RELEASED);
        previousRelease.setDistributors(new ArrayList<>(List.of(distributor(CurrencyCode.JPY, 900.0,
                LocalDate.of(2023, 1, 1), LocalDate.of(2023, 2, 1), LocalDate.of(2023, 3, 1), true))));

        FigurineReq request = figurineRequest();
        when(catalogService.retrieveCatalogContext()).thenReturn(emptyCatalogContext());
        when(figurineMapper.toFigurine(eq(request), any())).thenReturn(incoming);
        when(figurineRepository.saveAndFlush(any(Figurine.class))).thenAnswer(invocation -> {
            Figurine saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });
        when(figurineRepository.findReleasedOrAnnouncedOrderByFirstReleaseDateDesc())
                .thenReturn(List.of(incoming, previousRelease));
        when(figurineMapper.toFigurineResp(any(Figurine.class), any(), any())).thenAnswer(invocation -> {
            Figurine figurine = invocation.getArgument(0);
            Function<Figurine, List<FigurineRestockResp>> restockFn = invocation.getArgument(2);
            return figurineResponse(figurine, restockFn.apply(figurine));
        });
        when(currencyRegionResolver.resolveCountry(CurrencyCode.JPY)).thenReturn(JP);

        FigurineResp response = figurineService.createFigurine(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.restocks()).containsExactly(new FigurineRestockResp(9L, LocalDate.of(2023, 3, 1)));

        verify(catalogService).retrieveCatalogContext();
        verify(figurineRepository).saveAndFlush(any(Figurine.class));
        verify(figurineRepository).findReleasedOrAnnouncedOrderByFirstReleaseDateDesc();
        verify(currencyRegionResolver, times(3)).resolveCountry(CurrencyCode.JPY);
    }

    @Test
    void createFigurine_shouldSkipDefaultEvents_whenFigurineHasNoDistributors() {
        Figurine incoming = figurine(11L, "hyoga", "Hyoga", ReleaseStatus.RUMORED);
        incoming.setDistributors(null);

        FigurineReq request = figurineRequest();
        when(catalogService.retrieveCatalogContext()).thenReturn(emptyCatalogContext());
        when(figurineMapper.toFigurine(eq(request), any())).thenReturn(incoming);
        when(figurineRepository.saveAndFlush(any(Figurine.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(figurineMapper.toFigurineResp(any(Figurine.class), any(), any()))
                .thenAnswer(invocation -> figurineResponse(invocation.getArgument(0), List.of()));

        FigurineResp response = figurineService.createFigurine(request);

        assertThat(response.id()).isEqualTo(11L);
        assertThat(incoming.getEvents()).isEmpty();
        verify(figurineRepository, never()).findReleasedOrAnnouncedOrderByFirstReleaseDateDesc();
    }

    @Test
    void createFigurine_shouldSkipDefaultEvents_whenFigurineHasAnEmptyDistributorList() {
        Figurine incoming = figurine(12L, "shun", "Shun", ReleaseStatus.RUMORED);
        incoming.setDistributors(new ArrayList<>());

        FigurineReq request = figurineRequest();
        when(catalogService.retrieveCatalogContext()).thenReturn(emptyCatalogContext());
        when(figurineMapper.toFigurine(eq(request), any())).thenReturn(incoming);
        when(figurineRepository.saveAndFlush(any(Figurine.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(figurineMapper.toFigurineResp(any(Figurine.class), any(), any()))
                .thenAnswer(invocation -> figurineResponse(invocation.getArgument(0), List.of()));

        FigurineResp response = figurineService.createFigurine(request);

        assertThat(response.id()).isEqualTo(12L);
        assertThat(incoming.getEvents()).isEmpty();
        verify(figurineRepository, never()).findReleasedOrAnnouncedOrderByFirstReleaseDateDesc();
    }

    @Test
    void initializeFigurineForCreate_shouldLinkBidirectionalRelationshipsAndPopulateDefaultEvents() {
        Figurine incoming = figurine(13L, "seiya", "Seiya", RELEASED);
        incoming.setDistributors(new ArrayList<>(List.of(distributor(CurrencyCode.JPY, 1000.0, LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1), true))));
        incoming.setEvents(new ArrayList<>());

        when(currencyRegionResolver.resolveCountry(CurrencyCode.JPY)).thenReturn(JP);

        Figurine initialized = figurineService.initializeFigurineForCreate(incoming);

        assertThat(initialized).isSameAs(incoming);
        assertThat(initialized.getDistributors())
                .allSatisfy(distributor -> assertThat(distributor.getFigurine()).isSameAs(incoming));
        assertThat(initialized.getEvents()).hasSize(3);
        assertThat(initialized.getEvents()).extracting(FigurineEvent::getType).containsExactly(ANNOUNCEMENT,
                PREORDER_OPEN, RELEASE);
        assertThat(initialized.getEvents()).allSatisfy(event -> assertThat(event.getFigurine()).isSameAs(incoming));
        assertThat(initialized.getEvents()).extracting(FigurineEvent::getRegion).containsOnly(JP);
    }

    @Test
    void readFigurine_shouldThrowException_whenFigurineDoesNotExist() {
        when(figurineRepository.findById(44L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> figurineService.readFigurine(44L)).isInstanceOf(FigurineNotFoundException.class)
                .extracting(ex -> ((FigurineNotFoundException) ex).getId()).isEqualTo(44L);
    }

    @Test
    void readFigurine_shouldReturnResponseWithRestockHistory_whenFigurineExists() {
        Figurine previousRelease = figurine(5L, "seiya", "Seiya", RELEASED);
        previousRelease.setDistributors(new ArrayList<>(List.of(distributor(CurrencyCode.JPY, 900.0,
                LocalDate.of(2023, 1, 1), LocalDate.of(2023, 2, 1), LocalDate.of(2023, 3, 1), true))));

        Figurine figurine = figurine(6L, "seiya", "Seiya", RELEASED);
        figurine.setDistributors(new ArrayList<>(List.of(distributor(CurrencyCode.JPY, 1000.0, LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1), true))));
        figurine.setPreviousRelease(previousRelease);

        when(figurineRepository.findById(6L)).thenReturn(Optional.of(figurine));
        when(figurineMapper.toFigurineResp(any(Figurine.class), any(), any())).thenAnswer(invocation -> {
            Figurine current = invocation.getArgument(0);
            Function<Figurine, List<FigurineRestockResp>> restockFn = invocation.getArgument(2);
            return figurineResponse(current, restockFn.apply(current));
        });
        when(currencyRegionResolver.resolveCountry(CurrencyCode.JPY)).thenReturn(JP);

        FigurineResp response = figurineService.readFigurine(6L);

        assertThat(response.restocks()).containsExactly(new FigurineRestockResp(5L, LocalDate.of(2023, 3, 1)));
    }

    @Test
    void readFigurine_shouldReturnResponseWithNullRestockHistory_whenFigurineHasNoPreviousRelease() {
        Figurine figurine = figurine(7L, "seiya", "Seiya", RELEASED);
        figurine.setDistributors(new ArrayList<>(List.of(distributor(CurrencyCode.JPY, 1000.0, LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1), true))));

        when(figurineRepository.findById(7L)).thenReturn(Optional.of(figurine));
        when(figurineMapper.toFigurineResp(any(Figurine.class), any(), any())).thenAnswer(invocation -> {
            Figurine current = invocation.getArgument(0);
            Function<Figurine, List<FigurineRestockResp>> restockFn = invocation.getArgument(2);
            return figurineResponse(current, restockFn.apply(current));
        });

        FigurineResp response = figurineService.readFigurine(7L);

        assertThat(response.restocks()).isNull();
    }

    @Test
    void filterFigurines_shouldReturnPageOfMappedResponses_whenFigurinesAreFound() {
        Figurine first = figurine(1L, "seiya", "Seiya", RELEASED);
        Figurine second = figurine(2L, "hyoga", "Hyoga", ANNOUNCED);
        CollectablePageImpl<Figurine> page = new CollectablePageImpl<>(List.of(first, second), PageRequest.of(0, 2), 2,
                2);

        when(figurineRepository.findPaginated(any(), any())).thenReturn(page);
        when(figurineMapper.toFigurineResp(any(Figurine.class), any(), any()))
                .thenAnswer(invocation -> figurineResponse(invocation.getArgument(0), List.of()));

        CollectablePageImpl<FigurineResp> response = figurineService.filterFigurines(emptyFilter(), 0, 2);

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent()).extracting(FigurineResp::id).containsExactly(1L, 2L);
        assertThat(response.getTotalCollectables()).isEqualTo(2);
        verify(figurineRepository).findPaginated(any(), any());
    }

    @Test
    void retrieveCollectedFigurineIds_shouldPreferTheMatchingCollectionEvenWhenOthersAreListedFirst() {
        Collector collector = new Collector();
        collector.setId(10L);

        CollectorCollection wrongCollection = new CollectorCollection();
        wrongCollection.setId(19L);
        wrongCollection.setFigurines(new ArrayList<>(List.of(collectionFigurine(99L))));

        CollectorCollection matchingCollection = new CollectorCollection();
        matchingCollection.setId(20L);
        matchingCollection.setFigurines(new ArrayList<>(List.of(collectionFigurine(1L), collectionFigurine(2L))));

        when(collectorRepository.findById(10L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findByCollector(collector))
                .thenReturn(List.of(wrongCollection, matchingCollection));

        assertThat(figurineService.retrieveCollectedFigurineIds(10L, 20L)).containsExactly(1L, 2L);
    }

    @Test
    void retrieveCollectedFigurineIds_shouldReturnEmptyList_whenCollectionIdIsNull() {
        assertThat(figurineService.retrieveCollectedFigurineIds(10L, null)).isEmpty();
    }

    @Test
    void retrieveCollectedFigurineIds_shouldThrowException_whenCollectorDoesNotExist() {
        when(collectorRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> figurineService.retrieveCollectedFigurineIds(10L, 20L))
                .isInstanceOf(CollectorNotFoundException.class)
                .extracting(ex -> ((CollectorNotFoundException) ex).getId()).isEqualTo(10L);
    }

    @Test
    void retrieveCollectedFigurineIds_shouldReturnIds_whenCollectionExists() {
        Collector collector = new Collector();
        collector.setId(10L);

        CollectorCollection collection = new CollectorCollection();
        collection.setId(20L);
        collection.setFigurines(new ArrayList<>(List.of(collectionFigurine(1L), collectionFigurine(2L))));

        when(collectorRepository.findById(10L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findByCollector(collector)).thenReturn(List.of(collection));

        assertThat(figurineService.retrieveCollectedFigurineIds(10L, 20L)).containsExactly(1L, 2L);
    }

    @Test
    void retrieveCollectedFigurineIds_shouldReturnEmptyList_whenCollectionIsMissing() {
        Collector collector = new Collector();
        collector.setId(10L);

        CollectorCollection collection = new CollectorCollection();
        collection.setId(20L);

        when(collectorRepository.findById(10L)).thenReturn(Optional.of(collector));
        when(collectorCollectionRepository.findByCollector(collector)).thenReturn(List.of(collection));

        assertThat(figurineService.retrieveCollectedFigurineIds(10L, 21L)).isEmpty();
    }

    @Test
    void retrieveFigurineSummaries_shouldReturnOnlyReleasedOrAnnouncedFigurines() {
        Figurine released = figurine(1L, "seiya", "Seiya", RELEASED);
        Figurine announced = figurine(2L, "hyoga", "Hyoga", ANNOUNCED);
        Figurine hidden = figurine(3L, "shun", "Shun", ReleaseStatus.UNRELEASED);

        when(figurineRepository.findAll(any(com.mesofi.mythclothapi.figurines.FigurineFilter.class)))
                .thenReturn(List.of(released, announced, hidden));
        when(figurineMapper.toFigurineSummaryResp(any(Figurine.class)))
                .thenAnswer(invocation -> summaryResponse(invocation.getArgument(0)));

        List<FigurineSummaryResp> response = figurineService.retrieveFigurineSummaries(emptyFilter());

        assertThat(response).extracting(FigurineSummaryResp::id).containsExactly(1L, 2L);
    }

    @Test
    void retrieveSelectableFigurines_shouldReturnOnlyReleasedOrAnnouncedIds() {
        Figurine released = figurine(1L, "seiya", "Seiya", RELEASED);
        Figurine announced = figurine(2L, "hyoga", "Hyoga", ANNOUNCED);
        Figurine hidden = figurine(3L, "shun", "Shun", ReleaseStatus.RUMORED);

        when(figurineRepository.findAll(any(com.mesofi.mythclothapi.figurines.FigurineFilter.class)))
                .thenReturn(List.of(released, announced, hidden));

        assertThat(figurineService.retrieveSelectableFigurines(emptyFilter())).containsExactly(1L, 2L);
    }

    @Test
    void retrieveRecommendedFigurines_shouldReturnDefaultRecommendations_whenCollectorIsAnonymous() {
        Figurine first = figurine(101L, "seiya", "Seiya", RELEASED);
        Figurine second = figurine(102L, "hyoga", "Hyoga", ANNOUNCED);
        CollectablePageImpl<Figurine> page = new CollectablePageImpl<>(List.of(first, second), PageRequest.of(0, 2), 2,
                2);

        when(figurineRepository.findPaginated(any(), eq(PageRequest.of(0, 2)))).thenReturn(page);
        when(figurineMapper.toFigurineRecommendationResp(any(Figurine.class)))
                .thenAnswer(invocation -> recommendationResponse(invocation.getArgument(0)));

        List<FigurineRecommendationResp> response = figurineService.retrieveRecommendedFigurines(null, 2);

        assertThat(response).extracting(FigurineRecommendationResp::id).containsExactly(101L, 102L);
        verify(collectorCollectionFigurineService, never()).findLatestFavoriteCollectionFigurines(anyLong(), anyInt());
    }

    @Test
    void retrieveRecommendedFigurines_shouldReturnDefaultRecommendations_whenCollectorHasNoFavorites() {
        Figurine first = figurine(103L, "shun", "Shun", RELEASED);
        CollectablePageImpl<Figurine> page = new CollectablePageImpl<>(List.of(first), PageRequest.of(0, 1), 1, 1);

        when(collectorCollectionFigurineService.findLatestFavoriteCollectionFigurines(7L, 5)).thenReturn(List.of());
        when(figurineRepository.findPaginated(any(), eq(PageRequest.of(0, 1)))).thenReturn(page);
        when(figurineMapper.toFigurineRecommendationResp(any(Figurine.class)))
                .thenAnswer(invocation -> recommendationResponse(invocation.getArgument(0)));

        List<FigurineRecommendationResp> response = figurineService.retrieveRecommendedFigurines(7L, 1);

        assertThat(response).extracting(FigurineRecommendationResp::id).containsExactly(103L);
        verify(collectorCollectionFigurineService).findLatestFavoriteCollectionFigurines(7L, 5);
    }

    @Test
    void retrieveRecommendedFigurines_shouldExcludeCollectedFigurines_andLimitPersonalizedRecommendations() {
        Figurine collectedFirst = figurine(201L, "seiya", "Seiya", RELEASED);
        collectedFirst.setGroup(group("Bronze Saints"));
        Figurine collectedSecond = figurine(202L, "hyoga", "Hyoga", ANNOUNCED);
        collectedSecond.setGroup(group("Gold Saints"));
        Figurine recommendedFirst = figurine(203L, "shiryu", "Shiryu", RELEASED);
        recommendedFirst.setGroup(group("Bronze Saints"));
        Figurine duplicateCollected = figurine(202L, "hyoga", "Hyoga", ANNOUNCED);
        duplicateCollected.setGroup(group("Gold Saints"));
        Figurine recommendedSecond = figurine(204L, "ikki", "Ikki", ANNOUNCED);
        recommendedSecond.setGroup(group("Gold Saints"));

        CollectablePageImpl<Figurine> page = new CollectablePageImpl<>(
                List.of(collectedFirst, recommendedFirst, duplicateCollected, recommendedSecond), PageRequest.of(0, 4),
                4, 4);

        when(collectorCollectionFigurineService.findLatestFavoriteCollectionFigurines(9L, 5))
                .thenReturn(List.of(collectionFigurine(collectedFirst), collectionFigurine(collectedSecond)));
        when(figurineRepository.findPaginated(any(), eq(PageRequest.of(0, 4)))).thenReturn(page);
        when(figurineMapper.toFigurineRecommendationResp(any(Figurine.class)))
                .thenAnswer(invocation -> recommendationResponse(invocation.getArgument(0)));

        List<FigurineRecommendationResp> response = figurineService.retrieveRecommendedFigurines(9L, 2);

        assertThat(response).extracting(FigurineRecommendationResp::id).containsExactly(203L, 204L);
        verify(collectorCollectionFigurineService).findLatestFavoriteCollectionFigurines(9L, 5);
    }

    @Test
    void retrieveRecommendedFigurines_shouldUsePersonalizedBranch_whenCollectorIdIsZero() {
        Figurine collected = figurine(205L, "seiya", "Seiya", RELEASED);
        collected.setGroup(group("Bronze Saints"));
        Figurine recommendation = figurine(206L, "shiryu", "Shiryu", ANNOUNCED);
        recommendation.setGroup(group("Bronze Saints"));
        CollectablePageImpl<Figurine> page = new CollectablePageImpl<>(List.of(collected, recommendation),
                PageRequest.of(0, 2), 2, 2);

        when(collectorCollectionFigurineService.findLatestFavoriteCollectionFigurines(0L, 5))
                .thenReturn(List.of(collectionFigurine(collected)));
        when(figurineRepository.findPaginated(any(), eq(PageRequest.of(0, 2)))).thenReturn(page);
        when(figurineMapper.toFigurineRecommendationResp(any(Figurine.class)))
                .thenAnswer(invocation -> recommendationResponse(invocation.getArgument(0)));

        List<FigurineRecommendationResp> response = figurineService.retrieveRecommendedFigurines(0L, 1);

        assertThat(response).extracting(FigurineRecommendationResp::id).containsExactly(206L);
    }

    @Test
    void initializeFigurineForUpdate_shouldReturnTheExistingInstance() {
        Figurine existing = figurine(12L, "seiya", "Seiya", RELEASED);
        existing.setDistributors(
                new ArrayList<>(List.of(distributor(CurrencyCode.JPY, 1000.0, null, null, null, true))));
        existing.setEvents(new ArrayList<>());

        Figurine incoming = figurine(12L, "seiya", "Seiya", RELEASED);
        incoming.setDistributors(null);

        Figurine response = figurineService.initializeFigurineForUpdate(existing, incoming);

        assertThat(response).isSameAs(existing);
    }

    @Test
    void initializeFigurineForCreate_shouldPopulateDefaultEventDetailsDatesAndConfirmation() {
        Figurine incoming = figurine(14L, "shiryu", "Shiryu", RELEASED);
        incoming.setDistributors(new ArrayList<>(List.of(distributor(CurrencyCode.JPY, 1300.0,
                LocalDate.of(2024, 1, 10), LocalDate.of(2024, 2, 10), LocalDate.of(2024, 3, 10), false))));
        incoming.setEvents(new ArrayList<>());

        when(currencyRegionResolver.resolveCountry(CurrencyCode.JPY)).thenReturn(JP);

        figurineService.initializeFigurineForCreate(incoming);

        FigurineEvent announcement = incoming.getEvents().stream().filter(event -> event.getType() == ANNOUNCEMENT)
                .findFirst().orElseThrow();
        FigurineEvent preorder = incoming.getEvents().stream().filter(event -> event.getType() == PREORDER_OPEN)
                .findFirst().orElseThrow();
        FigurineEvent release = incoming.getEvents().stream().filter(event -> event.getType() == RELEASE).findFirst()
                .orElseThrow();

        assertThat(announcement.getDetails()).isNotBlank();
        assertThat(announcement.getEventDate()).isEqualTo(LocalDate.of(2024, 1, 10));
        assertThat(announcement.isEventDateConfirmed()).isTrue();
        assertThat(preorder.getDetails()).isNotBlank();
        assertThat(preorder.getEventDate()).isEqualTo(LocalDate.of(2024, 2, 10));
        assertThat(preorder.isEventDateConfirmed()).isTrue();
        assertThat(release.getDetails()).isNotBlank();
        assertThat(release.getEventDate()).isEqualTo(LocalDate.of(2024, 3, 10));
        assertThat(release.isEventDateConfirmed()).isFalse();
    }

    @Test
    void updateFigurine_shouldThrowException_whenFigurineDoesNotExist() {
        FigurineReq request = figurineRequest();
        when(figurineRepository.findById(44L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> figurineService.updateFigurine(44L, request))
                .isInstanceOf(FigurineNotFoundException.class)
                .extracting(ex -> ((FigurineNotFoundException) ex).getId()).isEqualTo(44L);
    }

    @Test
    void updateFigurine_shouldClearPreviousReleasesAndRebuildHistory_whenFigurineIsReleased() {
        Figurine existing = figurine(44L, "seiya", "Seiya", ANNOUNCED);
        existing.setDistributors(new ArrayList<>(List.of(distributor(CurrencyCode.JPY, 1000.0, LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1), true))));
        existing.setEvents(new ArrayList<>());

        Figurine incoming = figurine(44L, "seiya", "Seiya", RELEASED);
        incoming.setDistributors(new ArrayList<>(List.of(
                distributor(CurrencyCode.JPY, 1200.0, LocalDate.of(2024, 1, 2), LocalDate.of(2024, 2, 2),
                        LocalDate.of(2024, 3, 2), true),
                distributor(CurrencyCode.MXN, 1500.0, null, null, LocalDate.of(2024, 4, 1), false))));

        Figurine previousRelease = figurine(43L, "seiya", "Seiya", RELEASED);
        previousRelease.setDistributors(new ArrayList<>(List.of(distributor(CurrencyCode.JPY, 900.0,
                LocalDate.of(2023, 1, 1), LocalDate.of(2023, 2, 1), LocalDate.of(2023, 3, 1), true))));

        FigurineReq request = figurineRequest();
        when(figurineRepository.findById(44L)).thenReturn(Optional.of(existing));
        when(catalogService.retrieveCatalogContext()).thenReturn(emptyCatalogContext());
        when(figurineMapper.toFigurine(eq(request), any())).thenReturn(incoming);
        when(figurineRepository.saveAndFlush(any(Figurine.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(figurineRepository.clearPreviousReleases()).thenReturn(1);
        when(figurineRepository.findAll()).thenReturn(List.of(existing, previousRelease));
        when(figurineRepository.findReleasedOrAnnouncedOrderByFirstReleaseDateDesc())
                .thenReturn(List.of(existing, previousRelease));
        when(figurineMapper.toFigurineResp(any(Figurine.class), any(), any())).thenAnswer(invocation -> {
            Figurine current = invocation.getArgument(0);
            Function<Figurine, List<FigurineRestockResp>> restockFn = invocation.getArgument(2);
            return figurineResponse(current, restockFn.apply(current));
        });
        when(currencyRegionResolver.resolveCountry(CurrencyCode.JPY)).thenReturn(JP);
        when(currencyRegionResolver.resolveCountry(CurrencyCode.MXN)).thenReturn(MX);

        FigurineResp response = figurineService.updateFigurine(44L, request);

        assertThat(response.id()).isEqualTo(44L);
        verify(figurineRepository).clearPreviousReleases();
        verify(figurineRepository).findAll();
        verify(figurineRepository).findReleasedOrAnnouncedOrderByFirstReleaseDateDesc();
    }

    @Test
    void updateFigurine_shouldSkipRestockRebuild_whenFigurineIsNotReleasedOrAnnounced() {
        Figurine existing = figurine(45L, "seiya", "Seiya", ReleaseStatus.RUMORED);
        existing.setDistributors(new ArrayList<>(List.of(distributor(CurrencyCode.JPY, 1000.0, LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1), true))));
        existing.setEvents(new ArrayList<>());

        Figurine incoming = figurine(45L, "seiya", "Seiya", ReleaseStatus.RUMORED);
        incoming.setDistributors(new ArrayList<>(List.of(distributor(CurrencyCode.JPY, 1200.0, LocalDate.of(2024, 1, 2),
                LocalDate.of(2024, 2, 2), LocalDate.of(2024, 3, 2), true))));

        FigurineReq request = figurineRequest();
        when(figurineRepository.findById(45L)).thenReturn(Optional.of(existing));
        when(catalogService.retrieveCatalogContext()).thenReturn(emptyCatalogContext());
        when(figurineMapper.toFigurine(eq(request), any())).thenReturn(incoming);
        when(figurineRepository.saveAndFlush(any(Figurine.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(figurineMapper.toFigurineResp(any(Figurine.class), any(), any()))
                .thenAnswer(invocation -> figurineResponse(invocation.getArgument(0), List.of()));

        figurineService.updateFigurine(45L, request);

        verify(figurineRepository, never()).clearPreviousReleases();
        verify(figurineRepository, never()).findReleasedOrAnnouncedOrderByFirstReleaseDateDesc();
    }

    @Test
    void initializeFigurineForUpdate_shouldUpdateExistingEventsAndDistributors_whenIncomingDataChanges() {
        Figurine existing = figurine(7L, "seiya", "Seiya", RELEASED);
        FigurineDistributor existingDistributor = distributor(CurrencyCode.JPY, 1000.0, LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1), true);
        existing.setDistributors(new ArrayList<>(List.of(existingDistributor)));
        existing.setEvents(new ArrayList<>(List.of(event(existing, ANNOUNCEMENT, LocalDate.of(2024, 1, 1)),
                event(existing, PREORDER_OPEN, LocalDate.of(2024, 2, 1)),
                event(existing, RELEASE, LocalDate.of(2024, 3, 1)))));

        Figurine incoming = figurine(7L, "seiya-updated", "Seiya Updated", RELEASED);
        incoming.setDistributors(new ArrayList<>(List.of(
                distributor(CurrencyCode.JPY, 1200.0, LocalDate.of(2024, 1, 2), LocalDate.of(2024, 2, 2),
                        LocalDate.of(2024, 3, 2), false),
                distributor(CurrencyCode.MXN, 1400.0, null, null, LocalDate.of(2024, 4, 4), true))));

        doAnswer(invocation -> {
            Figurine target = invocation.getArgument(0);
            Figurine source = invocation.getArgument(1);
            target.setNormalizedName(source.getNormalizedName());
            target.setDisplayName(source.getDisplayName());
            target.setCurrentReleaseStatus(source.getCurrentReleaseStatus());
            return null;
        }).when(figurineMapper).updateFigurine(any(Figurine.class), any(Figurine.class));
        doAnswer(invocation -> {
            FigurineDistributor target = invocation.getArgument(0);
            FigurineDistributor source = invocation.getArgument(1);
            target.setPrice(source.getPrice());
            target.setAnnouncementDate(source.getAnnouncementDate());
            target.setPreorderDate(source.getPreorderDate());
            target.setReleaseDate(source.getReleaseDate());
            target.setReleaseDateConfirmed(source.isReleaseDateConfirmed());
            return null;
        }).when(figurineMapper).updateFigurineDistributor(any(FigurineDistributor.class),
                any(FigurineDistributor.class));

        figurineService.initializeFigurineForUpdate(existing, incoming);

        assertThat(existing.getDisplayName()).isEqualTo("Seiya Updated");
        assertThat(existing.getDistributors()).hasSize(2);
        assertThat(existing.getDistributors().get(1).getFigurine()).isSameAs(existing);
        assertThat(existing.getEvents()).extracting(FigurineEvent::getEventDate)
                .containsExactly(LocalDate.of(2024, 1, 2), LocalDate.of(2024, 2, 2), LocalDate.of(2024, 3, 2));
        assertThat(existing.getEvents()).allSatisfy(event -> assertThat(event.getFigurine()).isSameAs(existing));
    }

    @Test
    void initializeFigurineForUpdate_shouldAddDefaultEvents_whenExistingEventsAreMissing() {
        Figurine existing = figurine(8L, "seiya", "Seiya", RELEASED);
        existing.setDistributors(new ArrayList<>(List.of(distributor(CurrencyCode.JPY, 1000.0, LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1), true))));
        existing.setEvents(new ArrayList<>());

        Figurine incoming = figurine(8L, "seiya", "Seiya", RELEASED);
        incoming.setDistributors(new ArrayList<>(List.of(distributor(CurrencyCode.JPY, 1200.0, LocalDate.of(2024, 4, 1),
                LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1), true))));

        when(currencyRegionResolver.resolveCountry(CurrencyCode.JPY)).thenReturn(JP);
        figurineService.initializeFigurineForUpdate(existing, incoming);

        assertThat(existing.getEvents()).hasSize(3);
        assertThat(existing.getEvents()).extracting(FigurineEvent::getType).containsExactly(ANNOUNCEMENT, PREORDER_OPEN,
                RELEASE);
        assertThat(existing.getEvents()).allSatisfy(event -> assertThat(event.getFigurine()).isSameAs(existing));
    }

    @Test
    void initializeFigurineForUpdate_shouldKeepExistingEvents_whenIncomingDatesAreMissing() {
        Figurine existing = figurine(9L, "seiya", "Seiya", RELEASED);
        existing.setDistributors(new ArrayList<>(List.of(distributor(CurrencyCode.JPY, 1000.0, LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1), true))));
        existing.setEvents(new ArrayList<>(List.of(event(existing, ANNOUNCEMENT, LocalDate.of(2024, 1, 1)),
                event(existing, PREORDER_OPEN, LocalDate.of(2024, 2, 1)),
                event(existing, RELEASE, LocalDate.of(2024, 3, 1)))));

        Figurine incoming = figurine(9L, "seiya", "Seiya", RELEASED);
        incoming.setDistributors(
                new ArrayList<>(List.of(distributor(CurrencyCode.JPY, 1200.0, null, null, null, true))));

        doAnswer(invocation -> {
            FigurineDistributor target = invocation.getArgument(0);
            FigurineDistributor source = invocation.getArgument(1);
            target.setAnnouncementDate(source.getAnnouncementDate());
            target.setPreorderDate(source.getPreorderDate());
            target.setReleaseDate(source.getReleaseDate());
            target.setReleaseDateConfirmed(source.isReleaseDateConfirmed());
            return null;
        }).when(figurineMapper).updateFigurineDistributor(any(FigurineDistributor.class),
                any(FigurineDistributor.class));

        figurineService.initializeFigurineForUpdate(existing, incoming);

        assertThat(existing.getEvents()).extracting(FigurineEvent::getEventDate)
                .containsExactly(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1));
    }

    @Test
    void initializeFigurineForUpdate_shouldRefreshReleaseEventConfirmationFlag_whenDistributorProvidesNewStatus() {
        Figurine existing = figurine(9L, "seiya", "Seiya", RELEASED);
        FigurineDistributor distributor = distributor(CurrencyCode.JPY, 1000.0, LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1), false);
        existing.setDistributors(new ArrayList<>(List.of(distributor)));
        existing.setEvents(new ArrayList<>(List.of(event(existing, ANNOUNCEMENT, LocalDate.of(2024, 1, 1)),
                event(existing, PREORDER_OPEN, LocalDate.of(2024, 2, 1)),
                event(existing, RELEASE, LocalDate.of(2024, 3, 1)))));

        Figurine incoming = figurine(9L, "seiya", "Seiya", RELEASED);
        incoming.setDistributors(new ArrayList<>(
                List.of(distributor(CurrencyCode.JPY, 1200.0, null, null, LocalDate.of(2024, 4, 1), true))));

        doAnswer(invocation -> {
            FigurineDistributor target = invocation.getArgument(0);
            FigurineDistributor source = invocation.getArgument(1);
            target.setReleaseDate(source.getReleaseDate());
            target.setReleaseDateConfirmed(source.isReleaseDateConfirmed());
            return null;
        }).when(figurineMapper).updateFigurineDistributor(any(FigurineDistributor.class),
                any(FigurineDistributor.class));

        figurineService.initializeFigurineForUpdate(existing, incoming);

        FigurineEvent releaseEvent = existing.getEvents().stream().filter(event -> event.getType() == RELEASE)
                .findFirst().orElseThrow();
        assertThat(releaseEvent.getEventDate()).isEqualTo(LocalDate.of(2024, 4, 1));
        assertThat(releaseEvent.isEventDateConfirmed()).isTrue();
    }

    @Test
    void initializeFigurineForUpdate_shouldAddAnnouncementEvent_whenAnnouncementEventIsMissing() {
        Figurine existing = figurine(11L, "seiya", "Seiya", RELEASED);
        existing.setDistributors(
                new ArrayList<>(List.of(distributor(CurrencyCode.JPY, 1000.0, null, null, null, true))));
        existing.setEvents(new ArrayList<>(List.of(event(existing, PREORDER_OPEN, LocalDate.of(2024, 2, 1)),
                event(existing, RELEASE, LocalDate.of(2024, 3, 1)))));

        Figurine incoming = figurine(11L, "seiya", "Seiya", RELEASED);
        incoming.setDistributors(new ArrayList<>(
                List.of(distributor(CurrencyCode.JPY, 1200.0, LocalDate.of(2024, 1, 1), null, null, true))));

        doAnswer(invocation -> {
            FigurineDistributor target = invocation.getArgument(0);
            FigurineDistributor source = invocation.getArgument(1);
            target.setAnnouncementDate(source.getAnnouncementDate());
            target.setPreorderDate(source.getPreorderDate());
            target.setReleaseDate(source.getReleaseDate());
            target.setReleaseDateConfirmed(source.isReleaseDateConfirmed());
            return null;
        }).when(figurineMapper).updateFigurineDistributor(any(FigurineDistributor.class),
                any(FigurineDistributor.class));

        figurineService.initializeFigurineForUpdate(existing, incoming);

        assertThat(existing.getEvents()).extracting(FigurineEvent::getType).containsExactly(PREORDER_OPEN, RELEASE,
                ANNOUNCEMENT);
    }

    @Test
    void initializeFigurineForUpdate_shouldIgnoreNullIncomingDistributors() {
        Figurine existing = figurine(10L, "seiya", "Seiya", RELEASED);
        existing.setDistributors(
                new ArrayList<>(List.of(distributor(CurrencyCode.JPY, 1000.0, null, null, null, true))));
        existing.setEvents(new ArrayList<>());

        Figurine incoming = figurine(10L, "seiya", "Seiya", RELEASED);
        incoming.setDistributors(null);

        figurineService.initializeFigurineForUpdate(existing, incoming);

        assertThat(existing.getDistributors()).hasSize(1);
        assertThat(existing.getEvents()).isEmpty();
    }

    @Test
    void rebuildRestockHistory_shouldLinkMatchingPreviousRelease() {
        Figurine current = figurine(20L, "seiya", "Seiya", RELEASED);
        current.setDistributors(new ArrayList<>(List.of(distributor(CurrencyCode.JPY, 1200.0, LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1), true))));

        Figurine previous = figurine(19L, "seiya", "Seiya", RELEASED);
        previous.setDistributors(new ArrayList<>(List.of(distributor(CurrencyCode.JPY, 900.0, LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 2, 1), LocalDate.of(2023, 3, 1), true))));

        Figurine hidden = figurine(18L, "vega", "Vega", ReleaseStatus.RUMORED);
        hidden.setDistributors(new ArrayList<>(List.of(distributor(CurrencyCode.JPY, 800.0, LocalDate.of(2022, 1, 1),
                LocalDate.of(2022, 2, 1), LocalDate.of(2022, 3, 1), true))));

        Figurine notRestock = figurine(18L, "seiya", "Seiya", RELEASED);
        notRestock.setAnniversary(new Anniversary());
        notRestock.setDistributors(new ArrayList<>(List.of(distributor(CurrencyCode.JPY, 800.0,
                LocalDate.of(2022, 1, 1), LocalDate.of(2022, 2, 1), LocalDate.of(2022, 3, 1), true))));

        when(figurineRepository.findReleasedOrAnnouncedOrderByFirstReleaseDateDesc())
                .thenReturn(List.of(notRestock, current, previous, hidden));

        figurineService.rebuildRestockHistory(List.of(notRestock, current, previous, hidden));

        assertThat(current.getPreviousRelease()).isSameAs(previous);
    }

    @Test
    void deleteFigurine_shouldThrowException_whenFigurineDoesNotExist() {
        when(figurineRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> figurineService.deleteFigurine(9L)).isInstanceOf(FigurineNotFoundException.class)
                .extracting(ex -> ((FigurineNotFoundException) ex).getId()).isEqualTo(9L);
    }

    @Test
    void deleteFigurine_shouldDeleteExistingFigurine() {
        Figurine figurine = figurine(9L, "seiya", "Seiya", RELEASED);
        when(figurineRepository.findById(9L)).thenReturn(Optional.of(figurine));

        figurineService.deleteFigurine(9L);

        verify(figurineRepository).delete(figurine);
    }

    @Test
    void findBestMatchingFigurine_shouldReturnEmpty_whenLineUpIsNotConfigured() {
        when(cacheManager.getCache(FigurineService.FIGURINE_CACHE)).thenReturn(figurineCache);
        assertThat(figurineService.findBestMatchingFigurine(
                com.mesofi.mythclothapi.catalogs.model.LineUpType.TAMASHII_NATIONS_BOX, "seiya")).isEmpty();
    }

    @Test
    void findBestMatchingFigurine_shouldCacheOnlyReleasedOrAnnouncedOptions() {
        LineUp lineUp = lineUp("Myth Cloth EX");
        Figurine announced = figurine(30L, "saya", "Saya", ANNOUNCED);
        Figurine released = figurine(31L, "seiya", "Seiya", RELEASED);
        Figurine hidden = figurine(32L, "vega", "Vega", ReleaseStatus.RUMORED);

        when(cacheManager.getCache(FigurineService.FIGURINE_CACHE)).thenReturn(figurineCache);
        when(figurineCache.get("by-mythcloth-ex", List.class)).thenReturn(null);
        when(lineUpRepository.findByDescription("Myth Cloth EX")).thenReturn(Optional.of(lineUp));
        when(figurineRepository.findAllByLineup(lineUp)).thenReturn(List.of(released, hidden, announced));
        when(figurineRepository.findById(31L)).thenReturn(Optional.of(released));

        assertThat(figurineService.findBestMatchingFigurine(MYTH_CLOTH_EX, "Seiya")).contains(released);
        verify(figurineCache).put(eq("by-mythcloth-ex"), argThat(value -> {
            List<CachedFigurine> cached = (List<CachedFigurine>) value;
            List<Long> cachedIds = cached.stream().map(CachedFigurine::id).toList();
            return cachedIds.containsAll(List.of(31L, 30L)) && cachedIds.size() == 2 && !cachedIds.contains(32L);
        }));
    }

    @Test
    void findBestMatchingFigurine_shouldReturnEmpty_whenLineUpCannotBeFound() {
        when(cacheManager.getCache(FigurineService.FIGURINE_CACHE)).thenReturn(figurineCache);
        when(figurineCache.get("by-mythcloth-ex", List.class)).thenReturn(null);
        when(lineUpRepository.findByDescription("Myth Cloth EX")).thenReturn(Optional.empty());

        assertThat(figurineService.findBestMatchingFigurine(MYTH_CLOTH_EX, "seiya")).isEmpty();
        verify(figurineCache, never()).put(eq("by-mythcloth-ex"), any());
    }

    @Test
    void findBestMatchingFigurine_shouldReturnEmpty_whenSimilarityIsBelowThreshold() {
        when(cacheManager.getCache(FigurineService.FIGURINE_CACHE)).thenReturn(figurineCache);
        when(figurineCache.get("by-mythcloth-ex", List.class)).thenReturn(List.of(new CachedFigurine(30L, "A")));

        assertThat(figurineService.findBestMatchingFigurine(MYTH_CLOTH_EX, "zzzzzzzzzz")).isEmpty();
    }

    @Test
    void findBestMatchingFigurine_shouldPreferLatestCandidate_whenSimilarityTies() {
        Figurine thresholdMatch = figurine(33L, "seiya", "Seiya", RELEASED);
        Figurine other = figurine(34L, "shiryu", "Shiryu", RELEASED);

        when(cacheManager.getCache(FigurineService.FIGURINE_CACHE)).thenReturn(figurineCache);
        when(figurineCache.get("by-mythcloth-ex", List.class))
                .thenReturn(List.of(new CachedFigurine(33L, "Seiya"), new CachedFigurine(34L, "Seiya")));
        when(figurineRepository.findById(33L)).thenReturn(Optional.of(thresholdMatch));
        when(figurineRepository.findById(34L)).thenReturn(Optional.of(other));

        assertThat(figurineService.findBestMatchingFigurine(MYTH_CLOTH_EX, "Seiya")).contains(other);
    }

    @Test
    void findBestMatchingFigurine_shouldNotQueryRepository_whenNoSimilarityReachesThreshold() {
        when(cacheManager.getCache(FigurineService.FIGURINE_CACHE)).thenReturn(figurineCache);
        when(figurineCache.get("by-mythcloth-ex", List.class)).thenReturn(List.of(new CachedFigurine(34L, "A")));

        assertThat(figurineService.findBestMatchingFigurine(MYTH_CLOTH_EX, "zzzzzzzzzz")).isEmpty();
        verify(figurineRepository, never()).findById(34L);
    }

    @Test
    void findBestMatchingFigurine_shouldReturnBestMatch_whenSimilarityIsHighEnough() {
        LineUp lineUp = lineUp("Myth Cloth EX");
        Figurine match = figurine(30L, "seiya", "Seiya", RELEASED);

        when(cacheManager.getCache(FigurineService.FIGURINE_CACHE)).thenReturn(figurineCache);
        when(figurineCache.get("by-mythcloth-ex", List.class)).thenReturn(null);
        when(lineUpRepository.findByDescription("Myth Cloth EX")).thenReturn(Optional.of(lineUp));
        when(figurineRepository.findAllByLineup(lineUp))
                .thenReturn(List.of(match, figurine(31L, "vega", "Vega", ReleaseStatus.RUMORED),
                        figurine(32L, "andromeda", "Andromeda", ReleaseStatus.ANNOUNCED)));
        when(figurineRepository.findById(30L)).thenReturn(Optional.of(match));

        assertThat(figurineService.findBestMatchingFigurine(MYTH_CLOTH_EX, "Seiya")).contains(match);
        verify(figurineCache).put(eq("by-mythcloth-ex"), any());
    }

    @Test
    void calculatePriceWithTax_shouldReturnNull_whenPriceIsMissingOrInvalid() {
        assertThat(figurineService.calculatePriceWithTax(null)).isNull();

        FigurineDistributor noPrice = distributor(CurrencyCode.JPY, null, null, null, null, false);
        assertThat(figurineService.calculatePriceWithTax(noPrice)).isNull();

        FigurineDistributor zeroPrice = distributor(CurrencyCode.JPY, 0.0, null, null, null, false);
        assertThat(figurineService.calculatePriceWithTax(zeroPrice)).isNull();
    }

    @Test
    void calculatePriceWithTax_shouldApplyCurrencyRules() {
        assertThat(figurineService.calculatePriceWithTax(
                distributor(CurrencyCode.JPY, 100.0, null, null, LocalDate.of(1997, 3, 31), false)))
                .isCloseTo(103.0, within(0.0001));
        assertThat(figurineService.calculatePriceWithTax(
                distributor(CurrencyCode.JPY, 100.0, null, null, LocalDate.of(2014, 3, 31), false)))
                .isCloseTo(105.0, within(0.0001));
        assertThat(figurineService.calculatePriceWithTax(
                distributor(CurrencyCode.JPY, 100.0, null, null, LocalDate.of(2019, 9, 30), false)))
                .isCloseTo(108.0, within(0.0001));
        assertThat(figurineService.calculatePriceWithTax(
                distributor(CurrencyCode.JPY, 100.0, null, null, LocalDate.of(2019, 10, 1), false)))
                .isCloseTo(110.0, within(0.0001));
        assertThat(figurineService.calculatePriceWithTax(distributor(CurrencyCode.MXN, 100.0, null, null, null, false)))
                .isCloseTo(116.0, within(0.0001));
        assertThat(figurineService.calculatePriceWithTax(distributor(CurrencyCode.USD, 100.0, null, null, null, false)))
                .isCloseTo(100.0, within(0.0001));
        assertThat(figurineService.calculatePriceWithTax(distributor(CurrencyCode.CNY, 100.0, null, null, null, false)))
                .isCloseTo(100.0, within(0.0001));
    }

    @Test
    void calculatePriceWithTax_shouldReturnBasePrice_whenJapaneseReleaseDateIsMissing() {
        assertThat(figurineService.calculatePriceWithTax(distributor(CurrencyCode.JPY, 100.0, null, null, null, false)))
                .isCloseTo(100.0, within(0.0001));
    }

    private FigurineReq figurineRequest() {
        return new FigurineReq("Seiya",
                List.of(new DistributorReq(1L, CurrencyCode.JPY, 1000.0, LocalDate.of(2024, 1, 1),
                        LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1), true)),
                "tamashii", 1L, 1L, 1L, 1L, 1L, true, false, false, false, false, false, false, false, false, false,
                "notes", List.of("official"), List.of("unofficial"));
    }

    private CatalogContext emptyCatalogContext() {
        return new CatalogContext(List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }

    private Figurine figurine(Long id, String normalizedName, String displayName, ReleaseStatus status) {
        Figurine figurine = new Figurine();
        figurine.setId(id);
        figurine.setNormalizedName(normalizedName);
        figurine.setDisplayName(displayName);
        figurine.setCurrentReleaseStatus(status);
        figurine.setDistributors(new ArrayList<>());
        figurine.setEvents(new ArrayList<>());
        figurine.setOfficialImages(new ArrayList<>());
        figurine.setNonOfficialImages(new ArrayList<>());
        figurine.setLineup(lineUp("Myth Cloth EX"));
        figurine.setSeries(series("Poseidon"));
        figurine.setGroup(group("God Warriors"));
        figurine.setMetalBody(false);
        figurine.setOce(false);
        figurine.setRevival(false);
        figurine.setPlainCloth(false);
        figurine.setBroken(false);
        figurine.setGolden(false);
        figurine.setGold(false);
        figurine.setManga(false);
        figurine.setSet(false);
        figurine.setArticulable(true);
        return figurine;
    }

    private FigurineDistributor distributor(CurrencyCode currency, Double price, LocalDate announcementDate,
            LocalDate preorderDate, LocalDate releaseDate, boolean confirmed) {
        FigurineDistributor distributor = new FigurineDistributor();
        distributor.setCurrency(currency);
        distributor.setPrice(price);
        distributor.setAnnouncementDate(announcementDate);
        distributor.setPreorderDate(preorderDate);
        distributor.setReleaseDate(releaseDate);
        distributor.setReleaseDateConfirmed(confirmed);
        return distributor;
    }

    private FigurineEvent event(Figurine figurine, FigurineEventType type, LocalDate date) {
        FigurineEvent event = new FigurineEvent();
        event.setType(type);
        event.setEventDate(date);
        event.setFigurine(figurine);
        return event;
    }

    private com.mesofi.mythclothapi.catalogs.model.LineUp lineUp(String description) {
        com.mesofi.mythclothapi.catalogs.model.LineUp lineUp = new com.mesofi.mythclothapi.catalogs.model.LineUp();
        lineUp.setDescription(description);
        return lineUp;
    }

    private com.mesofi.mythclothapi.catalogs.model.Series series(String description) {
        com.mesofi.mythclothapi.catalogs.model.Series series = new com.mesofi.mythclothapi.catalogs.model.Series();
        series.setDescription(description);
        return series;
    }

    private com.mesofi.mythclothapi.catalogs.model.Group group(String description) {
        com.mesofi.mythclothapi.catalogs.model.Group group = new com.mesofi.mythclothapi.catalogs.model.Group();
        group.setDescription(description);
        return group;
    }

    private com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine collectionFigurine(
            Long id) {
        com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine collectionFigurine = new com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine();
        Figurine figurine = figurine(id, "seiya-" + id, "Seiya " + id, RELEASED);
        collectionFigurine.setFigurine(figurine);
        return collectionFigurine;
    }

    private com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine collectionFigurine(
            Figurine figurine) {
        com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine collectionFigurine = new com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine();
        collectionFigurine.setFigurine(figurine);
        return collectionFigurine;
    }

    private FigurineResp figurineResponse(Figurine figurine, List<FigurineRestockResp> restocks) {
        return new FigurineResp(figurine.getId(), figurine.getNormalizedName(), figurine.getDisplayName(), List.of(),
                figurine.getTamashiiUrl(), figurine.getCurrentReleaseStatus(), null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, figurine.getRemarks(), List.of(), List.of(), List.of(),
                restocks, null, null);
    }

    private FigurineSummaryResp summaryResponse(Figurine figurine) {
        return new FigurineSummaryResp(figurine.getId(), figurine.getDisplayName(),
                new CatalogResp(1L, "Myth Cloth EX"), "official.jpg");
    }

    private FigurineRecommendationResp recommendationResponse(Figurine figurine) {
        return new FigurineRecommendationResp(figurine.getId(), figurine.getDisplayName(), "official.jpg");
    }

    private com.mesofi.mythclothapi.figurines.FigurineFilter emptyFilter() {
        return new com.mesofi.mythclothapi.figurines.FigurineFilter(List.of(), null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null);
    }
}
