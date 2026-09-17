package com.mesofi.mythclothapi.collectorspurchases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.collectors.CollectorRepository;
import com.mesofi.mythclothapi.collectors.exceptions.CollectorNotFoundException;
import com.mesofi.mythclothapi.collectorscollections.CollectorCollection;
import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseFigurineReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseResp;
import com.mesofi.mythclothapi.collectorspurchases.exceptions.CollectorPurchaseFigurineNotFoundException;
import com.mesofi.mythclothapi.collectorspurchases.model.PurchaseType;
import com.mesofi.mythclothapi.collectorspurchases.model.ShippingStatus;
import com.mesofi.mythclothapi.collectorspurchases.repository.CollectorPurchaseRepository;
import com.mesofi.mythclothapi.config.MapperTestConfig;
import com.mesofi.mythclothapi.figurines.model.Figurine;

@ActiveProfiles("test")
@SpringBootTest(classes = {CollectorPurchaseService.class, MapperTestConfig.class})
public class CollectorPurchaseServiceTest {

    @Autowired
    private CollectorPurchaseService collectorPurchaseService;

    @MockitoBean
    private CollectorPurchaseRepository collectorPurchaseRepository;

    @MockitoBean
    private CollectorRepository collectorRepository;

    @Test
    void createPurchase_shouldThrowCollectorNotFoundException() {
        when(collectorRepository.findById(0L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectorPurchaseService.createPurchase(0L,
                new CollectorPurchaseReq(null, null, null, null, null, null, null, null, null)))
                .isInstanceOf(CollectorNotFoundException.class).hasMessage("Collector with id 0 was not found");
    }

    @Test
    void createPurchase_shouldThrowCollectorPurchaseFigurineNotFoundException_whenFigurineIdsAreEmpty() {
        Collector collector = new Collector();
        when(collectorRepository.findById(123L)).thenReturn(Optional.of(collector));

        assertThatThrownBy(() -> collectorPurchaseService.createPurchase(123L,
                new CollectorPurchaseReq(null, null, null, null, null, null, null, null, List.of())))
                .isInstanceOf(CollectorPurchaseFigurineNotFoundException.class)
                .hasMessage("Collector purchase figurines with IDs [] were not found");

    }

    @Test
    void createPurchase_shouldThrowCollectorPurchaseFigurineNotFoundException_whenFigurineIsNotOwned() {
        Collector collector = new Collector();
        collector.setCollections(List.of(createCollection(1L, List.of()), createCollection(2L, List.of())));

        when(collectorRepository.findById(123L)).thenReturn(Optional.of(collector));

        assertThatThrownBy(() -> collectorPurchaseService.createPurchase(123L,
                new CollectorPurchaseReq(null, null, null, null, null, null, null, null,
                        List.of(createCollectorPurchaseFigurineReq(3L, new BigDecimal("100.00")),
                                createCollectorPurchaseFigurineReq(2L, new BigDecimal("100.00"))))))
                .isInstanceOf(CollectorPurchaseFigurineNotFoundException.class)
                .hasMessage("Collector purchase figurines with IDs [3, 2] were not found");
    }

    @ParameterizedTest
    @NullSource
    @EnumSource(ShippingStatus.class)
    void createPurchase_shouldSetShippedAndDeliveredDates(ShippingStatus shippingStatus) {
        LocalDate today = LocalDate.now();

        // set up the collector with 2 collections, each with 3 figurines, some owned
        // and some not
        Collector collector = new Collector();
        collector.setId(123L);
        collector.setCollections(List.of(
                createCollection(1L,
                        List.of(createCollectorCollectionFigurine(1001L, 201L, false),
                                createCollectorCollectionFigurine(1002L, 500L, true),
                                createCollectorCollectionFigurine(1003L, 300L, true))),
                createCollection(2L,
                        List.of(createCollectorCollectionFigurine(2001L, 400L, true),
                                createCollectorCollectionFigurine(2002L, 500L, false),
                                createCollectorCollectionFigurine(2003L, 600L, true)))));

        when(collectorRepository.findById(123L)).thenReturn(Optional.of(collector));
        when(collectorPurchaseRepository.save(any(CollectorPurchase.class))).thenAnswer(invocation -> {
            CollectorPurchase purchase = invocation.getArgument(0);
            assertThat(purchase.getCollector().getId()).isEqualTo(123L);
            if (shippingStatus == ShippingStatus.SHIPPED) {
                assertThat(purchase.getShippedDate()).isEqualTo(today);
                assertThat(purchase.getDeliveredDate()).isNull();
            } else if (shippingStatus == ShippingStatus.DELIVERED) {
                assertThat(purchase.getShippedDate()).isNull();
                assertThat(purchase.getDeliveredDate()).isEqualTo(today);
            } else {
                assertThat(purchase.getShippedDate()).isNull();
                assertThat(purchase.getDeliveredDate()).isNull();
            }

            purchase.setId(1L); // Simulate saving and assigning an ID
            return purchase;
        });

        CollectorPurchaseResp collectorPurchaseResp = collectorPurchaseService.createPurchase(123L,
                new CollectorPurchaseReq(null, null, null, null, null, shippingStatus, null, null,
                        List.of(createCollectorPurchaseFigurineReq(400L, new BigDecimal("100.00")),
                                createCollectorPurchaseFigurineReq(600L, new BigDecimal("100.00")))));

        assertThat(collectorPurchaseResp).isNotNull();
        assertThat(collectorPurchaseResp)
                .extracting(CollectorPurchaseResp::purchaseId, CollectorPurchaseResp::seller,
                        CollectorPurchaseResp::orderNumber, CollectorPurchaseResp::currency,
                        CollectorPurchaseResp::totalAmount, CollectorPurchaseResp::purchaseChannel,
                        CollectorPurchaseResp::trackingNumber, CollectorPurchaseResp::carrier)
                .containsExactly(1L, null, null, null, new BigDecimal("1"), null, null, null);

        assertThat(collectorPurchaseResp.shippingStatus()).isEqualTo(shippingStatus);
        if (shippingStatus == ShippingStatus.SHIPPED) {
            assertThat(collectorPurchaseResp.shippedDate()).isEqualTo(today);
            assertThat(collectorPurchaseResp.deliveredDate()).isNull();
        } else if (shippingStatus == ShippingStatus.DELIVERED) {
            assertThat(collectorPurchaseResp.shippedDate()).isNull();
            assertThat(collectorPurchaseResp.deliveredDate()).isEqualTo(today);
        } else {
            assertThat(collectorPurchaseResp.shippedDate()).isNull();
            assertThat(collectorPurchaseResp.deliveredDate()).isNull();
        }
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
}
