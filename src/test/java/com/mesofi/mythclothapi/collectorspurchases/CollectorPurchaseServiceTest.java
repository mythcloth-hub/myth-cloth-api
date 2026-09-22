package com.mesofi.mythclothapi.collectorspurchases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.collectorscollections.CollectorCollection;
import com.mesofi.mythclothapi.collectorscollections.CollectorCollectionFigurineService;
import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseFigurineReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseResp;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseSummaryResp;
import com.mesofi.mythclothapi.collectorspurchases.dto.PurchaseSummaryResp;
import com.mesofi.mythclothapi.collectorspurchases.exceptions.CollectorPurchaseFigurineNotFoundException;
import com.mesofi.mythclothapi.collectorspurchases.exceptions.CollectorPurchaseInvalidShippingStatusException;
import com.mesofi.mythclothapi.collectorspurchases.exceptions.CollectorPurchaseNotFoundException;
import com.mesofi.mythclothapi.collectorspurchases.model.CollectorPurchase;
import com.mesofi.mythclothapi.collectorspurchases.model.CollectorPurchaseFigurine;
import com.mesofi.mythclothapi.collectorspurchases.model.PurchaseChannel;
import com.mesofi.mythclothapi.collectorspurchases.model.PurchaseType;
import com.mesofi.mythclothapi.collectorspurchases.model.ShippingStatus;
import com.mesofi.mythclothapi.common.CurrencyCode;
import com.mesofi.mythclothapi.config.MapperTestConfig;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.integration.fix.CurrencyConversionService;

@ActiveProfiles("test")
@SpringBootTest(classes = {CollectorPurchaseService.class, MapperTestConfig.class})
public class CollectorPurchaseServiceTest {

    private static final long COLLECTOR_ID = 123L;
    private static final long COLLECTION_ID = 456L;

    @Autowired
    private CollectorPurchaseService collectorPurchaseService;

    @MockitoBean
    private CollectorCollectionFigurineService collectorCollectionFigurineService;

    @MockitoBean
    private CollectorPurchaseRepository collectorPurchaseRepository;

    @MockitoBean
    private CurrencyConversionService currencyConversionService;

    @Test
    void createPurchase_shouldCreateShippedPurchaseWithMappedFigurines() {
        CollectorCollection collection = stubOwnedCollection(createCollectorCollectionFigurine(1001L, 201L, true),
                createCollectorCollectionFigurine(1002L, 202L, true));
        CollectorPurchaseReq request = createPurchaseRequest(ShippingStatus.SHIPPED,
                List.of(createCollectorPurchaseFigurineReq(1001L, new BigDecimal("100.00")),
                        createCollectorPurchaseFigurineReq(1002L, new BigDecimal("250.00"))));

        when(collectorPurchaseRepository.save(any(CollectorPurchase.class))).thenAnswer(invocation -> {
            CollectorPurchase purchase = invocation.getArgument(0);
            purchase.setId(900L);
            return purchase;
        });

        CollectorPurchaseResp response = collectorPurchaseService.createPurchase(COLLECTOR_ID, COLLECTION_ID, request);

        verify(collectorCollectionFigurineService).ensureCollectionOwnership(collection.getCollector(), COLLECTION_ID);
        ArgumentCaptor<CollectorPurchase> purchaseCaptor = ArgumentCaptor.forClass(CollectorPurchase.class);
        verify(collectorPurchaseRepository).save(purchaseCaptor.capture());

        CollectorPurchase savedPurchase = purchaseCaptor.getValue();
        assertThat(savedPurchase.getCollector()).isSameAs(collection.getCollector());
        assertThat(savedPurchase.getCollection()).isSameAs(collection);
        assertThat(savedPurchase.getOrderDate()).isEqualTo(request.purchaseDate());
        assertThat(savedPurchase.getSeller()).isEqualTo(request.seller());
        assertThat(savedPurchase.getOrderNumber()).isEqualTo(request.orderNumber());
        assertThat(savedPurchase.getCurrency().name()).isEqualTo("USD");
        assertThat(savedPurchase.getPurchaseChannel()).isEqualTo(PurchaseChannel.ONLINE);
        assertThat(savedPurchase.getShippingStatus()).isEqualTo(ShippingStatus.SHIPPED);
        assertThat(savedPurchase.getTrackingNumber()).isEqualTo(request.trackingNumber());
        assertThat(savedPurchase.getCarrier()).isEqualTo(request.carrier());
        assertThat(savedPurchase.getShippedDate()).isEqualTo(LocalDate.now());
        assertThat(savedPurchase.getDeliveredDate()).isNull();
        assertThat(savedPurchase.getFigurines()).hasSize(2);
        assertThat(savedPurchase.getFigurines())
                .extracting(purchaseFigurine -> purchaseFigurine.getCollectionFigurine().getId())
                .containsExactly(1001L, 1002L);
        assertThat(savedPurchase.getFigurines())
                .allSatisfy(purchaseFigurine -> assertThat(purchaseFigurine.getPurchase()).isSameAs(savedPurchase));

        assertThat(response.purchaseId()).isEqualTo(900L);
        assertThat(response.seller()).isEqualTo("Mandarake");
        assertThat(response.orderNumber()).isEqualTo("ORDER-123");
        assertThat(response.currency()).isEqualTo("USD");
        assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("350.00"));
        assertThat(response.purchaseChannel()).isEqualTo(PurchaseChannel.ONLINE);
        assertThat(response.shippingStatus()).isEqualTo(ShippingStatus.SHIPPED);
        assertThat(response.trackingNumber()).isEqualTo("TRACK-123");
        assertThat(response.carrier()).isEqualTo("DHL");
        assertThat(response.shippedDate()).isEqualTo(LocalDate.now());
        assertThat(response.deliveredDate()).isNull();
    }

    @Test
    void createPurchase_shouldSetDeliveredDateWhenDelivered() {
        stubOwnedCollection(createCollectorCollectionFigurine(1003L, 203L, true));
        CollectorPurchaseReq request = createPurchaseRequest(ShippingStatus.DELIVERED,
                List.of(createCollectorPurchaseFigurineReq(1003L, new BigDecimal("300.00"))));

        when(collectorPurchaseRepository.save(any(CollectorPurchase.class))).thenAnswer(invocation -> {
            CollectorPurchase purchase = invocation.getArgument(0);
            purchase.setId(901L);
            return purchase;
        });

        CollectorPurchaseResp response = collectorPurchaseService.createPurchase(COLLECTOR_ID, COLLECTION_ID, request);

        ArgumentCaptor<CollectorPurchase> purchaseCaptor = ArgumentCaptor.forClass(CollectorPurchase.class);
        verify(collectorPurchaseRepository).save(purchaseCaptor.capture());

        CollectorPurchase savedPurchase = purchaseCaptor.getValue();
        assertThat(savedPurchase.getShippedDate()).isNull();
        assertThat(savedPurchase.getDeliveredDate()).isEqualTo(LocalDate.now());
        assertThat(response.purchaseId()).isEqualTo(901L);
        assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(response.shippedDate()).isNull();
        assertThat(response.deliveredDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void createPurchase_shouldThrowCollectorPurchaseFigurineNotFoundException() {
        Collector collector = new Collector();
        collector.setId(COLLECTOR_ID);
        collector.setCollections(List.of(
                createCollection(1L,
                        List.of(createCollectorCollectionFigurine(1001L, 201L, false),
                                createCollectorCollectionFigurine(1002L, 500L, true),
                                createCollectorCollectionFigurine(1003L, 300L, true))),
                createCollection(2L,
                        List.of(createCollectorCollectionFigurine(2001L, 400L, true),
                                createCollectorCollectionFigurine(2002L, 500L, false),
                                createCollectorCollectionFigurine(2003L, 600L, true)))));

        CollectorCollection collectorCollection = new CollectorCollection();
        collectorCollection.setCollector(collector);

        when(collectorCollectionFigurineService.retrieveCollector(COLLECTOR_ID)).thenReturn(collector);
        when(collectorCollectionFigurineService.retrieveCollectorCollection(COLLECTION_ID))
                .thenReturn(collectorCollection);

        doNothing().when(collectorCollectionFigurineService).ensureCollectionOwnership(collector, COLLECTION_ID);

        assertThatThrownBy(() -> collectorPurchaseService.createPurchase(COLLECTOR_ID, COLLECTION_ID,
                new CollectorPurchaseReq(null, null, null, null, null, null, null, null,
                        List.of(createCollectorPurchaseFigurineReq(400L, new BigDecimal("100.00")),
                                createCollectorPurchaseFigurineReq(600L, new BigDecimal("100.00"))))))
                .isInstanceOf(CollectorPurchaseFigurineNotFoundException.class)
                .hasMessage("Collector purchase figurines with IDs [400, 600] were not found");
    }

    @Test
    void retrievePurchases_shouldReturnPurchases() {
        Collector collector = new Collector();
        collector.setId(COLLECTOR_ID);
        collector.setCollections(List.of(
                createCollection(1L,
                        List.of(createCollectorCollectionFigurine(1001L, 201L, false),
                                createCollectorCollectionFigurine(1002L, 500L, true),
                                createCollectorCollectionFigurine(1003L, 300L, true))),
                createCollection(2L,
                        List.of(createCollectorCollectionFigurine(2001L, 400L, true),
                                createCollectorCollectionFigurine(2002L, 500L, false),
                                createCollectorCollectionFigurine(2003L, 600L, true)))));

        CollectorCollection collectorCollection = new CollectorCollection();
        collectorCollection.setCollector(collector);

        CollectorPurchase firstPurchase = createCollectorPurchase(1L);
        firstPurchase.setFigurines(new ArrayList<>(List.of(
                createPurchaseFigurine(701L, firstPurchase, 1001L, 1, new BigDecimal("100.00"), PurchaseType.RETAIL))));

        CollectorPurchase secondPurchase = createCollectorPurchase(2L);
        secondPurchase.setFigurines(new ArrayList<>(List.of(createPurchaseFigurine(702L, secondPurchase, 1002L, 1,
                new BigDecimal("200.00"), PurchaseType.RETAIL))));

        CollectorPurchase thirdPurchase = createCollectorPurchase(3L);
        thirdPurchase.setCurrency(CurrencyCode.USD);
        thirdPurchase.setFigurines(new ArrayList<>(List.of(
                createPurchaseFigurine(703L, thirdPurchase, 1003L, 1, new BigDecimal("50.00"), PurchaseType.RETAIL))));

        when(collectorCollectionFigurineService.retrieveCollector(COLLECTOR_ID)).thenReturn(collector);
        when(collectorPurchaseRepository.findByCollectorOrderByOrderDateAsc(any(Collector.class),
                any(PageRequest.class))).thenReturn(List.of(firstPurchase, secondPurchase, thirdPurchase));
        when(currencyConversionService.convert(new BigDecimal("50.00"), "USD", "JPY"))
                .thenReturn(new BigDecimal("300.00"));

        CollectorPurchaseSummaryResp collectorPurchaseSummaryResp = collectorPurchaseService
                .retrievePurchases(COLLECTOR_ID);

        assertThat(collectorPurchaseSummaryResp).isNotNull();

        PurchaseSummaryResp purchaseSummaryResp = collectorPurchaseSummaryResp.summary();
        assertThat(purchaseSummaryResp).isNotNull();
        assertThat(purchaseSummaryResp.currency()).isEqualTo("JPY");
        assertThat(purchaseSummaryResp.totalAmount()).isEqualByComparingTo(new BigDecimal("600.00"));

        List<CollectorPurchaseResp> purchases = collectorPurchaseSummaryResp.purchases();
        assertThat(purchases).hasSize(3);
        assertThat(purchases).extracting(CollectorPurchaseResp::purchaseId).containsExactly(1L, 2L, 3L);
    }

    @Test
    void retrievePurchase_shouldThrowCollectorPurchaseNotFoundException() {
        Long purchaseId = 789L;
        Collector collector = new Collector();
        collector.setId(COLLECTOR_ID);
        collector.setCollections(List.of(
                createCollection(1L,
                        List.of(createCollectorCollectionFigurine(1001L, 201L, false),
                                createCollectorCollectionFigurine(1002L, 500L, true),
                                createCollectorCollectionFigurine(1003L, 300L, true))),
                createCollection(2L,
                        List.of(createCollectorCollectionFigurine(2001L, 400L, true),
                                createCollectorCollectionFigurine(2002L, 500L, false),
                                createCollectorCollectionFigurine(2003L, 600L, true)))));

        when(collectorCollectionFigurineService.retrieveCollector(COLLECTOR_ID)).thenReturn(collector);
        when(collectorPurchaseRepository.findByIdAndCollector(any(Long.class), any(Collector.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectorPurchaseService.retrievePurchase(COLLECTOR_ID, purchaseId))
                .isInstanceOf(CollectorPurchaseNotFoundException.class)
                .hasMessageContaining("Collector purchase with id 789 was not found");
    }

    @Test
    void retrievePurchase_shouldReturnPurchase() {
        Long purchaseId = 0L;
        Collector collector = new Collector();
        collector.setId(COLLECTOR_ID);
        collector.setCollections(List.of(
                createCollection(1L,
                        List.of(createCollectorCollectionFigurine(1001L, 201L, false),
                                createCollectorCollectionFigurine(1002L, 500L, true),
                                createCollectorCollectionFigurine(1003L, 300L, true))),
                createCollection(2L,
                        List.of(createCollectorCollectionFigurine(2001L, 400L, true),
                                createCollectorCollectionFigurine(2002L, 500L, false),
                                createCollectorCollectionFigurine(2003L, 600L, true)))));

        when(collectorCollectionFigurineService.retrieveCollector(COLLECTOR_ID)).thenReturn(collector);
        when(collectorPurchaseRepository.findByIdAndCollector(any(Long.class), any(Collector.class)))
                .thenReturn(Optional.of(createCollectorPurchase(1L)));

        CollectorPurchaseResp purchase = collectorPurchaseService.retrievePurchase(COLLECTOR_ID, purchaseId);
        assertThat(purchase).isNotNull();
        assertThat(purchase.purchaseId()).isEqualTo(1L);
    }

    @Test
    void updatePurchase_shouldCreateNewPurchaseFigurineWhenRequestIncludesNewCollectionFigurine() {
        Collector collector = new Collector();
        collector.setId(COLLECTOR_ID);

        CollectorCollection collection = createCollection(COLLECTION_ID,
                List.of(createCollectorCollectionFigurine(1001L, 201L, true),
                        createCollectorCollectionFigurine(1002L, 202L, true)));
        collection.setCollector(collector);
        collector.setCollections(List.of(collection));

        CollectorPurchase existingPurchase = createCollectorPurchase(900L);
        existingPurchase.setCollector(collector);
        existingPurchase.setFigurines(new ArrayList<>(List.of(createPurchaseFigurine(501L, existingPurchase, 1001L, 1,
                new BigDecimal("100.00"), PurchaseType.RETAIL))));

        CollectorPurchaseReq request = new CollectorPurchaseReq(LocalDate.of(2026, 9, 10), "AmiAmi", "ORDER-999",
                Currency.getInstance("USD"), PurchaseChannel.PHYSICAL_STORE, ShippingStatus.DELIVERED, "TRACK-999",
                "FedEx",
                List.of(new CollectorPurchaseFigurineReq(1001L, 2, new BigDecimal("120.00"), PurchaseType.PREORDER),
                        new CollectorPurchaseFigurineReq(1002L, 1, new BigDecimal("250.00"), PurchaseType.RETAIL)));

        when(collectorCollectionFigurineService.retrieveCollector(COLLECTOR_ID)).thenReturn(collector);
        when(collectorPurchaseRepository.findByIdAndCollector(existingPurchase.getId(), collector))
                .thenReturn(Optional.of(existingPurchase));
        when(collectorPurchaseRepository.saveAndFlush(existingPurchase)).thenReturn(existingPurchase);

        CollectorPurchaseResp response = collectorPurchaseService.updatePurchase(COLLECTOR_ID, existingPurchase.getId(),
                request);

        assertThat(existingPurchase.getFigurines()).hasSize(2);
        assertThat(existingPurchase.getFigurines())
                .extracting(purchaseFigurine -> purchaseFigurine.getCollectionFigurine().getId())
                .containsExactlyInAnyOrder(1001L, 1002L);
        assertThat(existingPurchase.getFigurines())
                .filteredOn(purchaseFigurine -> purchaseFigurine.getCollectionFigurine().getId().equals(1001L))
                .singleElement().satisfies(purchaseFigurine -> {
                    assertThat(purchaseFigurine.getId()).isEqualTo(501L);
                    assertThat(purchaseFigurine.getQuantity()).isEqualTo(2);
                    assertThat(purchaseFigurine.getPricePaid()).isEqualByComparingTo("120.00");
                    assertThat(purchaseFigurine.getPurchaseType()).isEqualTo(PurchaseType.PREORDER);
                });
        assertThat(existingPurchase.getFigurines())
                .filteredOn(purchaseFigurine -> purchaseFigurine.getCollectionFigurine().getId().equals(1002L))
                .singleElement().satisfies(purchaseFigurine -> {
                    assertThat(purchaseFigurine.getPurchase()).isSameAs(existingPurchase);
                    assertThat(purchaseFigurine.getQuantity()).isEqualTo(1);
                    assertThat(purchaseFigurine.getPricePaid()).isEqualByComparingTo("250.00");
                    assertThat(purchaseFigurine.getPurchaseType()).isEqualTo(PurchaseType.RETAIL);
                });
        assertThat(existingPurchase.getOrderDate()).isEqualTo(LocalDate.of(2026, 9, 10));
        assertThat(existingPurchase.getSeller()).isEqualTo("AmiAmi");
        assertThat(existingPurchase.getOrderNumber()).isEqualTo("ORDER-999");
        assertThat(existingPurchase.getPurchaseChannel()).isEqualTo(PurchaseChannel.PHYSICAL_STORE);
        assertThat(existingPurchase.getShippingStatus()).isEqualTo(ShippingStatus.DELIVERED);
        assertThat(existingPurchase.getTrackingNumber()).isEqualTo("TRACK-999");
        assertThat(existingPurchase.getCarrier()).isEqualTo("FedEx");
        assertThat(existingPurchase.getShippedDate()).isNull();
        assertThat(existingPurchase.getDeliveredDate()).isEqualTo(LocalDate.now());
        assertThat(response.totalAmount()).isEqualByComparingTo("490.00");
        assertThat(response.seller()).isEqualTo("AmiAmi");
        assertThat(response.orderNumber()).isEqualTo("ORDER-999");
        assertThat(response.purchaseChannel()).isEqualTo(PurchaseChannel.PHYSICAL_STORE);
        assertThat(response.shippingStatus()).isEqualTo(ShippingStatus.DELIVERED);
        assertThat(response.trackingNumber()).isEqualTo("TRACK-999");
        assertThat(response.carrier()).isEqualTo("FedEx");
        assertThat(response.deliveredDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void updatePurchase_shouldRemoveExistingPurchaseFigurineWhenMissingFromRequest() {
        Collector collector = new Collector();
        collector.setId(COLLECTOR_ID);

        CollectorCollection collection = createCollection(COLLECTION_ID,
                List.of(createCollectorCollectionFigurine(1001L, 201L, true),
                        createCollectorCollectionFigurine(1002L, 202L, true)));
        collection.setCollector(collector);
        collector.setCollections(List.of(collection));

        CollectorPurchase existingPurchase = createCollectorPurchase(901L);
        existingPurchase.setCollector(collector);
        existingPurchase.setFigurines(new ArrayList<>(List.of(
                createPurchaseFigurine(601L, existingPurchase, 1001L, 1, new BigDecimal("100.00"), PurchaseType.RETAIL),
                createPurchaseFigurine(602L, existingPurchase, 1002L, 3, new BigDecimal("200.00"),
                        PurchaseType.PREORDER))));

        CollectorPurchaseReq request = createPurchaseRequest(ShippingStatus.SHIPPED,
                List.of(new CollectorPurchaseFigurineReq(1002L, 2, new BigDecimal("210.00"), PurchaseType.RETAIL)));

        when(collectorCollectionFigurineService.retrieveCollector(COLLECTOR_ID)).thenReturn(collector);
        when(collectorPurchaseRepository.findByIdAndCollector(existingPurchase.getId(), collector))
                .thenReturn(Optional.of(existingPurchase));
        when(collectorPurchaseRepository.saveAndFlush(existingPurchase)).thenReturn(existingPurchase);

        CollectorPurchaseResp response = collectorPurchaseService.updatePurchase(COLLECTOR_ID, existingPurchase.getId(),
                request);

        assertThat(existingPurchase.getFigurines()).hasSize(1);
        assertThat(existingPurchase.getFigurines()).singleElement().satisfies(purchaseFigurine -> {
            assertThat(purchaseFigurine.getId()).isEqualTo(602L);
            assertThat(purchaseFigurine.getCollectionFigurine().getId()).isEqualTo(1002L);
            assertThat(purchaseFigurine.getQuantity()).isEqualTo(2);
            assertThat(purchaseFigurine.getPricePaid()).isEqualByComparingTo("210.00");
            assertThat(purchaseFigurine.getPurchaseType()).isEqualTo(PurchaseType.RETAIL);
        });
        assertThat(response.totalAmount()).isEqualByComparingTo("420.00");
    }

    @Test
    void updatePurchase_shouldThrowCollectorPurchaseNotFoundException() {
        long purchaseId = 999L;
        Collector collector = new Collector();
        collector.setId(COLLECTOR_ID);

        when(collectorCollectionFigurineService.retrieveCollector(COLLECTOR_ID)).thenReturn(collector);
        when(collectorPurchaseRepository.findByIdAndCollector(purchaseId, collector)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectorPurchaseService.updatePurchase(COLLECTOR_ID, purchaseId,
                createPurchaseRequest(ShippingStatus.SHIPPED,
                        List.of(createCollectorPurchaseFigurineReq(1001L, new BigDecimal("100.00"))))))
                .isInstanceOf(CollectorPurchaseNotFoundException.class)
                .hasMessageContaining("Collector purchase with id 999 was not found");
    }

    @Test
    void updatePurchaseShippingStatus_shouldThrowCollectorPurchaseNotFoundException() {
        long purchaseId = 999L;
        Collector collector = new Collector();
        collector.setId(COLLECTOR_ID);

        when(collectorCollectionFigurineService.retrieveCollector(COLLECTOR_ID)).thenReturn(collector);
        when(collectorPurchaseRepository.findByIdAndCollector(purchaseId, collector)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectorPurchaseService.updatePurchaseShippingStatus(COLLECTOR_ID, purchaseId, null))
                .isInstanceOf(CollectorPurchaseNotFoundException.class)
                .hasMessageContaining("Collector purchase with id 999 was not found");

        verify(collectorCollectionFigurineService).retrieveCollector(COLLECTOR_ID);
        verify(collectorPurchaseRepository).findByIdAndCollector(purchaseId, collector);
    }

    @Test
    void updatePurchaseShippingStatus_shouldThrowCollectorPurchaseInvalidShippingStatusException_whenShippingStatusIsNull() {
        long purchaseId = 999L;
        Collector collector = new Collector();
        collector.setId(COLLECTOR_ID);

        CollectorPurchase existingPurchase = createCollectorPurchase(901L);

        when(collectorCollectionFigurineService.retrieveCollector(COLLECTOR_ID)).thenReturn(collector);
        when(collectorPurchaseRepository.findByIdAndCollector(purchaseId, collector))
                .thenReturn(Optional.of(existingPurchase));

        assertThatThrownBy(() -> collectorPurchaseService.updatePurchaseShippingStatus(COLLECTOR_ID, purchaseId, null))
                .isInstanceOf(CollectorPurchaseInvalidShippingStatusException.class)
                .hasMessageContaining("Collector purchase has an invalid shipping status");

        verify(collectorCollectionFigurineService).retrieveCollector(COLLECTOR_ID);
        verify(collectorPurchaseRepository).findByIdAndCollector(purchaseId, collector);
    }

    @Test
    void updatePurchaseShippingStatus_shouldReturnOk_whenValidShippingStatus() {

        long purchaseId = 1000L;
        Collector collector = new Collector();
        collector.setId(COLLECTOR_ID);

        CollectorPurchase existingPurchase = createCollectorPurchase(purchaseId);
        existingPurchase.setCollector(collector);

        when(collectorCollectionFigurineService.retrieveCollector(COLLECTOR_ID)).thenReturn(collector);
        when(collectorPurchaseRepository.findByIdAndCollector(purchaseId, collector))
                .thenReturn(Optional.of(existingPurchase));
        when(collectorPurchaseRepository.saveAndFlush(existingPurchase)).thenReturn(existingPurchase);

        CollectorPurchaseResp response = collectorPurchaseService.updatePurchaseShippingStatus(COLLECTOR_ID, purchaseId,
                ShippingStatus.DELIVERED);

        assertThat(existingPurchase.getShippingStatus()).isEqualTo(ShippingStatus.DELIVERED);
        assertThat(existingPurchase.getDeliveredDate()).isEqualTo(LocalDate.now());
        assertThat(response.shippingStatus()).isEqualTo(ShippingStatus.DELIVERED);
        assertThat(response.deliveredDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void deletePurchase_shouldThrowCollectorPurchaseNotFoundException() {
        long purchaseId = 999L;

        Collector collector = new Collector();
        collector.setId(COLLECTOR_ID);

        when(collectorCollectionFigurineService.retrieveCollector(COLLECTOR_ID)).thenReturn(collector);
        when(collectorPurchaseRepository.findByIdAndCollector(purchaseId, collector)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectorPurchaseService.deletePurchase(COLLECTOR_ID, purchaseId))
                .isInstanceOf(CollectorPurchaseNotFoundException.class)
                .hasMessageContaining("Collector purchase with id 999 was not found");
    }

    @Test
    void deletePurchase_shouldDeletePurchase() {
        long purchaseId = 999L;

        Collector collector = new Collector();
        collector.setId(COLLECTOR_ID);

        CollectorPurchase purchase = new CollectorPurchase();
        purchase.setId(purchaseId);

        when(collectorCollectionFigurineService.retrieveCollector(COLLECTOR_ID)).thenReturn(collector);
        when(collectorPurchaseRepository.findByIdAndCollector(purchaseId, collector)).thenReturn(Optional.of(purchase));

        collectorPurchaseService.deletePurchase(COLLECTOR_ID, purchaseId);

        verify(collectorPurchaseRepository).delete(purchase);
    }

    @Test
    void calculateTotalAmount_shouldReturnZero() {
        assertThat(collectorPurchaseService.calculateTotalAmount(new CollectorPurchase()))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @ParameterizedTest
    @MethodSource("provideCarriersForUrlTracking")
    void generateTrackingUrl_shouldReturnExpectedUrl(String carrier, String trackingNumber, String expected) {
        CollectorPurchase purchase = new CollectorPurchase();
        purchase.setCarrier(carrier);
        purchase.setTrackingNumber(trackingNumber);

        assertThat(collectorPurchaseService.generateTrackingUrl(purchase)).isEqualTo(expected);
    }

    private static Stream<Arguments> provideCarriersForUrlTracking() {
        return Stream.of(Arguments.of(null, null, null), Arguments.of(null, "sss", null),
                Arguments.of("sss", null, null), Arguments.of("ups", null, null),
                Arguments.of("ups", "123456", "https://www.ups.com/track?tracknum=123456"),
                Arguments.of("dhl", "654321", "https://www.dhl.com/global-en/home/tracking.html?tracking-id=654321"),
                Arguments.of("fedex", "abc123", "https://www.fedex.com/fedextrack/?trknbr=abc123"),
                Arguments.of("correos de mexico", "abc123",
                        "https://www.correosdemexico.gob.mx/SSLServicios/SeguimientoEnvio/seguimientoportal2.aspx?guia=abc123"),
                Arguments.of("something-else", "abc123", null));
    }

    private CollectorCollection stubOwnedCollection(CollectorCollectionFigurine... figurines) {
        Collector collector = new Collector();
        collector.setId(COLLECTOR_ID);

        CollectorCollection collection = createCollection(COLLECTION_ID, List.of(figurines));
        collection.setCollector(collector);

        when(collectorCollectionFigurineService.retrieveCollector(COLLECTOR_ID)).thenReturn(collector);
        when(collectorCollectionFigurineService.retrieveCollectorCollection(COLLECTION_ID)).thenReturn(collection);
        doNothing().when(collectorCollectionFigurineService).ensureCollectionOwnership(collector, COLLECTION_ID);
        return collection;
    }

    private CollectorPurchaseReq createPurchaseRequest(ShippingStatus shippingStatus,
            List<CollectorPurchaseFigurineReq> figurines) {
        return new CollectorPurchaseReq(LocalDate.of(2026, 9, 1), "Mandarake", "ORDER-123", Currency.getInstance("USD"),
                PurchaseChannel.ONLINE, shippingStatus, "TRACK-123", "DHL", figurines);
    }

    private CollectorPurchaseFigurineReq createCollectorPurchaseFigurineReq(Long figurineId, BigDecimal pricePaid) {
        return new CollectorPurchaseFigurineReq(figurineId, 1, pricePaid, PurchaseType.RETAIL);
    }

    private CollectorCollection createCollection(Long collectionId, List<CollectorCollectionFigurine> figurines) {
        CollectorCollection collection = new CollectorCollection();
        collection.setId(collectionId);
        collection.setFigurines(figurines);
        return collection;
    }

    private CollectorCollectionFigurine createCollectorCollectionFigurine(Long collectionFigurineId, Long figurineId,
            boolean owned) {
        CollectorCollectionFigurine collectionFigurine = new CollectorCollectionFigurine();
        collectionFigurine.setId(collectionFigurineId);
        collectionFigurine.setFigurine(createFigurine(figurineId));
        collectionFigurine.setOwned(owned);
        return collectionFigurine;
    }

    private Figurine createFigurine(Long figurineId) {
        Figurine figurine = new Figurine();
        figurine.setId(figurineId);
        return figurine;
    }

    private CollectorPurchaseFigurine createPurchaseFigurine(Long id, CollectorPurchase purchase,
            Long collectionFigurineId, int quantity, BigDecimal pricePaid, PurchaseType purchaseType) {
        CollectorPurchaseFigurine purchaseFigurine = new CollectorPurchaseFigurine();
        purchaseFigurine.setId(id);
        purchaseFigurine.setPurchase(purchase);
        purchaseFigurine.setCollectionFigurine(createCollectorCollectionFigurine(collectionFigurineId, 0L, true));
        purchaseFigurine.setQuantity(quantity);
        purchaseFigurine.setPricePaid(pricePaid);
        purchaseFigurine.setPurchaseType(purchaseType);
        return purchaseFigurine;
    }

    private CollectorPurchase createCollectorPurchase(long id) {
        CollectorPurchase purchase = new CollectorPurchase();
        purchase.setId(id);
        purchase.setCollector(new Collector());
        purchase.setOrderDate(LocalDate.of(2026, 9, 1));
        purchase.setSeller("Mandarake");
        purchase.setOrderNumber("ORDER-123");
        purchase.setCurrency(CurrencyCode.JPY);
        purchase.setPurchaseChannel(PurchaseChannel.ONLINE);
        purchase.setShippingStatus(ShippingStatus.SHIPPED);
        purchase.setTrackingNumber("TRACK-123");
        purchase.setCarrier("DHL");
        purchase.setShippedDate(LocalDate.of(2026, 9, 2));
        purchase.setDeliveredDate(null);
        return purchase;
    }
}
