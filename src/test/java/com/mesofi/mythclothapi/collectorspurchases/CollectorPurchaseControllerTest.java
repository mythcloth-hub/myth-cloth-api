package com.mesofi.mythclothapi.collectorspurchases;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseLineItemReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseLineItemResp;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseSummaryLineItemReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseSummaryLineItemResp;
import com.mesofi.mythclothapi.collectorspurchases.exceptions.CollectorPurchaseNotFoundException;
import com.mesofi.mythclothapi.collectorspurchases.model.PurchaseType;
import com.mesofi.mythclothapi.collectorspurchases.model.ShippingStatus;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;
import com.mesofi.mythclothapi.security.config.SecurityConfig;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(CollectorPurchaseController.class)
@Import(SecurityConfig.class)
public class CollectorPurchaseControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private CollectorPurchaseService service;

  @MockitoBean private JwtDecoder jwtDecoder;

  @Test
  void createSummaryLineItem_shouldReturnMethodNotAllowed_whenRequestMethodIsInvalid()
      throws Exception {
    mockMvc
        .perform(
            patch("/purchases/summary-line-items")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("purchases:add"))))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.detail").value("Request method 'PATCH' is not supported"))
        .andExpect(jsonPath("$.instance").value("/purchases/summary-line-items"))
        .andExpect(jsonPath("$.status").value("405"))
        .andExpect(jsonPath("$.title").value("Method Not Allowed"))
        .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(service);
  }

  @ParameterizedTest
  @ValueSource(strings = {"/purchases2/2", "/purchases2/2/figurines"})
  void createSummaryLineItem_shouldReturn404_whenPostingToInvalidEndpoint(String invalidEndpoint)
      throws Exception {
    mockMvc
        .perform(
            post(invalidEndpoint)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("purchases:add"))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("The URL you are calling does not exist."))
        .andExpect(jsonPath("$.instance").value(invalidEndpoint))
        .andExpect(jsonPath("$.status").value("404"))
        .andExpect(jsonPath("$.title").value("Endpoint not found"))
        .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(service);
  }

  @Test
  void createSummaryLineItem_shouldReturnUnauthorized_whenJwtTokenIsMissing() throws Exception {
    mockMvc.perform(post("/purchases/summary-line-items")).andExpect(status().isUnauthorized());
    verifyNoInteractions(service);
  }

  @Test
  void createSummaryLineItem_shouldReturnBadRequest_whenBodyIsMissing() throws Exception {
    mockMvc
        .perform(
            post("/purchases/summary-line-items")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("purchases:add"))))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.detail")
                .value(
                    "Required request body is missing: public org.springframework.http.ResponseEntity<com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseSummaryLineItemResp> com.mesofi.mythclothapi.collectorspurchases.CollectorPurchaseController.createSummaryLineItem(org.springframework.security.oauth2.jwt.Jwt,com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseSummaryLineItemReq)"))
        .andExpect(jsonPath("$.instance").value("/purchases/summary-line-items"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Invalid body"))
        .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(service);
  }

  @Test
  void createSummaryLineItem_shouldReturn415_whenContentTypeIsMissing() throws Exception {
    mockMvc
        .perform(
            post("/purchases/summary-line-items")
                .content("{}")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("purchases:add"))))
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(
            jsonPath("$.detail").value("Content-Type 'application/octet-stream' is not supported"))
        .andExpect(jsonPath("$.instance").value("/purchases/summary-line-items"))
        .andExpect(jsonPath("$.status").value("415"))
        .andExpect(jsonPath("$.title").value("Unsupported Media Type"))
        .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(service);
  }

  @Test
  void createSummaryLineItem_shouldReturn400_whenRequestBodyFailsValidation() throws Exception {
    CollectorPurchaseSummaryLineItemReq request =
        new CollectorPurchaseSummaryLineItemReq(null, null, null, null, null, null, null, null);

    mockMvc
        .perform(
            post("/purchases/summary-line-items")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("purchases:add")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
        .andExpect(jsonPath("$.instance").value("/purchases/summary-line-items"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Validation Failed"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.errors.lineItems").value("lineItems is required"))
        .andExpect(jsonPath("$.errors.shippingStatus").value("shippingStatus is required"))
        .andExpect(jsonPath("$.errors.currency").value("currency is required"));

    verifyNoInteractions(service);
  }

  @Test
  void createSummaryLineItem_shouldReturn400_whenRequiredFieldsAreMissing() throws Exception {
    CollectorPurchaseSummaryLineItemReq request =
        new CollectorPurchaseSummaryLineItemReq(
            LocalDate.of(2100, 1, 1),
            "store".repeat(60),
            "x".repeat(51),
            null,
            null,
            "abc".repeat(50),
            "fedex".repeat(50),
            null);

    mockMvc
        .perform(
            post("/purchases/summary-line-items")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("purchases:add")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
        .andExpect(jsonPath("$.instance").value("/purchases/summary-line-items"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Validation Failed"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.errors.shippingStatus").value("shippingStatus is required"))
        .andExpect(jsonPath("$.errors.lineItems").value("lineItems is required"))
        .andExpect(jsonPath("$.errors.carrier").value("carrier must not exceed 50 characters"))
        .andExpect(
            jsonPath("$.errors.orderNumber").value("orderNumber must not exceed 50 characters"))
        .andExpect(jsonPath("$.errors.currency").value("currency is required"))
        .andExpect(jsonPath("$.errors.store").value("store must not exceed 100 characters"))
        .andExpect(
            jsonPath("$.errors.trackingNumber")
                .value("trackingNumber must not exceed 50 characters"))
        .andExpect(jsonPath("$.errors.orderDate").value("orderDate cannot be in the future"));

    verifyNoInteractions(service);
  }

  @Test
  void createSummaryLineItem_shouldReturn400_whenMandatoryFieldsAreNull() throws Exception {

    List<CollectorPurchaseLineItemReq> lineItems = new ArrayList<>();
    lineItems.add(new CollectorPurchaseLineItemReq(null, null, null, null));

    CollectorPurchaseSummaryLineItemReq request =
        new CollectorPurchaseSummaryLineItemReq(
            LocalDate.of(2026, 1, 1),
            "Amiami",
            "OSMPHKBKI",
            CurrencyCode.JPY,
            ShippingStatus.ORDERED,
            "881682504940",
            "Fedex",
            lineItems);

    mockMvc
        .perform(
            post("/purchases/summary-line-items")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("purchases:add")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
        .andExpect(jsonPath("$.instance").value("/purchases/summary-line-items"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Validation Failed"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.errors['lineItems[0].figurineId']").value("figurineId is required"))
        .andExpect(
            jsonPath("$.errors['lineItems[0].purchaseType']").value("purchaseType is required"))
        .andExpect(jsonPath("$.errors['lineItems[0].quantity']").value("quantity is required"))
        .andExpect(jsonPath("$.errors['lineItems[0].pricePaid']").value("pricePaid is required"));

    verifyNoInteractions(service);
  }

  @Test
  void createSummaryLineItem_shouldReturn400_whenLineItemFieldsAreInvalid() throws Exception {

    List<CollectorPurchaseLineItemReq> lineItems = new ArrayList<>();
    lineItems.add(
        new CollectorPurchaseLineItemReq(-2L, 0, BigDecimal.valueOf(-3.0), PurchaseType.PREORDER));

    CollectorPurchaseSummaryLineItemReq request =
        new CollectorPurchaseSummaryLineItemReq(
            LocalDate.of(2026, 1, 1),
            "Amiami",
            "OSMPHKBKI",
            CurrencyCode.JPY,
            ShippingStatus.ORDERED,
            "881682504940",
            "Fedex",
            lineItems);

    mockMvc
        .perform(
            post("/purchases/summary-line-items")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("purchases:add")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
        .andExpect(jsonPath("$.instance").value("/purchases/summary-line-items"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Validation Failed"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(
            jsonPath("$.errors['lineItems[0].figurineId']").value("figurineId must be positive"))
        .andExpect(jsonPath("$.errors['lineItems[0].quantity']").value("quantity must be positive"))
        .andExpect(
            jsonPath("$.errors['lineItems[0].pricePaid']")
                .value("pricePaid must be greater than 0"));

    verifyNoInteractions(service);
  }

  @Test
  void createSummaryLineItem_shouldReturn201_whenRequestIsValid() throws Exception {

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

    CollectorPurchaseSummaryLineItemResp response =
        new CollectorPurchaseSummaryLineItemResp(
            5001L,
            LocalDate.of(2026, 6, 20),
            "AmiAmi",
            "OSMPHKBKI",
            CurrencyCode.JPY,
            new BigDecimal("259.99"),
            2,
            ShippingStatus.ORDERED,
            "881682504940",
            "Fedex",
            null,
            null,
            List.of(
                new CollectorPurchaseLineItemResp(
                    4001L, 101L, 1, new BigDecimal("129.99"), PurchaseType.PREORDER),
                new CollectorPurchaseLineItemResp(
                    4002L, 102L, 1, new BigDecimal("130.00"), PurchaseType.RETAIL)));

    when(service.createSummaryLineItem(123L, request)).thenReturn(response);

    mockMvc
        .perform(
            post("/purchases/summary-line-items")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("123"))
                        .authorities(new SimpleGrantedAuthority("purchases:add")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.purchaseId").value(5001L))
        .andExpect(jsonPath("$.orderDate").value("2026-06-20"))
        .andExpect(jsonPath("$.store").value("AmiAmi"))
        .andExpect(jsonPath("$.orderNumber").value("OSMPHKBKI"))
        .andExpect(jsonPath("$.currency").value("JPY"))
        .andExpect(jsonPath("$.totalAmount").value("259.99"))
        .andExpect(jsonPath("$.totalFigurines").value("2"))
        .andExpect(jsonPath("$.shippingStatus").value("ORDERED"))
        .andExpect(jsonPath("$.trackingNumber").value("881682504940"))
        .andExpect(jsonPath("$.carrier").value("Fedex"))
        .andExpect(jsonPath("$.lineItems.length()").value(2))
        .andExpect(jsonPath("$.lineItems[0].lineItemId").value(4001L))
        .andExpect(jsonPath("$.lineItems[0].figurineId").value(101L))
        .andExpect(jsonPath("$.lineItems[0].quantity").value(1L))
        .andExpect(jsonPath("$.lineItems[0].pricePaid").value(129.99))
        .andExpect(jsonPath("$.lineItems[0].purchaseType").value("PREORDER"))
        .andExpect(jsonPath("$.lineItems[1].lineItemId").value(4002L))
        .andExpect(jsonPath("$.lineItems[1].figurineId").value(102L))
        .andExpect(jsonPath("$.lineItems[1].quantity").value(1L))
        .andExpect(jsonPath("$.lineItems[1].pricePaid").value(130))
        .andExpect(jsonPath("$.lineItems[1].purchaseType").value("RETAIL"));

    verify(service).createSummaryLineItem(123L, request);
  }

  @Test
  void updateSummaryLineItem_shouldReturn404_whenPurchaseDoesNotExist() throws Exception {
    CollectorPurchaseSummaryLineItemReq request =
        new CollectorPurchaseSummaryLineItemReq(
            LocalDate.of(2026, 6, 21),
            "Mandarake",
            "M99881",
            CurrencyCode.JPY,
            ShippingStatus.SHIPPED,
            "TRACK123",
            "DHL",
            List.of(
                new CollectorPurchaseLineItemReq(
                    102L, 1, new BigDecimal("14800"), PurchaseType.SECOND_HAND),
                new CollectorPurchaseLineItemReq(
                    103L, 2, new BigDecimal("2000"), PurchaseType.RETAIL)));

    when(service.updateSummaryLineItem(1L, 5002L, request))
        .thenThrow(new CollectorPurchaseNotFoundException(5002L));

    mockMvc
        .perform(
            put("/purchases/summary-line-items/{purchaseId}", 5002L)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("1").claim("name", "Armando"))
                        .authorities(new SimpleGrantedAuthority("purchases:update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("Collector purchase not found for this id: 5002"))
        .andExpect(jsonPath("$.instance").value("/purchases/summary-line-items/5002"))
        .andExpect(jsonPath("$.status").value("404"))
        .andExpect(jsonPath("$.title").value("Collector purchase not found for this id: 5002"))
        .andExpect(jsonPath("$.timestamp").exists());

    verify(service).updateSummaryLineItem(1L, 5002L, request);
  }

  @Test
  void updateSummaryLineItem_shouldReturn200_whenRequestIsValid() throws Exception {
    CollectorPurchaseSummaryLineItemReq request =
        new CollectorPurchaseSummaryLineItemReq(
            LocalDate.of(2026, 6, 21),
            "Mandarake",
            "M99881",
            CurrencyCode.JPY,
            ShippingStatus.SHIPPED,
            "TRACK123",
            "DHL",
            List.of(
                new CollectorPurchaseLineItemReq(
                    102L, 1, new BigDecimal("14800"), PurchaseType.SECOND_HAND),
                new CollectorPurchaseLineItemReq(
                    103L, 2, new BigDecimal("2000"), PurchaseType.RETAIL)));

    CollectorPurchaseSummaryLineItemResp response =
        new CollectorPurchaseSummaryLineItemResp(
            5002L,
            LocalDate.of(2026, 6, 21),
            "Mandarake",
            "M99881",
            CurrencyCode.JPY,
            new BigDecimal("18800"),
            3,
            ShippingStatus.SHIPPED,
            "TRACK123",
            "DHL",
            LocalDate.of(2026, 6, 22),
            null,
            List.of(
                new CollectorPurchaseLineItemResp(
                    7002L, 102L, 1, new BigDecimal("14800"), PurchaseType.SECOND_HAND),
                new CollectorPurchaseLineItemResp(
                    7003L, 103L, 2, new BigDecimal("2000"), PurchaseType.RETAIL)));

    when(service.updateSummaryLineItem(eq(1L), eq(5002L), any())).thenReturn(response);

    mockMvc
        .perform(
            put("/purchases/summary-line-items/{purchaseId}", 5002L)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("1").claim("name", "Armando"))
                        .authorities(new SimpleGrantedAuthority("purchases:update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.purchaseId").value(5002L))
        .andExpect(jsonPath("$.store").value("Mandarake"))
        .andExpect(jsonPath("$.lineItems.length()").value(2))
        .andExpect(jsonPath("$.lineItems[0].figurineId").value(102L))
        .andExpect(jsonPath("$.lineItems[1].figurineId").value(103L));

    verify(service).updateSummaryLineItem(eq(1L), eq(5002L), any());
  }

  @Test
  void retrieveSummaryLineItems_shouldReturn200_whenPurchasesExist() throws Exception {
    List<CollectorPurchaseSummaryLineItemResp> response =
        List.of(
            new CollectorPurchaseSummaryLineItemResp(
                5002L,
                LocalDate.of(2026, 6, 21),
                "Mandarake",
                "M99881",
                CurrencyCode.JPY,
                new BigDecimal("18800"),
                3,
                ShippingStatus.SHIPPED,
                "TRACK123",
                "DHL",
                LocalDate.of(2026, 6, 22),
                null,
                List.of(
                    new CollectorPurchaseLineItemResp(
                        7002L, 102L, 1, new BigDecimal("14800"), PurchaseType.SECOND_HAND),
                    new CollectorPurchaseLineItemResp(
                        7003L, 103L, 2, new BigDecimal("2000"), PurchaseType.RETAIL))));

    when(service.retrieveSummaryLineItems(1L)).thenReturn(response);

    mockMvc
        .perform(
            get("/purchases/summary-line-items")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("1").claim("name", "Armando"))
                        .authorities(new SimpleGrantedAuthority("purchases:read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].purchaseId").value(5002L))
        .andExpect(jsonPath("$[0].store").value("Mandarake"))
        .andExpect(jsonPath("$[0].lineItems.length()").value(2))
        .andExpect(jsonPath("$[0].lineItems[0].figurineId").value(102L))
        .andExpect(jsonPath("$[0].lineItems[1].figurineId").value(103L));

    verify(service).retrieveSummaryLineItems(1L);
  }

  @Test
  void retrieveSummaryLineItem_shouldReturn200_whenPurchaseExists() throws Exception {
    CollectorPurchaseSummaryLineItemResp response =
        new CollectorPurchaseSummaryLineItemResp(
            5002L,
            LocalDate.of(2026, 6, 21),
            "Mandarake",
            "M99881",
            CurrencyCode.JPY,
            new BigDecimal("18800"),
            3,
            ShippingStatus.SHIPPED,
            "TRACK123",
            "DHL",
            LocalDate.of(2026, 6, 22),
            null,
            List.of(
                new CollectorPurchaseLineItemResp(
                    7002L, 102L, 1, new BigDecimal("14800"), PurchaseType.SECOND_HAND),
                new CollectorPurchaseLineItemResp(
                    7003L, 103L, 2, new BigDecimal("2000"), PurchaseType.RETAIL)));

    when(service.retrieveSummaryLineItem(1L, 5002L)).thenReturn(response);

    mockMvc
        .perform(
            get("/purchases/summary-line-items/{purchaseId}", 5002L)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("1").claim("name", "Armando"))
                        .authorities(new SimpleGrantedAuthority("purchases:read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.purchaseId").value(5002L))
        .andExpect(jsonPath("$.store").value("Mandarake"))
        .andExpect(jsonPath("$.lineItems.length()").value(2))
        .andExpect(jsonPath("$.lineItems[0].figurineId").value(102L))
        .andExpect(jsonPath("$.lineItems[1].figurineId").value(103L));

    verify(service).retrieveSummaryLineItem(1L, 5002L);
  }

  @Test
  void deleteSummaryLineItem_shouldReturn204_whenRequestIsValid() throws Exception {
    doNothing().when(service).deleteSummaryLineItem(eq(1L), eq(5002L));

    mockMvc
        .perform(
            delete("/purchases/summary-line-items/{purchaseId}", 5002L)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject("1").claim("name", "Armando"))
                        .authorities(new SimpleGrantedAuthority("purchases:delete"))))
        .andExpect(status().isNoContent());

    verify(service).deleteSummaryLineItem(eq(1L), eq(5002L));
  }
}
