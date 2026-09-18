package com.mesofi.mythclothapi.collectorspurchases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.Test;
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
import com.mesofi.mythclothapi.collectorspurchases.exceptions.CollectorPurchaseFigurineNotFoundException;
import com.mesofi.mythclothapi.collectorspurchases.model.PurchaseChannel;
import com.mesofi.mythclothapi.collectorspurchases.model.PurchaseType;
import com.mesofi.mythclothapi.collectorspurchases.model.ShippingStatus;
import com.mesofi.mythclothapi.collectorspurchases.repository.CollectorPurchaseRepository;
import com.mesofi.mythclothapi.common.CurrencyCode;
import com.mesofi.mythclothapi.config.MapperTestConfig;
import com.mesofi.mythclothapi.figurines.model.Figurine;

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

        when(collectorCollectionFigurineService.retrieveCollector(COLLECTOR_ID)).thenReturn(collector);
        when(collectorPurchaseRepository.findByCollectorOrderByOrderDateDesc(any(Collector.class),
                any(PageRequest.class))).thenReturn(List.of(createCollectorPurchase(1L), createCollectorPurchase(2L)));

        List<CollectorPurchaseResp> purchases = collectorPurchaseService.retrievePurchases(COLLECTOR_ID);
        assertThat(purchases).hasSize(2);
        assertThat(purchases).extracting(CollectorPurchaseResp::purchaseId).containsExactly(1L, 2L);
    }

    @Test
    void calculateTotalAmount_shouldReturnOne() {
        assertThat(collectorPurchaseService.calculateTotalAmount(new CollectorPurchase()))
                .isEqualByComparingTo(BigDecimal.ZERO);
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
