package com.mesofi.mythclothapi.figurinestores;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Sort;

import com.mesofi.mythclothapi.catalogs.model.LineUpType;
import com.mesofi.mythclothapi.figurines.FigurineNotFoundException;
import com.mesofi.mythclothapi.figurines.FigurineService;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreHistoricalPriceResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreHistoricalResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreMatchedResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreMatchedSummaryResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStorePriceResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreUnmatchedResp;
import com.mesofi.mythclothapi.figurinestores.model.CachedStores;
import com.mesofi.mythclothapi.figurinestores.model.FigurineStore;
import com.mesofi.mythclothapi.figurinestores.model.FigurineStorePricing;
import com.mesofi.mythclothapi.figurinestores.model.FigurineStoreUnmatched;
import com.mesofi.mythclothapi.figurinestores.repository.FigurineStorePricingRepository;
import com.mesofi.mythclothapi.figurinestores.repository.FigurineStoreRepository;
import com.mesofi.mythclothapi.figurinestores.repository.UnmatchedFigurineListingRepository;
import com.mesofi.mythclothapi.integration.fix.CurrencyConversionService;
import com.mesofi.mythclothapi.messaging.pricing.model.ListingStatus;
import com.mesofi.mythclothapi.messaging.pricing.model.StoreListing;
import com.mesofi.mythclothapi.messaging.pricing.model.StoreName;
import com.mesofi.mythclothapi.stores.StoreNotFoundException;
import com.mesofi.mythclothapi.stores.StoreRepository;
import com.mesofi.mythclothapi.stores.model.Store;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@ExtendWith(MockitoExtension.class)
class FigurineStoreServiceTest {

    @InjectMocks
    private FigurineStoreService service;

    @Mock
    private FigurineStoreMapper figurineStoreMapper;
    @Mock
    private FigurineService figurineService;
    @Mock
    private FigurineRepository figurineRepository;
    @Mock
    private FigurineStoreRepository figurineStoreRepository;
    @Mock
    private FigurineStorePricingRepository figurineStorePricingRepository;
    @Mock
    private UnmatchedFigurineListingRepository unmatchedFigurineListingRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache cache;
    @Mock
    private CurrencyConversionService currencyService;

    @Test
    void processStorePricing_shouldReturnEarlyWhenListingIsIgnored() {
        Store store = store(1L, "Myth Supplies", "MYTH_SUPPLIES", "USD");
        FigurineStoreUnmatched ignored = unmatchedListing(store, "Original Aries", "Aries", BigDecimal.TEN,
                BigDecimal.ONE, false);

        mockStoreCache(store);
        when(figurineStoreMapper.toStore(new CachedStores(1L, "MYTH_SUPPLIES"))).thenReturn(store);
        when(unmatchedFigurineListingRepository.findByStoreAndOriginalNameAndIgnoredTrue(store, "Original Aries"))
                .thenReturn(Optional.of(ignored));

        service.processStorePricing(storeListing(StoreName.MYTH_SUPPLIES, LineUpType.MYTH_CLOTH, "Original Aries",
                "Aries", BigDecimal.TEN, BigDecimal.ONE, "USD", ListingStatus.IN_STOCK, false));

        verify(unmatchedFigurineListingRepository).findByStoreAndOriginalNameAndIgnoredTrue(store, "Original Aries");
        verify(figurineStoreRepository, never()).findByStoreAndOriginalName(any(), any());
        verify(cache).put(eq("store"), any());
        verifyNoInteractions(figurineService, figurineStorePricingRepository);
    }

    @Test
    void processStorePricing_shouldUpdateExistingMappingAndPricing() {
        Store store = store(1L, "Myth Supplies", "MYTH_SUPPLIES", "USD");
        Figurine figurine = figurine(7L, "Aries");
        FigurineStore existing = figurineStore(figurine, store, "Original Aries", "Aries", ListingStatus.OUT_OF_STOCK,
                false);
        FigurineStorePricing existingPricing = pricing(existing, new BigDecimal("99.99"), new BigDecimal("5.00"),
                Instant.parse("2025-01-01T10:00:00Z"));

        mockStoreCache(store);
        when(figurineStoreMapper.toStore(new CachedStores(1L, "MYTH_SUPPLIES"))).thenReturn(store);
        when(unmatchedFigurineListingRepository.findByStoreAndOriginalNameAndIgnoredTrue(store, "Original Aries"))
                .thenReturn(Optional.empty());
        when(figurineStoreRepository.findByStoreAndOriginalName(store, "Original Aries"))
                .thenReturn(Optional.of(existing));
        when(figurineStorePricingRepository.findByFigurineStoreAndCurrentPrice(existing, new BigDecimal("120.00")))
                .thenReturn(Optional.of(existingPricing));

        service.processStorePricing(storeListing(StoreName.MYTH_SUPPLIES, LineUpType.MYTH_CLOTH, "Original Aries",
                "Aries", new BigDecimal("120.00"), new BigDecimal("10.00"), "USD", ListingStatus.IN_STOCK, true));

        assertThat(existing.isPreorder()).isTrue();
        assertThat(existing.getStatus()).isEqualTo(ListingStatus.IN_STOCK);
        assertThat(existingPricing.getDiscount()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(existingPricing.getCheckedAt()).isEqualTo(Instant.parse("2025-03-11T12:30:45Z"));
        verify(figurineStorePricingRepository, never()).save(any());
        verifyNoInteractions(figurineService);
    }

    @Test
    void processStorePricing_shouldCreateMatchedMappingAndPricingWhenFigurineMatches() {
        Store store = store(1L, "Myth Supplies", "MYTH_SUPPLIES", "USD");
        Figurine figurine = figurine(7L, "Aries");

        mockStoreCache(store);
        when(figurineStoreMapper.toStore(new CachedStores(1L, "MYTH_SUPPLIES"))).thenReturn(store);
        when(unmatchedFigurineListingRepository.findByStoreAndOriginalNameAndIgnoredTrue(store, "Original Aries"))
                .thenReturn(Optional.empty());
        when(figurineStoreRepository.findByStoreAndOriginalName(store, "Original Aries")).thenReturn(Optional.empty());
        when(figurineService.findBestMatchingFigurine(LineUpType.MYTH_CLOTH, "Aries"))
                .thenReturn(Optional.of(figurine));
        when(figurineStoreRepository.findByFigurineAndStoreAndOriginalName(figurine, store, "Original Aries"))
                .thenReturn(Optional.empty());
        when(figurineStoreRepository.save(any(FigurineStore.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(figurineStorePricingRepository.findByFigurineStoreAndCurrentPrice(any(FigurineStore.class),
                eq(new BigDecimal("120.00")))).thenReturn(Optional.empty());
        when(figurineStorePricingRepository.save(any(FigurineStorePricing.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.processStorePricing(storeListing(StoreName.MYTH_SUPPLIES, LineUpType.MYTH_CLOTH, "Original Aries",
                "Aries", new BigDecimal("120.00"), new BigDecimal("10.00"), "USD", ListingStatus.IN_STOCK, true));

        ArgumentCaptor<FigurineStore> figurineStoreCaptor = ArgumentCaptor.forClass(FigurineStore.class);
        verify(figurineStoreRepository).save(figurineStoreCaptor.capture());
        assertThat(figurineStoreCaptor.getValue().getFigurine()).isEqualTo(figurine);
        assertThat(figurineStoreCaptor.getValue().getStore()).isEqualTo(store);
        assertThat(figurineStoreCaptor.getValue().getOriginalName()).isEqualTo("Original Aries");
        assertThat(figurineStoreCaptor.getValue().getNormalizedName()).isEqualTo("Aries");
        assertThat(figurineStoreCaptor.getValue().getLineUp()).isEqualTo(LineUpType.MYTH_CLOTH);
        assertThat(figurineStoreCaptor.getValue().getImageUrl()).isEqualTo("https://example.com/aries.jpg");
        assertThat(figurineStoreCaptor.getValue().getProductUrl()).isEqualTo("https://example.com/aries");
        assertThat(figurineStoreCaptor.getValue().getStatus()).isEqualTo(ListingStatus.IN_STOCK);
        assertThat(figurineStoreCaptor.getValue().isPreorder()).isTrue();

        ArgumentCaptor<FigurineStorePricing> pricingCaptor = ArgumentCaptor.forClass(FigurineStorePricing.class);
        verify(figurineStorePricingRepository).save(pricingCaptor.capture());
        assertThat(pricingCaptor.getValue().getFigurineStore()).isEqualTo(figurineStoreCaptor.getValue());
        assertThat(pricingCaptor.getValue().getCurrentPrice()).isEqualByComparingTo(new BigDecimal("120.00"));
        assertThat(pricingCaptor.getValue().getDiscount()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(pricingCaptor.getValue().getCheckedAt()).isEqualTo(Instant.parse("2025-03-11T12:30:45Z"));
    }

    @Test
    void processStorePricing_shouldCreateUnmatchedListingWhenNoFigurineMatches() {
        Store store = store(1L, "Myth Supplies", "MYTH_SUPPLIES", "USD");

        try (MockedConstruction<FigurineStoreUnmatched> construction = mockConstruction(FigurineStoreUnmatched.class,
                withSettings().useConstructor().defaultAnswer(CALLS_REAL_METHODS),
                (mock, context) -> mock.setIgnored(true))) {
            mockStoreCache(store);
            when(figurineStoreMapper.toStore(new CachedStores(1L, "MYTH_SUPPLIES"))).thenReturn(store);
            when(unmatchedFigurineListingRepository.findByStoreAndOriginalNameAndIgnoredTrue(store, "Original Aries"))
                    .thenReturn(Optional.empty());
            when(figurineStoreRepository.findByStoreAndOriginalName(store, "Original Aries"))
                    .thenReturn(Optional.empty());
            when(figurineService.findBestMatchingFigurine(LineUpType.MYTH_CLOTH, "Aries")).thenReturn(Optional.empty());
            when(unmatchedFigurineListingRepository.findByStoreAndOriginalNameAndIgnoredFalse(store, "Original Aries"))
                    .thenReturn(Optional.empty());
            when(unmatchedFigurineListingRepository.save(any(FigurineStoreUnmatched.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            service.processStorePricing(storeListing(StoreName.MYTH_SUPPLIES, LineUpType.MYTH_CLOTH, "Original Aries",
                    "Aries", new BigDecimal("120.00"), new BigDecimal("10.00"), "USD", ListingStatus.IN_STOCK, true));

            ArgumentCaptor<FigurineStoreUnmatched> unmatchedCaptor = ArgumentCaptor
                    .forClass(FigurineStoreUnmatched.class);
            verify(unmatchedFigurineListingRepository).save(unmatchedCaptor.capture());
            assertThat(unmatchedCaptor.getValue().getStore()).isEqualTo(store);
            assertThat(unmatchedCaptor.getValue().getLineUp()).isEqualTo(LineUpType.MYTH_CLOTH);
            assertThat(unmatchedCaptor.getValue().getOriginalName()).isEqualTo("Original Aries");
            assertThat(unmatchedCaptor.getValue().getNormalizedName()).isEqualTo("Aries");
            assertThat(unmatchedCaptor.getValue().getImageUrl()).isEqualTo("https://example.com/aries.jpg");
            assertThat(unmatchedCaptor.getValue().getProductUrl()).isEqualTo("https://example.com/aries");
            assertThat(unmatchedCaptor.getValue().getPrice()).isEqualByComparingTo(new BigDecimal("120.00"));
            assertThat(unmatchedCaptor.getValue().getDiscount()).isEqualByComparingTo(new BigDecimal("10.00"));
            assertThat(unmatchedCaptor.getValue().getStatus()).isEqualTo(ListingStatus.IN_STOCK);
            assertThat(unmatchedCaptor.getValue().isPreorder()).isTrue();
            assertThat(unmatchedCaptor.getValue().getCheckedAt()).isEqualTo(Instant.parse("2025-03-11T12:30:45Z"));
            assertThat(unmatchedCaptor.getValue().isIgnored()).isFalse();
            assertThat(construction.constructed()).hasSize(1);
            assertThat(construction.constructed().getFirst().isIgnored()).isFalse();
        }
    }

    @Test
    void processStorePricing_shouldThrowWhenStoreCodeIsMissing() {
        mockStoreCache(store(1L, "Other Store", "OTHER_STORE", "USD"));

        assertThatThrownBy(() -> service.processStorePricing(
                storeListing(StoreName.MYTH_SUPPLIES, LineUpType.MYTH_CLOTH, "Original Aries", "Aries",
                        new BigDecimal("120.00"), new BigDecimal("10.00"), "USD", ListingStatus.IN_STOCK, true)))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Store not found for code: MYTH_SUPPLIES");
    }

    @Test
    void retrieveMatchedFigurineListingSummary_shouldReturnStoreSummariesAndCountFigurines() {
        Store first = store(1L, "A Store", "A_STORE", "USD");
        Store second = store(2L, "B Store", "B_STORE", "JPY");

        when(storeRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of(first, second));
        when(figurineStoreRepository.countByStore(first)).thenReturn(2L);
        when(figurineStoreRepository.countByStore(second)).thenReturn(1L);
        when(figurineStoreMapper.toFigurineStoreMatchedSummaryResp(first, 2L))
                .thenReturn(new FigurineStoreMatchedSummaryResp(1L, "A Store", "https://a.store", "https://a.logo",
                        Currency.getInstance("USD"), "US", 2));
        when(figurineStoreMapper.toFigurineStoreMatchedSummaryResp(second, 1L))
                .thenReturn(new FigurineStoreMatchedSummaryResp(2L, "B Store", "https://b.store", "https://b.logo",
                        Currency.getInstance("JPY"), "JP", 1));

        List<FigurineStoreMatchedSummaryResp> result = service.retrieveMatchedFigurineListingSummary();

        assertThat(result).extracting(FigurineStoreMatchedSummaryResp::storeName).containsExactly("A Store", "B Store");
        verify(figurineStoreRepository).countByStore(first);
        verify(figurineStoreRepository).countByStore(second);
    }

    @Test
    void retrieveMatchedFigurineListing_shouldReturnMatchedListingsAndUseStoreCurrency() {
        Store store = store(3L, "Myth Supplies", "MYTH_SUPPLIES", "USD");
        Figurine figurine = figurine(7L, "Aries");
        FigurineStore figurineStore = figurineStore(figurine, store, "Original Aries", "Aries", ListingStatus.IN_STOCK,
                false);
        FigurineStorePricing pricing = pricing(figurineStore, new BigDecimal("120.00"), new BigDecimal("10.00"),
                Instant.parse("2025-03-11T12:30:45Z"));

        when(storeRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(store));
        when(figurineStoreRepository.findByStoreOrderByOriginalName(store)).thenReturn(List.of(figurineStore));
        when(figurineStorePricingRepository.findByFigurineStoreOrderByCreationDateAsc(figurineStore))
                .thenReturn(List.of(pricing));
        when(figurineStoreMapper.toFigurineStoreMatchedResp(eq(figurineStore), eq("Aries"),
                eq(Currency.getInstance("USD")), eq(List.of(pricing))))
                .thenReturn(new FigurineStoreMatchedResp(8L, 7L, "Aries", "MYTH_CLOTH",
                        "https://example.com/figurine.jpg", "https://example.com/tamashii", 3L,
                        Currency.getInstance("USD"), "Original Aries", "https://example.com/product.jpg",
                        "https://example.com/product", ListingStatus.IN_STOCK, false,
                        List.of(new FigurineStorePriceResp(new BigDecimal("120.00"), "USD"))));

        List<FigurineStoreMatchedResp> result = service.retrieveMatchedFigurineListing(3L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().figurineDisplayableName()).isEqualTo("Aries");
        verify(storeRepository).findByIdAndActiveTrue(3L);
        verify(figurineStorePricingRepository).findByFigurineStoreOrderByCreationDateAsc(figurineStore);
    }

    @Test
    void retrieveMatchedFigurineListing_shouldThrowWhenStoreIsMissing() {
        when(storeRepository.findByIdAndActiveTrue(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.retrieveMatchedFigurineListing(9L)).isInstanceOf(StoreNotFoundException.class)
                .hasMessageContaining("Store with id 9 was not found");
    }

    @Test
    void manuallyUnmatchFigurineListings_shouldMoveListingBackToUnmatchedQueue() {
        Store store = store(3L, "Myth Supplies", "MYTH_SUPPLIES", "USD");
        Figurine figurine = figurine(7L, "Aries");
        FigurineStore figurineStore = figurineStore(figurine, store, "Original Aries", "Aries", ListingStatus.IN_STOCK,
                true);
        FigurineStorePricing pricing = pricing(figurineStore, new BigDecimal("120.00"), new BigDecimal("10.00"),
                Instant.parse("2025-03-11T12:30:45Z"));

        when(figurineStoreRepository.findById(8L)).thenReturn(Optional.of(figurineStore));
        when(figurineStorePricingRepository.findByFigurineStoreOrderByCreationDateAsc(figurineStore))
                .thenReturn(List.of(pricing));
        when(unmatchedFigurineListingRepository.findByStoreAndOriginalNameAndIgnoredFalse(store, "Original Aries"))
                .thenReturn(Optional.empty());
        when(unmatchedFigurineListingRepository.save(any(FigurineStoreUnmatched.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.manuallyUnmatchFigurineListings(List.of(8L));

        ArgumentCaptor<FigurineStoreUnmatched> captor = ArgumentCaptor.forClass(FigurineStoreUnmatched.class);
        verify(unmatchedFigurineListingRepository).save(captor.capture());
        assertThat(captor.getValue().getStore()).isEqualTo(store);
        assertThat(captor.getValue().getOriginalName()).isEqualTo("Original Aries");
        assertThat(captor.getValue().getNormalizedName()).isEqualTo("Aries");
        verify(figurineStorePricingRepository).deleteAll(List.of(pricing));
        verify(figurineStoreRepository).delete(figurineStore);
    }

    @Test
    void manuallyUnmatchFigurineListings_shouldThrowWhenPricingIsMissing() {
        FigurineStore figurineStore = figurineStore(figurine(7L, "Aries"),
                store(3L, "Myth Supplies", "MYTH_SUPPLIES", "USD"), "Original Aries", "Aries", ListingStatus.IN_STOCK,
                true);
        when(figurineStoreRepository.findById(8L)).thenReturn(Optional.of(figurineStore));
        when(figurineStorePricingRepository.findByFigurineStoreOrderByCreationDateAsc(figurineStore))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.manuallyUnmatchFigurineListings(List.of(8L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No pricing data found for FigurineStore ID: 8");
    }

    @Test
    void manuallyUnmatchFigurineListings_shouldThrowWhenFigurineStoreIsMissing() {
        when(figurineStoreRepository.findById(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.manuallyUnmatchFigurineListings(List.of(8L)))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("FigurineStore not found for ID: 8");
    }

    @Test
    void ignoreUnmatchedFigurineListing_shouldToggleIgnoredFlag() {
        FigurineStoreUnmatched unmatched = unmatchedListing(store(3L, "Myth Supplies", "MYTH_SUPPLIES", "USD"),
                "Original Aries", "Aries", BigDecimal.TEN, BigDecimal.ONE, false);

        when(unmatchedFigurineListingRepository.findById(8L)).thenReturn(Optional.of(unmatched));

        service.ignoreUnmatchedFigurineListing(8L, true);

        assertThat(unmatched.isIgnored()).isTrue();
    }

    @Test
    void ignoreUnmatchedFigurineListing_shouldLogUnignoredWhenSetToFalse() {
        FigurineStoreUnmatched unmatched = unmatchedListing(store(3L, "Myth Supplies", "MYTH_SUPPLIES", "USD"),
                "Original Aries", "Aries", BigDecimal.TEN, BigDecimal.ONE, true);

        when(unmatchedFigurineListingRepository.findById(8L)).thenReturn(Optional.of(unmatched));

        ListAppender<ILoggingEvent> appender = attachAppender();
        try {
            service.ignoreUnmatchedFigurineListing(8L, false);
        } finally {
            detachAppender(appender);
        }

        assertThat(appender.list).anySatisfy(event -> assertThat(event.getFormattedMessage())
                .isEqualTo("Unmatched figurine listing 8 has been unignored."));
    }

    @Test
    void ignoreUnmatchedFigurineListing_shouldLogIgnoredWhenSetToTrue() {
        FigurineStoreUnmatched unmatched = unmatchedListing(store(3L, "Myth Supplies", "MYTH_SUPPLIES", "USD"),
                "Original Aries", "Aries", BigDecimal.TEN, BigDecimal.ONE, false);

        when(unmatchedFigurineListingRepository.findById(8L)).thenReturn(Optional.of(unmatched));

        ListAppender<ILoggingEvent> appender = attachAppender();
        try {
            service.ignoreUnmatchedFigurineListing(8L, true);
        } finally {
            detachAppender(appender);
        }

        assertThat(appender.list).anySatisfy(event -> assertThat(event.getFormattedMessage())
                .isEqualTo("Unmatched figurine listing 8 has been ignored."));
    }

    @Test
    void ignoreUnmatchedFigurineListing_shouldThrowWhenListingIsMissing() {
        when(unmatchedFigurineListingRepository.findById(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.ignoreUnmatchedFigurineListing(8L, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unmatched figurine listing not found for ID: 8");
    }

    @Test
    void retrieveUnmatchedFigurineListings_shouldReturnMappedListingsWithSort() {
        FigurineStoreUnmatched first = unmatchedListing(store(1L, "A Store", "A_STORE", "USD"), "Original A", "A",
                BigDecimal.ONE, BigDecimal.ZERO, false);
        FigurineStoreUnmatched second = unmatchedListing(store(2L, "B Store", "B_STORE", "USD"), "Original B", "B",
                BigDecimal.TEN, BigDecimal.ONE, true);

        when(unmatchedFigurineListingRepository.findAll(any(Sort.class))).thenReturn(List.of(first, second));
        when(figurineStoreMapper.toFigurineStoreUnmatchedResp(first)).thenReturn(new FigurineStoreUnmatchedResp(1L, 1L,
                "https://a.store", "https://a.logo", "Original A", "https://a.image", "https://a.product", false));
        when(figurineStoreMapper.toFigurineStoreUnmatchedResp(second)).thenReturn(new FigurineStoreUnmatchedResp(2L, 2L,
                "https://b.store", "https://b.logo", "Original B", "https://b.image", "https://b.product", true));

        List<FigurineStoreUnmatchedResp> result = service.retrieveUnmatchedFigurineListings();

        assertThat(result).extracting(FigurineStoreUnmatchedResp::originalName).containsExactly("Original A",
                "Original B");

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(unmatchedFigurineListingRepository).findAll(sortCaptor.capture());
        assertThat(sortCaptor.getValue())
                .isEqualTo(Sort.by(Sort.Order.asc("store.id"), Sort.Order.asc("originalName")));
    }

    @Test
    void matchUnmatchedListingToFigurine_shouldMatchAndDeleteUnmatchedListing() {
        Store store = store(3L, "Myth Supplies", "MYTH_SUPPLIES", "USD");
        Figurine figurine = figurine(7L, "Aries");
        FigurineStoreUnmatched unmatched = unmatchedListing(store, "Original Aries", "Aries", new BigDecimal("120.00"),
                new BigDecimal("10.00"), false);
        FigurineStore stored = figurineStore(figurine, store, "Original Aries", "Aries", ListingStatus.IN_STOCK, false);
        FigurineStorePricing pricing = pricing(stored, new BigDecimal("120.00"), new BigDecimal("10.00"),
                Instant.parse("2025-03-11T12:30:45Z"));

        when(unmatchedFigurineListingRepository.findById(11L)).thenReturn(Optional.of(unmatched));
        when(figurineRepository.findById(42L)).thenReturn(Optional.of(figurine));
        when(figurineStoreRepository.findByFigurineAndStoreAndOriginalName(figurine, store, "Original Aries"))
                .thenReturn(Optional.empty());
        when(figurineStoreRepository.save(any(FigurineStore.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(figurineStorePricingRepository.findByFigurineStoreAndCurrentPrice(any(FigurineStore.class),
                eq(new BigDecimal("120.00")))).thenReturn(Optional.empty());
        when(figurineStorePricingRepository.save(any(FigurineStorePricing.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.matchUnmatchedListingToFigurine(11L, 42L);

        verify(unmatchedFigurineListingRepository).delete(unmatched);
        verify(figurineStoreRepository).save(any(FigurineStore.class));
        verify(figurineStorePricingRepository).save(any(FigurineStorePricing.class));
    }

    @Test
    void matchUnmatchedListingToFigurine_shouldThrowWhenUnmatchedListingIsMissing() {
        when(unmatchedFigurineListingRepository.findById(11L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.matchUnmatchedListingToFigurine(11L, 42L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unmatched figurine listing not found for ID: 11");
    }

    @Test
    void matchUnmatchedListingToFigurine_shouldThrowWhenFigurineIsMissing() {
        FigurineStoreUnmatched unmatched = unmatchedListing(store(3L, "Myth Supplies", "MYTH_SUPPLIES", "USD"),
                "Original Aries", "Aries", new BigDecimal("120.00"), new BigDecimal("10.00"), false);

        when(unmatchedFigurineListingRepository.findById(11L)).thenReturn(Optional.of(unmatched));
        when(figurineRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.matchUnmatchedListingToFigurine(11L, 42L))
                .isInstanceOf(FigurineNotFoundException.class).hasMessage("Figurine with id 42 was not found");
    }

    @Test
    void retrieveAverageRealtimePrice_shouldAverageConvertedCurrentPrices() {
        Figurine figurine = figurine(42L, "Seiya");
        Store usdStore = store(1L, "US Store", "US_STORE", "USD");
        Store jpyStore = store(2L, "JP Store", "JP_STORE", "JPY");

        FigurineStore usdListing = figurineStore(figurine, usdStore, "Original Seiya", "Seiya", ListingStatus.IN_STOCK,
                false);
        usdListing.setPrices(
                List.of(pricing(usdListing, new BigDecimal("100.00"), null, Instant.parse("2025-03-11T10:00:00Z"))));

        FigurineStore jpyListing = figurineStore(figurine, jpyStore, "Original Seiya", "Seiya", ListingStatus.IN_STOCK,
                false);
        jpyListing.setPrices(
                List.of(pricing(jpyListing, new BigDecimal("200.00"), null, Instant.parse("2025-03-11T11:00:00Z"))));

        when(figurineRepository.findById(42L)).thenReturn(Optional.of(figurine));
        when(figurineStoreRepository.findByFigurine(figurine)).thenReturn(List.of(usdListing, jpyListing));
        when(currencyService.convert(new BigDecimal("100.00"), "USD", "USD")).thenReturn(new BigDecimal("100.00"));
        when(currencyService.convert(new BigDecimal("200.00"), "JPY", "USD")).thenReturn(new BigDecimal("2.00"));

        FigurineStorePriceResp result = service.retrieveAverageRealtimePrice(42L, Currency.getInstance("USD"));

        assertThat(result.realTimePrice()).isEqualByComparingTo(new BigDecimal("51.00"));
        assertThat(result.currency()).isEqualTo("USD");
    }

    @Test
    void retrieveAverageRealtimePrice_shouldReturnZeroWhenNoPricesExist() {
        Figurine figurine = figurine(42L, "Seiya");
        FigurineStore storeListing = figurineStore(figurine, store(1L, "US Store", "US_STORE", "USD"), "Original Seiya",
                "Seiya", ListingStatus.IN_STOCK, false);
        storeListing.setPrices(List.of());

        when(figurineRepository.findById(42L)).thenReturn(Optional.of(figurine));
        when(figurineStoreRepository.findByFigurine(figurine)).thenReturn(List.of(storeListing));

        FigurineStorePriceResp result = service.retrieveAverageRealtimePrice(42L, Currency.getInstance("USD"));

        assertThat(result.realTimePrice()).isZero();
        assertThat(result.currency()).isEqualTo("USD");
        verifyNoInteractions(currencyService);
    }

    @Test
    void retrieveAverageRealtimePrice_shouldThrowWhenFigurineIsMissing() {
        when(figurineRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.retrieveAverageRealtimePrice(42L, Currency.getInstance("USD")))
                .isInstanceOf(FigurineNotFoundException.class).hasMessage("Figurine with id 42 was not found");
    }

    @Test
    void retrieveHistoricalPrices_shouldReturnAllStorePricesAndSortDescending() {
        Figurine figurine = figurine(42L, "Aries");
        Store firstStore = store(1L, "First Store", "FIRST_STORE", "USD");
        Store secondStore = store(2L, "Second Store", "SECOND_STORE", "EUR");

        FigurineStore firstListing = figurineStore(figurine, firstStore, "Original Aries", "Aries",
                ListingStatus.IN_STOCK, false);
        firstListing.setProductUrl("https://first/product");
        firstListing.setPrices(
                List.of(pricing(firstListing, new BigDecimal("100.00"), null, Instant.parse("2025-03-11T12:00:00Z"))));

        FigurineStore secondListing = figurineStore(figurine, secondStore, "Original Aries", "Aries",
                ListingStatus.IN_STOCK, false);
        secondListing.setProductUrl("https://second/product");
        secondListing.setPrices(
                List.of(pricing(secondListing, new BigDecimal("200.00"), null, Instant.parse("2025-03-11T13:00:00Z"))));

        when(figurineRepository.findById(42L)).thenReturn(Optional.of(figurine));
        when(figurineStoreRepository.findByFigurine(figurine)).thenReturn(List.of(firstListing, secondListing));
        when(currencyService.convert(new BigDecimal("100.00"), "USD", "JPY")).thenReturn(new BigDecimal("10000.00"));
        when(currencyService.convert(new BigDecimal("200.00"), "EUR", "JPY")).thenReturn(new BigDecimal("30000.00"));

        FigurineStoreHistoricalResp result = service.retrieveHistoricalPrices(42L, null, Currency.getInstance("JPY"));

        assertThat(result.currency()).isEqualTo("JPY");
        assertThat(result.prices()).extracting(FigurineStoreHistoricalPriceResp::checkedAt)
                .containsExactly(Instant.parse("2025-03-11T13:00:00Z"), Instant.parse("2025-03-11T12:00:00Z"));
    }

    @Test
    void retrieveHistoricalPrices_shouldPreserveStoreCurrencyWhenRequestedCurrencyIsDefault() {
        Figurine figurine = figurine(42L, "Aries");
        Store store = store(3L, "Myth Supplies", "MYTH_SUPPLIES", "USD");
        FigurineStore listing = figurineStore(figurine, store, "Original Aries", "Aries", ListingStatus.IN_STOCK,
                false);
        listing.setProductUrl("https://product");
        listing.setPrices(
                List.of(pricing(listing, new BigDecimal("150.00"), null, Instant.parse("2025-03-11T12:00:00Z"))));

        when(figurineRepository.findById(42L)).thenReturn(Optional.of(figurine));
        when(storeRepository.findById(3L)).thenReturn(Optional.of(store));
        when(figurineStoreRepository.findByFigurineAndStore(figurine, store)).thenReturn(List.of(listing));

        FigurineStoreHistoricalResp result = service.retrieveHistoricalPrices(42L, 3L, Currency.getInstance("JPY"));

        assertThat(result.currency()).isEqualTo("USD");
        assertThat(result.prices()).singleElement()
                .satisfies(price -> assertThat(price.price()).isEqualByComparingTo(new BigDecimal("150.00")));
        verifyNoInteractions(currencyService);
    }

    @Test
    void retrieveHistoricalPrices_shouldConvertStoreSpecificPricesWhenRequestedCurrencyIsNotDefault() {
        Figurine figurine = figurine(42L, "Aries");
        Store store = store(3L, "Myth Supplies", "MYTH_SUPPLIES", "USD");
        FigurineStore listing = figurineStore(figurine, store, "Original Aries", "Aries", ListingStatus.IN_STOCK,
                false);
        listing.setProductUrl("https://product");
        listing.setPrices(
                List.of(pricing(listing, new BigDecimal("150.00"), null, Instant.parse("2025-03-11T12:00:00Z"))));

        when(figurineRepository.findById(42L)).thenReturn(Optional.of(figurine));
        when(storeRepository.findById(3L)).thenReturn(Optional.of(store));
        when(figurineStoreRepository.findByFigurineAndStore(figurine, store)).thenReturn(List.of(listing));
        when(currencyService.convert(new BigDecimal("150.00"), "USD", "USD")).thenReturn(new BigDecimal("150.00"));

        FigurineStoreHistoricalResp result = service.retrieveHistoricalPrices(42L, 3L, Currency.getInstance("USD"));

        assertThat(result.currency()).isEqualTo("USD");
        assertThat(result.prices()).singleElement()
                .satisfies(price -> assertThat(price.price()).isEqualByComparingTo(new BigDecimal("150.00")));
        verify(currencyService).convert(new BigDecimal("150.00"), "USD", "USD");
    }

    @Test
    void retrieveHistoricalPrices_shouldThrowWhenFigurineIsMissing() {
        when(figurineRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.retrieveHistoricalPrices(42L, null, Currency.getInstance("USD")))
                .isInstanceOf(FigurineNotFoundException.class).hasMessage("Figurine with id 42 was not found");
    }

    @Test
    void retrieveHistoricalPrices_shouldThrowWhenStoreIsMissing() {
        Figurine figurine = figurine(42L, "Aries");
        when(figurineRepository.findById(42L)).thenReturn(Optional.of(figurine));
        when(storeRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.retrieveHistoricalPrices(42L, 3L, Currency.getInstance("USD")))
                .isInstanceOf(StoreNotFoundException.class).hasMessageContaining("Store with id 3 was not found");
    }

    private void mockStoreCache(Store store) {
        when(cacheManager.getCache("stores")).thenReturn(cache);
        doReturn(null).when(cache).get("store", List.class);
        when(storeRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of(store));
        when(figurineStoreMapper.toStoreCache(store)).thenReturn(new CachedStores(store.getId(), store.getCode()));
    }

    private ListAppender<ILoggingEvent> attachAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(FigurineStoreService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }

    private void detachAppender(ListAppender<ILoggingEvent> appender) {
        Logger logger = (Logger) LoggerFactory.getLogger(FigurineStoreService.class);
        logger.detachAppender(appender);
    }

    private Store store(long id, String name, String code, String currency) {
        Store store = new Store();
        store.setId(id);
        store.setName(name);
        store.setCode(code);
        store.setWebsite("https://example.com/" + code.toLowerCase());
        store.setLogoUrl("https://example.com/" + code.toLowerCase() + ".png");
        store.setCurrency(currency);
        store.setCountry("JP");
        store.setActive(true);
        return store;
    }

    private Figurine figurine(long id, String normalizedName) {
        Figurine figurine = new Figurine();
        figurine.setId(id);
        figurine.setNormalizedName(normalizedName);
        figurine.setDisplayName(normalizedName);
        figurine.setOfficialImages(List.of("https://example.com/" + normalizedName.toLowerCase() + ".jpg"));
        figurine.setTamashiiUrl("https://example.com/tamashii/" + normalizedName.toLowerCase());
        return figurine;
    }

    private FigurineStore figurineStore(Figurine figurine, Store store, String originalName, String normalizedName,
            ListingStatus status, boolean preorder) {
        FigurineStore figurineStore = new FigurineStore();
        figurineStore.setFigurine(figurine);
        figurineStore.setStore(store);
        figurineStore.setLineUp(LineUpType.MYTH_CLOTH);
        figurineStore.setOriginalName(originalName);
        figurineStore.setNormalizedName(normalizedName);
        figurineStore.setImageUrl("https://example.com/" + normalizedName.toLowerCase() + ".jpg");
        figurineStore.setProductUrl("https://example.com/" + normalizedName.toLowerCase());
        figurineStore.setStatus(status);
        figurineStore.setPreorder(preorder);
        return figurineStore;
    }

    private FigurineStorePricing pricing(FigurineStore figurineStore, BigDecimal price, BigDecimal discount,
            Instant checkedAt) {
        FigurineStorePricing pricing = new FigurineStorePricing();
        pricing.setFigurineStore(figurineStore);
        pricing.setCurrentPrice(price);
        pricing.setDiscount(discount);
        pricing.setCheckedAt(checkedAt);
        return pricing;
    }

    private FigurineStoreUnmatched unmatchedListing(Store store, String originalName, String normalizedName,
            BigDecimal price, BigDecimal discount, boolean ignored) {
        FigurineStoreUnmatched unmatched = new FigurineStoreUnmatched();
        unmatched.setStore(store);
        unmatched.setLineUp(LineUpType.MYTH_CLOTH);
        unmatched.setOriginalName(originalName);
        unmatched.setNormalizedName(normalizedName);
        unmatched.setImageUrl("https://example.com/" + normalizedName.toLowerCase() + ".jpg");
        unmatched.setProductUrl("https://example.com/" + normalizedName.toLowerCase());
        unmatched.setPrice(price);
        unmatched.setDiscount(discount);
        unmatched.setStatus(ListingStatus.IN_STOCK);
        unmatched.setPreorder(false);
        unmatched.setCheckedAt(Instant.parse("2025-03-11T12:30:45Z"));
        unmatched.setIgnored(ignored);
        return unmatched;
    }

    private StoreListing storeListing(StoreName store, LineUpType lineUp, String originalProductName,
            String productName, BigDecimal price, BigDecimal discount, String currency, ListingStatus status,
            boolean preorder) {
        return new StoreListing(store, lineUp, originalProductName, productName,
                "https://example.com/" + productName.toLowerCase() + ".jpg",
                "https://example.com/" + productName.toLowerCase(), price, discount, null,
                Currency.getInstance(currency), status, preorder, Instant.parse("2025-03-11T12:30:45Z"));
    }
}
