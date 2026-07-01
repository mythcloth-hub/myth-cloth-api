package com.mesofi.mythclothapi.collectorspurchases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.collectors.CollectorRepository;
import com.mesofi.mythclothapi.collectors.exceptions.CollectorNotFoundException;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseLineItemReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseSummaryLineItemReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseSummaryLineItemResp;
import com.mesofi.mythclothapi.collectorspurchases.model.CollectorPurchase;
import com.mesofi.mythclothapi.collectorspurchases.model.CollectorPurchaseFigurine;
import com.mesofi.mythclothapi.collectorspurchases.model.PurchaseType;
import com.mesofi.mythclothapi.collectorspurchases.model.ShippingStatus;
import com.mesofi.mythclothapi.collectorspurchases.repository.CollectorPurchaseFigurineRepository;
import com.mesofi.mythclothapi.collectorspurchases.repository.CollectorPurchaseRepository;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;
import com.mesofi.mythclothapi.figurines.exceptions.FigurineNotFoundException;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;

@ExtendWith(MockitoExtension.class)
public class CollectorPurchaseServiceTest {

  @InjectMocks private CollectorPurchaseService service;

  @Mock private CollectorPurchaseRepository collectorPurchaseRepository;
  @Mock private CollectorPurchaseFigurineRepository collectorPurchaseFigurineRepository;
  @Mock private CollectorRepository collectorRepository;
  @Mock private FigurineRepository figurineRepository;

  @Test
  void createSummaryLineItem_shouldThrowCollectorNotFoundException_whenCollectorDoesNotExist() {
    when(collectorRepository.findById(123L)).thenReturn(Optional.empty());

    CollectorPurchaseSummaryLineItemReq request =
        new CollectorPurchaseSummaryLineItemReq(
            LocalDate.of(2026, 6, 20),
            "Amiami",
            "OSMPHKBKI",
            CurrencyCode.JPY,
            ShippingStatus.ORDERED,
            "881682504940",
            "Fedex",
            List.of(
                new CollectorPurchaseLineItemReq(
                    101L, 1, new BigDecimal("129.99"), PurchaseType.PREORDER),
                new CollectorPurchaseLineItemReq(
                    102L, 1, new BigDecimal("130.00"), PurchaseType.RETAIL)));

    assertThatThrownBy(() -> service.createSummaryLineItem(123L, request))
        .isInstanceOf(CollectorNotFoundException.class)
        .hasMessage("Collector with id 123 was not found");

    verify(collectorRepository).findById(123L);
  }

  @Test
  void createSummaryLineItem_shouldThrowFigurineNotFoundException_whenFigurineDoesNotExist() {
    Collector collectorFound = new Collector();
    collectorFound.setId(123L);
    collectorFound.setEmail("myemail@sample.com");
    collectorFound.setCreationDate(Instant.now());

    when(collectorRepository.findById(123L)).thenReturn(Optional.of(collectorFound));
    when(collectorPurchaseRepository.save(any()))
        .thenAnswer(
            invocation -> {
              CollectorPurchase saved = invocation.getArgument(0);
              saved.setId(500L);
              return saved;
            });
    when(figurineRepository.findById(101L)).thenReturn(Optional.empty());

    CollectorPurchaseSummaryLineItemReq request =
        new CollectorPurchaseSummaryLineItemReq(
            LocalDate.of(2026, 6, 20),
            "Amiami",
            "OSMPHKBKI",
            CurrencyCode.JPY,
            ShippingStatus.ORDERED,
            "881682504940",
            "Fedex",
            List.of(
                new CollectorPurchaseLineItemReq(
                    101L, 3, new BigDecimal("100.00"), PurchaseType.PREORDER),
                new CollectorPurchaseLineItemReq(
                    102L, 1, new BigDecimal("130.00"), PurchaseType.RETAIL)));

    assertThatThrownBy(() -> service.createSummaryLineItem(123L, request))
        .isInstanceOf(FigurineNotFoundException.class)
        .hasMessage("Figurine not found");

    verify(collectorRepository).findById(123L);
    verify(collectorPurchaseRepository).save(any());
    verify(figurineRepository).findById(101L);
  }

  @Test
  void createSummaryLineItem_shouldThrowIllegalArgumentException_whenFigurineIdsAreDuplicated() {
    Collector collectorFound = new Collector();
    collectorFound.setId(123L);
    collectorFound.setEmail("myemail@sample.com");
    collectorFound.setCreationDate(Instant.now());

    when(collectorRepository.findById(123L)).thenReturn(Optional.of(collectorFound));
    when(collectorPurchaseRepository.save(any()))
        .thenAnswer(
            invocation -> {
              CollectorPurchase saved = invocation.getArgument(0);
              saved.setId(500L);
              return saved;
            });

    CollectorPurchaseSummaryLineItemReq request =
        new CollectorPurchaseSummaryLineItemReq(
            LocalDate.of(2026, 6, 20),
            "Amiami",
            "OSMPHKBKI",
            CurrencyCode.JPY,
            ShippingStatus.ORDERED,
            "881682504940",
            "Fedex",
            List.of(
                new CollectorPurchaseLineItemReq(
                    101L, 3, new BigDecimal("100.00"), PurchaseType.PREORDER),
                new CollectorPurchaseLineItemReq(
                    101L, 1, new BigDecimal("130.00"), PurchaseType.RETAIL)));

    assertThatThrownBy(() -> service.createSummaryLineItem(123L, request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Duplicate figurine ID found in line items: 101");

    verify(collectorRepository).findById(123L);
    verify(collectorPurchaseRepository).save(any());
    verifyNoInteractions(figurineRepository, collectorPurchaseFigurineRepository);
  }

  @Test
  void createSummaryLineItem_shouldSaveSummaryAndLineItems_whenShippingStatusIsOrdered() {
    Collector collectorFound = new Collector();
    collectorFound.setId(123L);
    collectorFound.setEmail("myemail@sample.com");
    collectorFound.setCreationDate(Instant.now());

    Figurine figurine1Found = new Figurine();
    figurine1Found.setId(101L);
    figurine1Found.setNormalizedName("Figurine 1");
    Figurine figurine2Found = new Figurine();
    figurine2Found.setId(102L);
    figurine2Found.setNormalizedName("Figurine 2");

    CollectorPurchase collectorPurchaseSaved = getCollectorPurchase(collectorFound);
    collectorPurchaseSaved.setTotalAmount(null);
    collectorPurchaseSaved.setTotalFigurines(null);
    collectorPurchaseSaved.setShippedDate(null);
    collectorPurchaseSaved.setDeliveredDate(null);

    CollectorPurchaseFigurine collectorPurchaseFigurine1 = new CollectorPurchaseFigurine();
    collectorPurchaseFigurine1.setId(201L);
    collectorPurchaseFigurine1.setPurchaseType(PurchaseType.PREORDER);
    collectorPurchaseFigurine1.setQuantity(3);
    collectorPurchaseFigurine1.setPricePaid(new BigDecimal("100.00"));
    CollectorPurchaseFigurine collectorPurchaseFigurine2 = new CollectorPurchaseFigurine();
    collectorPurchaseFigurine2.setId(202L);
    collectorPurchaseFigurine2.setPurchaseType(PurchaseType.RETAIL);
    collectorPurchaseFigurine2.setQuantity(1);
    collectorPurchaseFigurine2.setPricePaid(new BigDecimal("130.00"));

    when(collectorRepository.findById(123L)).thenReturn(Optional.of(collectorFound));
    when(collectorPurchaseRepository.save(any())).thenReturn(collectorPurchaseSaved);
    when(figurineRepository.findById(101L)).thenReturn(Optional.of(figurine1Found));
    when(figurineRepository.findById(102L)).thenReturn(Optional.of(figurine2Found));
    when(collectorPurchaseFigurineRepository.save(any()))
        .thenReturn(collectorPurchaseFigurine1, collectorPurchaseFigurine2);

    CollectorPurchaseSummaryLineItemReq request =
        new CollectorPurchaseSummaryLineItemReq(
            LocalDate.of(2026, 6, 20),
            "Amiami",
            "OSMPHKBKI",
            CurrencyCode.JPY,
            ShippingStatus.ORDERED,
            "881682504940",
            "Fedex",
            List.of(
                new CollectorPurchaseLineItemReq(
                    101L, 3, new BigDecimal("100.00"), PurchaseType.PREORDER),
                new CollectorPurchaseLineItemReq(
                    102L, 1, new BigDecimal("130.00"), PurchaseType.RETAIL)));

    CollectorPurchaseSummaryLineItemResp resp = service.createSummaryLineItem(123L, request);

    ArgumentCaptor<CollectorPurchase> purchaseCaptor =
        ArgumentCaptor.forClass(CollectorPurchase.class);
    verify(collectorPurchaseRepository).save(purchaseCaptor.capture());
    CollectorPurchase savedPurchase = purchaseCaptor.getValue();
    assertThat(savedPurchase.getCollector()).isEqualTo(collectorFound);
    assertThat(savedPurchase.getOrderDate()).isEqualTo(LocalDate.of(2026, 6, 20));
    assertThat(savedPurchase.getStore()).isEqualTo("Amiami");
    assertThat(savedPurchase.getOrderNumber()).isEqualTo("OSMPHKBKI");
    assertThat(savedPurchase.getCurrency()).isEqualTo(CurrencyCode.JPY);
    assertThat(savedPurchase.getTotalAmount()).isEqualTo(new BigDecimal("430.00"));
    assertThat(savedPurchase.getTotalFigurines()).isEqualTo(4);
    assertThat(savedPurchase.getShippingStatus()).isEqualTo(ShippingStatus.ORDERED);
    assertThat(savedPurchase.getTrackingNumber()).isEqualTo("881682504940");
    assertThat(savedPurchase.getCarrier()).isEqualTo("Fedex");
    assertThat(savedPurchase.getShippedDate()).isNull();
    assertThat(savedPurchase.getDeliveredDate()).isNull();

    ArgumentCaptor<CollectorPurchaseFigurine> figurineCaptor =
        ArgumentCaptor.forClass(CollectorPurchaseFigurine.class);
    verify(collectorPurchaseFigurineRepository, times(2)).save(figurineCaptor.capture());
    List<CollectorPurchaseFigurine> savedLineItems = figurineCaptor.getAllValues();
    assertThat(savedLineItems).hasSize(2);
    assertThat(savedLineItems.getFirst().getPurchase().getId()).isEqualTo(500L);
    assertThat(savedLineItems.getFirst().getPurchase().getCollector()).isEqualTo(collectorFound);
    assertThat(savedLineItems.getFirst().getFigurine()).isEqualTo(figurine1Found);
    assertThat(savedLineItems.getFirst().getQuantity()).isEqualTo(3);
    assertThat(savedLineItems.get(0).getPricePaid()).isEqualTo(new BigDecimal("100.00"));
    assertThat(savedLineItems.get(0).getPurchaseType()).isEqualTo(PurchaseType.PREORDER);
    assertThat(savedLineItems.get(1).getPurchase().getId()).isEqualTo(500L);
    assertThat(savedLineItems.get(1).getPurchase().getCollector()).isEqualTo(collectorFound);
    assertThat(savedLineItems.get(1).getFigurine()).isEqualTo(figurine2Found);
    assertThat(savedLineItems.get(1).getQuantity()).isEqualTo(1);
    assertThat(savedLineItems.get(1).getPricePaid()).isEqualTo(new BigDecimal("130.00"));
    assertThat(savedLineItems.get(1).getPurchaseType()).isEqualTo(PurchaseType.RETAIL);

    verify(figurineRepository).findById(101L);
    verify(figurineRepository).findById(102L);
    assertThat(resp.lineItems()).hasSize(2);
  }

  @Test
  void createSummaryLineItem_shouldSetShippedDate_whenShippingStatusIsShipped() {
    Collector collectorFound = new Collector();
    collectorFound.setId(123L);
    collectorFound.setEmail("myemail@sample.com");
    collectorFound.setCreationDate(Instant.now());

    Figurine figurine1Found = new Figurine();
    figurine1Found.setId(101L);
    figurine1Found.setNormalizedName("Figurine 1");

    CollectorPurchase collectorPurchaseSaved = getCollectorPurchase(collectorFound);
    collectorPurchaseSaved.setShippedDate(null);
    collectorPurchaseSaved.setDeliveredDate(null);

    CollectorPurchaseFigurine collectorPurchaseFigurine1 = new CollectorPurchaseFigurine();
    collectorPurchaseFigurine1.setId(201L);
    collectorPurchaseFigurine1.setPurchaseType(PurchaseType.PREORDER);
    collectorPurchaseFigurine1.setQuantity(1);

    when(collectorRepository.findById(123L)).thenReturn(Optional.of(collectorFound));
    when(collectorPurchaseRepository.save(any())).thenReturn(collectorPurchaseSaved);
    when(figurineRepository.findById(101L)).thenReturn(Optional.of(figurine1Found));
    when(collectorPurchaseFigurineRepository.save(any())).thenReturn(collectorPurchaseFigurine1);

    CollectorPurchaseSummaryLineItemReq request =
        new CollectorPurchaseSummaryLineItemReq(
            LocalDate.of(2026, 6, 20),
            "Amiami",
            "OSMPHKBKI",
            CurrencyCode.JPY,
            ShippingStatus.SHIPPED,
            "881682504940",
            "Fedex",
            List.of(
                new CollectorPurchaseLineItemReq(
                    101L, 1, new BigDecimal("129.99"), PurchaseType.PREORDER)));

    service.createSummaryLineItem(123L, request);

    ArgumentCaptor<CollectorPurchase> purchaseCaptor =
        ArgumentCaptor.forClass(CollectorPurchase.class);
    verify(collectorPurchaseRepository).save(purchaseCaptor.capture());
    assertThat(purchaseCaptor.getValue().getShippedDate()).isEqualTo(LocalDate.now());
    assertThat(purchaseCaptor.getValue().getDeliveredDate()).isNull();
  }

  @Test
  void createSummaryLineItem_shouldSetDeliveredDate_whenShippingStatusIsDelivered() {
    Collector collectorFound = new Collector();
    collectorFound.setId(123L);
    collectorFound.setEmail("myemail@sample.com");
    collectorFound.setCreationDate(Instant.now());

    Figurine figurine1Found = new Figurine();
    figurine1Found.setId(101L);
    figurine1Found.setNormalizedName("Figurine 1");

    CollectorPurchase collectorPurchaseSaved = getCollectorPurchase(collectorFound);
    collectorPurchaseSaved.setShippedDate(null);
    collectorPurchaseSaved.setDeliveredDate(null);

    CollectorPurchaseFigurine collectorPurchaseFigurine1 = new CollectorPurchaseFigurine();
    collectorPurchaseFigurine1.setId(201L);
    collectorPurchaseFigurine1.setPurchaseType(PurchaseType.PREORDER);
    collectorPurchaseFigurine1.setQuantity(1);

    when(collectorRepository.findById(123L)).thenReturn(Optional.of(collectorFound));
    when(collectorPurchaseRepository.save(any())).thenReturn(collectorPurchaseSaved);
    when(figurineRepository.findById(101L)).thenReturn(Optional.of(figurine1Found));
    when(collectorPurchaseFigurineRepository.save(any())).thenReturn(collectorPurchaseFigurine1);

    CollectorPurchaseSummaryLineItemReq request =
        new CollectorPurchaseSummaryLineItemReq(
            LocalDate.of(2026, 6, 20),
            "Amiami",
            "OSMPHKBKI",
            CurrencyCode.JPY,
            ShippingStatus.DELIVERED,
            "881682504940",
            "Fedex",
            List.of(
                new CollectorPurchaseLineItemReq(
                    101L, 1, new BigDecimal("129.99"), PurchaseType.PREORDER)));

    service.createSummaryLineItem(123L, request);

    ArgumentCaptor<CollectorPurchase> purchaseCaptor =
        ArgumentCaptor.forClass(CollectorPurchase.class);
    verify(collectorPurchaseRepository).save(purchaseCaptor.capture());
    assertThat(purchaseCaptor.getValue().getShippedDate()).isNull();
    assertThat(purchaseCaptor.getValue().getDeliveredDate()).isEqualTo(LocalDate.now());
  }

  @Test
  void updateSummaryLineItem_shouldThrowCollectorNotFoundException_whenCollectorDoesNotExist() {
    when(collectorRepository.findById(123L)).thenReturn(Optional.empty());

    CollectorPurchaseSummaryLineItemReq request =
        new CollectorPurchaseSummaryLineItemReq(
            LocalDate.of(2026, 6, 20),
            "Amiami",
            "OSMPHKBKI",
            CurrencyCode.JPY,
            ShippingStatus.ORDERED,
            "881682504940",
            "Fedex",
            List.of(
                new CollectorPurchaseLineItemReq(
                    101L, 1, new BigDecimal("129.99"), PurchaseType.PREORDER)));

    assertThatThrownBy(() -> service.updateSummaryLineItem(123L, 500L, request))
        .isInstanceOf(CollectorNotFoundException.class)
        .hasMessage("Collector with id 123 was not found");

    verify(collectorRepository).findById(123L);
    verifyNoInteractions(
        collectorPurchaseRepository, collectorPurchaseFigurineRepository, figurineRepository);
  }

  @Test
  void
      updateSummaryLineItem_shouldThrowCollectorPurchaseNotFoundException_whenPurchaseDoesNotExist() {
    Collector collectorFound = new Collector();
    collectorFound.setId(123L);
    collectorFound.setEmail("myemail@sample.com");
    collectorFound.setCreationDate(Instant.now());

    when(collectorRepository.findById(123L)).thenReturn(Optional.of(collectorFound));
    when(collectorPurchaseRepository.findByIdAndCollectorId(500L, 123L))
        .thenReturn(Optional.empty());

    CollectorPurchaseSummaryLineItemReq request =
        new CollectorPurchaseSummaryLineItemReq(
            LocalDate.of(2026, 6, 20),
            "Amiami",
            "OSMPHKBKI",
            CurrencyCode.JPY,
            ShippingStatus.ORDERED,
            "881682504940",
            "Fedex",
            List.of(
                new CollectorPurchaseLineItemReq(
                    101L, 1, new BigDecimal("129.99"), PurchaseType.PREORDER)));

    assertThatThrownBy(() -> service.updateSummaryLineItem(123L, 500L, request))
        .isInstanceOf(
            com.mesofi.mythclothapi.collectorspurchases.exceptions
                .CollectorPurchaseNotFoundException.class)
        .hasMessage("Collector purchase not found for this id: 500");

    verify(collectorRepository).findById(123L);
    verify(collectorPurchaseRepository).findByIdAndCollectorId(500L, 123L);
    verifyNoInteractions(collectorPurchaseFigurineRepository, figurineRepository);
  }

  @SuppressWarnings("unchecked")
  @Test
  void updateSummaryLineItem_shouldReplaceLineItems_whenLineItemsChanged() {
    Collector collectorFound = new Collector();
    collectorFound.setId(123L);
    collectorFound.setEmail("myemail@sample.com");
    collectorFound.setCreationDate(Instant.now());

    Figurine figurine1Found = new Figurine();
    figurine1Found.setId(101L);
    figurine1Found.setNormalizedName("Figurine 1");

    CollectorPurchase existingPurchase = getCollectorPurchase(collectorFound);
    existingPurchase.setId(500L);

    CollectorPurchaseFigurine existingLineItem = new CollectorPurchaseFigurine();
    existingLineItem.setId(301L);
    existingLineItem.setPurchase(existingPurchase);
    existingLineItem.setFigurine(figurine1Found);
    existingLineItem.setQuantity(1);
    existingLineItem.setPricePaid(new BigDecimal("129.99"));
    existingLineItem.setPurchaseType(PurchaseType.PREORDER);

    CollectorPurchaseFigurine updatedLineItem = new CollectorPurchaseFigurine();
    updatedLineItem.setId(302L);
    updatedLineItem.setPurchase(existingPurchase);
    updatedLineItem.setFigurine(figurine1Found);
    updatedLineItem.setQuantity(2);
    updatedLineItem.setPricePaid(new BigDecimal("129.99"));
    updatedLineItem.setPurchaseType(PurchaseType.PREORDER);

    when(collectorRepository.findById(123L)).thenReturn(Optional.of(collectorFound));
    when(collectorPurchaseRepository.findByIdAndCollectorId(500L, 123L))
        .thenReturn(Optional.of(existingPurchase));
    when(collectorPurchaseRepository.save(any()))
        .thenAnswer(
            invocation -> {
              CollectorPurchase saved = invocation.getArgument(0);
              saved.setId(500L);
              return saved;
            });
    when(collectorPurchaseFigurineRepository.findByPurchase(any()))
        .thenReturn(List.of(existingLineItem), List.of(existingLineItem));
    when(figurineRepository.findById(101L)).thenReturn(Optional.of(figurine1Found));
    when(collectorPurchaseFigurineRepository.save(any())).thenReturn(updatedLineItem);

    CollectorPurchaseSummaryLineItemReq request =
        new CollectorPurchaseSummaryLineItemReq(
            LocalDate.of(2026, 6, 21),
            "Amiami updated",
            "OSMPHKBKI-2",
            CurrencyCode.JPY,
            ShippingStatus.SHIPPED,
            "TRACK-2",
            "DHL",
            List.of(
                new CollectorPurchaseLineItemReq(
                    101L, 2, new BigDecimal("129.99"), PurchaseType.PREORDER)));

    CollectorPurchaseSummaryLineItemResp response =
        service.updateSummaryLineItem(123L, 500L, request);

    verify(collectorPurchaseFigurineRepository)
        .deletePurchaseFigurineById(existingLineItem.getId());
    verify(collectorPurchaseFigurineRepository).save(any());
    assertThat(response.orderDate()).isEqualTo(LocalDate.of(2026, 6, 21));
    assertThat(response.store()).isEqualTo("Amiami updated");
    assertThat(response.orderNumber()).isEqualTo("OSMPHKBKI-2");
    assertThat(response.shippingStatus()).isEqualTo(ShippingStatus.SHIPPED);
    assertThat(response.trackingNumber()).isEqualTo("TRACK-2");
    assertThat(response.carrier()).isEqualTo("DHL");
    assertThat(response.totalAmount()).isEqualTo(new BigDecimal("259.98"));
    assertThat(response.totalFigurines()).isEqualTo(2);
    assertThat(response.shippedDate()).isEqualTo(LocalDate.now());
    assertThat(response.lineItems()).hasSize(1);
    assertThat(response.lineItems().getFirst().quantity()).isEqualTo(2);
  }

  @SuppressWarnings("unchecked")
  @Test
  void updateSummaryLineItem_shouldReuseExistingLineItems_whenLineItemsAreUnchanged() {
    Collector collectorFound = new Collector();
    collectorFound.setId(123L);
    collectorFound.setEmail("myemail@sample.com");
    collectorFound.setCreationDate(Instant.now());

    Figurine figurine1Found = new Figurine();
    figurine1Found.setId(101L);
    figurine1Found.setNormalizedName("Figurine 1");

    CollectorPurchase existingPurchase = getCollectorPurchase(collectorFound);
    existingPurchase.setId(500L);

    CollectorPurchaseFigurine existingLineItem = new CollectorPurchaseFigurine();
    existingLineItem.setId(301L);
    existingLineItem.setPurchase(existingPurchase);
    existingLineItem.setFigurine(figurine1Found);
    existingLineItem.setQuantity(1);
    existingLineItem.setPricePaid(new BigDecimal("129.99"));
    existingLineItem.setPurchaseType(PurchaseType.PREORDER);

    when(collectorRepository.findById(123L)).thenReturn(Optional.of(collectorFound));
    when(collectorPurchaseRepository.findByIdAndCollectorId(500L, 123L))
        .thenReturn(Optional.of(existingPurchase));
    when(collectorPurchaseRepository.save(any()))
        .thenAnswer(
            invocation -> {
              CollectorPurchase saved = invocation.getArgument(0);
              saved.setId(500L);
              return saved;
            });
    when(collectorPurchaseFigurineRepository.findByPurchase(any()))
        .thenReturn(List.of(existingLineItem), List.of(existingLineItem));

    CollectorPurchaseSummaryLineItemReq request =
        new CollectorPurchaseSummaryLineItemReq(
            LocalDate.of(2026, 6, 21),
            "Amiami updated",
            "OSMPHKBKI-2",
            CurrencyCode.JPY,
            ShippingStatus.ORDERED,
            "TRACK-2",
            "DHL",
            List.of(
                new CollectorPurchaseLineItemReq(
                    101L, 1, new BigDecimal("129.99"), PurchaseType.PREORDER)));

    CollectorPurchaseSummaryLineItemResp response =
        service.updateSummaryLineItem(123L, 500L, request);

    verify(collectorPurchaseFigurineRepository, never()).delete(any());
    verify(collectorPurchaseFigurineRepository, never()).save(any());
    assertThat(response.lineItems()).hasSize(1);
    assertThat(response.lineItems().getFirst().figurineId()).isEqualTo(101L);
  }

  @SuppressWarnings("unchecked")
  @Test
  void updateSummaryLineItem_shouldReplaceLineItems_whenLineItemCountDiffers() {
    Collector collectorFound = new Collector();
    collectorFound.setId(123L);
    collectorFound.setEmail("myemail@sample.com");
    collectorFound.setCreationDate(Instant.now());

    Figurine figurine1Found = new Figurine();
    figurine1Found.setId(101L);
    figurine1Found.setNormalizedName("Figurine 1");
    Figurine figurine2Found = new Figurine();
    figurine2Found.setId(102L);
    figurine2Found.setNormalizedName("Figurine 2");

    CollectorPurchase existingPurchase = getCollectorPurchase(collectorFound);
    existingPurchase.setId(500L);

    CollectorPurchaseFigurine existingLineItem = new CollectorPurchaseFigurine();
    existingLineItem.setId(301L);
    existingLineItem.setPurchase(existingPurchase);
    existingLineItem.setFigurine(figurine1Found);
    existingLineItem.setQuantity(1);
    existingLineItem.setPricePaid(new BigDecimal("129.99"));
    existingLineItem.setPurchaseType(PurchaseType.PREORDER);

    when(collectorRepository.findById(123L)).thenReturn(Optional.of(collectorFound));
    when(collectorPurchaseRepository.findByIdAndCollectorId(500L, 123L))
        .thenReturn(Optional.of(existingPurchase));
    when(collectorPurchaseRepository.save(any()))
        .thenAnswer(
            invocation -> {
              CollectorPurchase saved = invocation.getArgument(0);
              saved.setId(500L);
              return saved;
            });
    when(collectorPurchaseFigurineRepository.findByPurchase(any()))
        .thenReturn(List.of(existingLineItem), List.of(existingLineItem));
    when(figurineRepository.findById(101L)).thenReturn(Optional.of(figurine1Found));
    when(figurineRepository.findById(102L)).thenReturn(Optional.of(figurine2Found));
    when(collectorPurchaseFigurineRepository.save(any()))
        .thenAnswer(
            invocation -> {
              CollectorPurchaseFigurine saved = invocation.getArgument(0);
              saved.setId(302L);
              return saved;
            });

    CollectorPurchaseSummaryLineItemReq request =
        new CollectorPurchaseSummaryLineItemReq(
            LocalDate.of(2026, 6, 21),
            "Amiami updated",
            "OSMPHKBKI-2",
            CurrencyCode.JPY,
            ShippingStatus.ORDERED,
            "TRACK-2",
            "DHL",
            List.of(
                new CollectorPurchaseLineItemReq(
                    101L, 1, new BigDecimal("129.99"), PurchaseType.PREORDER),
                new CollectorPurchaseLineItemReq(
                    102L, 1, new BigDecimal("130.00"), PurchaseType.RETAIL)));

    CollectorPurchaseSummaryLineItemResp response =
        service.updateSummaryLineItem(123L, 500L, request);

    verify(collectorPurchaseFigurineRepository)
        .deletePurchaseFigurineById(existingLineItem.getId());
    verify(collectorPurchaseFigurineRepository, times(2)).save(any());
    assertThat(response.lineItems()).hasSize(2);
  }

  @SuppressWarnings("unchecked")
  @Test
  void updateSummaryLineItem_shouldReplaceLineItems_whenPurchaseTypeDiffers() {
    Collector collectorFound = new Collector();
    collectorFound.setId(123L);
    collectorFound.setEmail("myemail@sample.com");
    collectorFound.setCreationDate(Instant.now());

    Figurine figurine1Found = new Figurine();
    figurine1Found.setId(101L);
    figurine1Found.setNormalizedName("Figurine 1");

    CollectorPurchase existingPurchase = getCollectorPurchase(collectorFound);
    existingPurchase.setId(500L);

    CollectorPurchaseFigurine existingLineItem = new CollectorPurchaseFigurine();
    existingLineItem.setId(301L);
    existingLineItem.setPurchase(existingPurchase);
    existingLineItem.setFigurine(figurine1Found);
    existingLineItem.setQuantity(1);
    existingLineItem.setPricePaid(new BigDecimal("129.99"));
    existingLineItem.setPurchaseType(PurchaseType.PREORDER);

    when(collectorRepository.findById(123L)).thenReturn(Optional.of(collectorFound));
    when(collectorPurchaseRepository.findByIdAndCollectorId(500L, 123L))
        .thenReturn(Optional.of(existingPurchase));
    when(collectorPurchaseRepository.save(any()))
        .thenAnswer(
            invocation -> {
              CollectorPurchase saved = invocation.getArgument(0);
              saved.setId(500L);
              return saved;
            });
    when(collectorPurchaseFigurineRepository.findByPurchase(any()))
        .thenReturn(List.of(existingLineItem), List.of(existingLineItem));
    when(figurineRepository.findById(101L)).thenReturn(Optional.of(figurine1Found));
    when(collectorPurchaseFigurineRepository.save(any()))
        .thenAnswer(
            invocation -> {
              CollectorPurchaseFigurine saved = invocation.getArgument(0);
              saved.setId(302L);
              return saved;
            });

    CollectorPurchaseSummaryLineItemReq request =
        new CollectorPurchaseSummaryLineItemReq(
            LocalDate.of(2026, 6, 21),
            "Amiami updated",
            "OSMPHKBKI-2",
            CurrencyCode.JPY,
            ShippingStatus.ORDERED,
            "TRACK-2",
            "DHL",
            List.of(
                new CollectorPurchaseLineItemReq(
                    101L, 1, new BigDecimal("129.99"), PurchaseType.RETAIL)));

    CollectorPurchaseSummaryLineItemResp response =
        service.updateSummaryLineItem(123L, 500L, request);

    verify(collectorPurchaseFigurineRepository)
        .deletePurchaseFigurineById(existingLineItem.getId());
    verify(collectorPurchaseFigurineRepository).save(any());
    assertThat(response.lineItems()).hasSize(1);
    assertThat(response.lineItems().getFirst().purchaseType()).isEqualTo(PurchaseType.RETAIL);
  }

  @Test
  void retrieveSummaryLineItems_shouldReturnEmptyList_whenCollectorHasNoPurchases() {
    Collector collectorFound = new Collector();
    collectorFound.setId(123L);
    collectorFound.setEmail("myemail@sample.com");
    collectorFound.setCreationDate(Instant.now());

    when(collectorRepository.findById(123L)).thenReturn(Optional.of(collectorFound));
    when(collectorPurchaseRepository.findByCollectorIdOrderByOrderDateDescIdDesc(123L))
        .thenReturn(List.of());

    List<CollectorPurchaseSummaryLineItemResp> response = service.retrieveSummaryLineItems(123L);

    assertThat(response).isEmpty();
    verify(collectorPurchaseFigurineRepository, never()).findByPurchaseIdIn(any());
  }

  @Test
  void retrieveSummaryLineItems_shouldThrowCollectorNotFoundException_whenCollectorDoesNotExist() {
    when(collectorRepository.findById(123L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.retrieveSummaryLineItems(123L))
        .isInstanceOf(CollectorNotFoundException.class)
        .hasMessage("Collector with id 123 was not found");

    verify(collectorRepository).findById(123L);
    verifyNoInteractions(
        collectorPurchaseRepository, collectorPurchaseFigurineRepository, figurineRepository);
  }

  @Test
  void retrieveSummaryLineItems_shouldReturnSummaries_whenPurchasesExist() {
    Collector collectorFound = new Collector();
    collectorFound.setId(123L);
    collectorFound.setEmail("myemail@sample.com");
    collectorFound.setCreationDate(Instant.now());

    CollectorPurchase purchase1 = getCollectorPurchase(collectorFound);
    purchase1.setId(500L);
    CollectorPurchase purchase2 = getCollectorPurchase(collectorFound);
    purchase2.setId(501L);
    purchase2.setOrderNumber("OSMPHKBKI-2");

    Figurine figurine1Found = new Figurine();
    figurine1Found.setId(101L);
    figurine1Found.setNormalizedName("Figurine 1");
    Figurine figurine2Found = new Figurine();
    figurine2Found.setId(102L);
    figurine2Found.setNormalizedName("Figurine 2");

    CollectorPurchaseFigurine lineItem1 = new CollectorPurchaseFigurine();
    lineItem1.setId(301L);
    lineItem1.setPurchase(purchase1);
    lineItem1.setFigurine(figurine1Found);
    lineItem1.setQuantity(1);
    lineItem1.setPricePaid(new BigDecimal("129.99"));
    lineItem1.setPurchaseType(PurchaseType.PREORDER);

    CollectorPurchaseFigurine lineItem2 = new CollectorPurchaseFigurine();
    lineItem2.setId(302L);
    lineItem2.setPurchase(purchase2);
    lineItem2.setFigurine(figurine2Found);
    lineItem2.setQuantity(2);
    lineItem2.setPricePaid(new BigDecimal("130.00"));
    lineItem2.setPurchaseType(PurchaseType.RETAIL);

    when(collectorRepository.findById(123L)).thenReturn(Optional.of(collectorFound));
    when(collectorPurchaseRepository.findByCollectorIdOrderByOrderDateDescIdDesc(123L))
        .thenReturn(List.of(purchase1, purchase2));
    when(collectorPurchaseFigurineRepository.findByPurchaseIdIn(List.of(500L, 501L)))
        .thenReturn(List.of(lineItem1, lineItem2));

    List<CollectorPurchaseSummaryLineItemResp> response = service.retrieveSummaryLineItems(123L);

    assertThat(response).hasSize(2);
    assertThat(response.getFirst().purchaseId()).isEqualTo(500L);
    assertThat(response.get(0).lineItems()).hasSize(1);
    assertThat(response.get(1).purchaseId()).isEqualTo(501L);
    assertThat(response.get(1).lineItems().getFirst().figurineId()).isEqualTo(102L);
  }

  @Test
  void retrieveSummaryLineItem_shouldThrowCollectorNotFoundException_whenCollectorDoesNotExist() {
    when(collectorRepository.findById(123L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.retrieveSummaryLineItem(123L, 500L))
        .isInstanceOf(CollectorNotFoundException.class)
        .hasMessage("Collector with id 123 was not found");

    verify(collectorRepository).findById(123L);
    verifyNoInteractions(
        collectorPurchaseRepository, collectorPurchaseFigurineRepository, figurineRepository);
  }

  @Test
  void
      retrieveSummaryLineItem_shouldThrowCollectorPurchaseNotFoundException_whenPurchaseDoesNotExist() {
    Collector collectorFound = new Collector();
    collectorFound.setId(123L);
    collectorFound.setEmail("myemail@sample.com");
    collectorFound.setCreationDate(Instant.now());

    when(collectorRepository.findById(123L)).thenReturn(Optional.of(collectorFound));
    when(collectorPurchaseRepository.findByIdAndCollectorId(500L, 123L))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.retrieveSummaryLineItem(123L, 500L))
        .isInstanceOf(
            com.mesofi.mythclothapi.collectorspurchases.exceptions
                .CollectorPurchaseNotFoundException.class)
        .hasMessage("Collector purchase not found for this id: 500");

    verify(collectorRepository).findById(123L);
    verify(collectorPurchaseRepository).findByIdAndCollectorId(500L, 123L);
    verifyNoInteractions(collectorPurchaseFigurineRepository, figurineRepository);
  }

  @Test
  void retrieveSummaryLineItem_shouldReturnSummary_whenPurchaseExists() {
    Collector collectorFound = new Collector();
    collectorFound.setId(123L);
    collectorFound.setEmail("myemail@sample.com");
    collectorFound.setCreationDate(Instant.now());

    CollectorPurchase purchase = getCollectorPurchase(collectorFound);
    purchase.setId(500L);

    Figurine figurine1Found = new Figurine();
    figurine1Found.setId(101L);
    figurine1Found.setNormalizedName("Figurine 1");

    CollectorPurchaseFigurine lineItem1 = new CollectorPurchaseFigurine();
    lineItem1.setId(301L);
    lineItem1.setPurchase(purchase);
    lineItem1.setFigurine(figurine1Found);
    lineItem1.setQuantity(1);
    lineItem1.setPricePaid(new BigDecimal("129.99"));
    lineItem1.setPurchaseType(PurchaseType.PREORDER);

    when(collectorRepository.findById(123L)).thenReturn(Optional.of(collectorFound));
    when(collectorPurchaseRepository.findByIdAndCollectorId(500L, 123L))
        .thenReturn(Optional.of(purchase));
    when(collectorPurchaseFigurineRepository.findByPurchaseIdOrderByIdAsc(500L))
        .thenReturn(List.of(lineItem1));

    CollectorPurchaseSummaryLineItemResp response = service.retrieveSummaryLineItem(123L, 500L);

    assertThat(response.purchaseId()).isEqualTo(500L);
    assertThat(response.lineItems()).hasSize(1);
    assertThat(response.lineItems().getFirst().figurineId()).isEqualTo(101L);
  }

  @Test
  void deleteSummaryLineItem_shouldThrowCollectorNotFoundException_whenCollectorDoesNotExist() {
    when(collectorRepository.findById(123L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.deleteSummaryLineItem(123L, 500L))
        .isInstanceOf(CollectorNotFoundException.class)
        .hasMessage("Collector with id 123 was not found");

    verify(collectorRepository).findById(123L);
    verifyNoInteractions(
        collectorPurchaseRepository, collectorPurchaseFigurineRepository, figurineRepository);
  }

  @Test
  void
      deleteSummaryLineItem_shouldThrowCollectorPurchaseNotFoundException_whenPurchaseDoesNotExist() {
    Collector collectorFound = new Collector();
    collectorFound.setId(123L);
    collectorFound.setEmail("myemail@sample.com");
    collectorFound.setCreationDate(Instant.now());

    when(collectorRepository.findById(123L)).thenReturn(Optional.of(collectorFound));
    when(collectorPurchaseRepository.findByIdAndCollectorId(500L, 123L))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.deleteSummaryLineItem(123L, 500L))
        .isInstanceOf(
            com.mesofi.mythclothapi.collectorspurchases.exceptions
                .CollectorPurchaseNotFoundException.class)
        .hasMessage("Collector purchase not found for this id: 500");

    verify(collectorRepository).findById(123L);
    verify(collectorPurchaseRepository).findByIdAndCollectorId(500L, 123L);
    verifyNoInteractions(collectorPurchaseFigurineRepository, figurineRepository);
  }

  @Test
  void deleteSummaryLineItem_shouldDeleteLineItemsAndPurchase_whenPurchaseExists() {
    Collector collectorFound = new Collector();
    collectorFound.setId(123L);
    collectorFound.setEmail("myemail@sample.com");
    collectorFound.setCreationDate(Instant.now());

    CollectorPurchase purchase = getCollectorPurchase(collectorFound);
    purchase.setId(500L);

    CollectorPurchaseFigurine lineItem1 = new CollectorPurchaseFigurine();
    lineItem1.setId(301L);
    lineItem1.setPurchase(purchase);
    lineItem1.setQuantity(1);

    CollectorPurchaseFigurine lineItem2 = new CollectorPurchaseFigurine();
    lineItem2.setId(302L);
    lineItem2.setPurchase(purchase);
    lineItem2.setQuantity(2);

    when(collectorRepository.findById(123L)).thenReturn(Optional.of(collectorFound));
    when(collectorPurchaseRepository.findByIdAndCollectorId(500L, 123L))
        .thenReturn(Optional.of(purchase));
    when(collectorPurchaseFigurineRepository.findByPurchase(purchase))
        .thenReturn(List.of(lineItem1, lineItem2));

    service.deleteSummaryLineItem(123L, 500L);

    verify(collectorPurchaseFigurineRepository).delete(lineItem1);
    verify(collectorPurchaseFigurineRepository).delete(lineItem2);
    verify(collectorPurchaseRepository).delete(purchase);
    verifyNoInteractions(figurineRepository);
  }

  @Test
  void deleteSummaryLineItem_shouldDeletePurchase_whenNoLineItemsExist() {
    Collector collectorFound = new Collector();
    collectorFound.setId(123L);
    collectorFound.setEmail("myemail@sample.com");
    collectorFound.setCreationDate(Instant.now());

    CollectorPurchase purchase = getCollectorPurchase(collectorFound);
    purchase.setId(500L);

    when(collectorRepository.findById(123L)).thenReturn(Optional.of(collectorFound));
    when(collectorPurchaseRepository.findByIdAndCollectorId(500L, 123L))
        .thenReturn(Optional.of(purchase));
    when(collectorPurchaseFigurineRepository.findByPurchase(purchase)).thenReturn(List.of());

    service.deleteSummaryLineItem(123L, 500L);

    verify(collectorPurchaseFigurineRepository, never()).delete(any());
    verify(collectorPurchaseRepository).delete(purchase);
    verifyNoInteractions(figurineRepository);
  }

  private static @NonNull CollectorPurchase getCollectorPurchase(Collector collectorFound) {
    CollectorPurchase collectorPurchaseSaved = new CollectorPurchase();
    collectorPurchaseSaved.setId(500L);
    collectorPurchaseSaved.setCollector(collectorFound);
    collectorPurchaseSaved.setOrderDate(LocalDate.of(2026, 6, 20));
    collectorPurchaseSaved.setStore("Amiami");
    collectorPurchaseSaved.setOrderNumber("OSMPHKBKI");
    collectorPurchaseSaved.setCurrency(CurrencyCode.JPY);
    collectorPurchaseSaved.setTotalAmount(new BigDecimal("430.00"));
    collectorPurchaseSaved.setTotalFigurines(4);
    collectorPurchaseSaved.setShippingStatus(ShippingStatus.ORDERED);
    collectorPurchaseSaved.setTrackingNumber("881682504940");
    collectorPurchaseSaved.setCarrier("Fedex");
    return collectorPurchaseSaved;
  }
}
