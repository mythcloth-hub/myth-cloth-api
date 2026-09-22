package com.mesofi.mythclothapi.collectorspurchases;

import static org.hamcrest.Matchers.containsString;
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
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mesofi.mythclothapi.collectors.exceptions.CollectorNotFoundException;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseFigurineReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseResp;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseSummaryResp;
import com.mesofi.mythclothapi.collectorspurchases.dto.PurchaseSummaryResp;
import com.mesofi.mythclothapi.collectorspurchases.dto.ShippingStatusReq;
import com.mesofi.mythclothapi.collectorspurchases.exceptions.CollectorPurchaseFigurineNotFoundException;
import com.mesofi.mythclothapi.collectorspurchases.exceptions.CollectorPurchaseInvalidShippingStatusException;
import com.mesofi.mythclothapi.collectorspurchases.exceptions.CollectorPurchaseNotFoundException;
import com.mesofi.mythclothapi.collectorspurchases.model.PurchaseChannel;
import com.mesofi.mythclothapi.collectorspurchases.model.PurchaseType;
import com.mesofi.mythclothapi.collectorspurchases.model.ShippingStatus;
import com.mesofi.mythclothapi.security.config.SecurityConfig;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(value = CollectorPurchaseController.class, properties = {
        "myth-cloth.security.cors-url=http://localhost:5173",
        "myth-cloth.security.jwt.secret=test-secret-test-secret-test-secret-1234",
        "myth-cloth.security.jwt.issuer=myth-cloth-api", "myth-cloth.security.jwt.ttl-minutes=60"})
@Import(SecurityConfig.class)
public class CollectorPurchaseControllerTest {

    private static final long COLLECTION_ID = 77L;
    private static final String PURCHASES = "/collectors/purchases";
    private static final String PURCHASES_CREATION = PURCHASES + "/collections/" + COLLECTION_ID;
    private static final String PURCHASES_RETRIEVAL_BY_ID = PURCHASES + "/{purchaseId}";
    private static final String PURCHASES_UPDATE_BY_ID = PURCHASES + "/{purchaseId}";
    private static final String PURCHASES_DELETION_BY_ID = PURCHASES + "/{purchaseId}";
    private static final String PURCHASES_PARTIAL_UPDATE_BY_ID = PURCHASES_UPDATE_BY_ID + "/shipping-status";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CollectorPurchaseService collectorPurchaseService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createPurchase_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post(PURCHASES_CREATION, COLLECTION_ID)).andExpect(status().isUnauthorized());

        verifyNoInteractions(collectorPurchaseService);
    }

    @Test
    void createPurchase_shouldReturnBadRequest_whenRequestBodyIsMissing() throws Exception {
        mockMvc.perform(post(PURCHASES_CREATION)
                .with(jwt().jwt(jwt -> jwt.subject("123")).authorities(new SimpleGrantedAuthority("purchases:create"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("Required request body is missing")))
                .andExpect(jsonPath("$.instance").value(PURCHASES_CREATION))
                .andExpect(jsonPath("$.status").value("400")).andExpect(jsonPath("$.title").value("Invalid body"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(collectorPurchaseService);
    }

    @Test
    void createPurchase_shouldReturnUnsupportedMediaType_whenContentTypeIsMissing() throws Exception {

        mockMvc.perform(post(PURCHASES_CREATION)
                .with(jwt().jwt(jwt -> jwt.subject("123")).authorities(new SimpleGrantedAuthority("purchases:create")))
                .content("{}")).andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.detail").value("Content-Type 'application/octet-stream' is not supported"))
                .andExpect(jsonPath("$.instance").value(PURCHASES_CREATION))
                .andExpect(jsonPath("$.status").value("415"))
                .andExpect(jsonPath("$.title").value("Unsupported Media Type"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(collectorPurchaseService);
    }

    @Test
    void createPurchase_shouldReturnBadRequest_whenRequestContainsInvalidRequiredFields() throws Exception {
        CollectorPurchaseReq request = new CollectorPurchaseReq(null, null, null, null, null, null, null, null, null);

        mockMvc.perform(post(PURCHASES_CREATION)
                .with(jwt().jwt(jwt -> jwt.subject("123")).authorities(new SimpleGrantedAuthority("purchases:create")))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
                .andExpect(jsonPath("$.instance").value(PURCHASES_CREATION))
                .andExpect(jsonPath("$.status").value("400")).andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors.seller").value("must not be null"))
                .andExpect(jsonPath("$.errors.purchaseDate").value("must not be null"))
                .andExpect(jsonPath("$.errors.figurines").value("must not be empty"))
                .andExpect(jsonPath("$.errors.currency").value("must not be null"))
                .andExpect(jsonPath("$.errors.purchaseChannel").value("must not be null"));

        verifyNoInteractions(collectorPurchaseService);
    }

    @Test
    void createPurchase_shouldReturnBadRequest_whenSellerIsValidButOtherRequiredFieldsAreMissing() throws Exception {
        CollectorPurchaseReq request = new CollectorPurchaseReq(null, "yoyaKuNow", null, null, null, null, null, null,
                null);

        mockMvc.perform(post(PURCHASES_CREATION)
                .with(jwt().jwt(jwt -> jwt.subject("123")).authorities(new SimpleGrantedAuthority("purchases:create")))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
                .andExpect(jsonPath("$.instance").value(PURCHASES_CREATION))
                .andExpect(jsonPath("$.status").value("400")).andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors.purchaseDate").value("must not be null"))
                .andExpect(jsonPath("$.errors.figurines").value("must not be empty"))
                .andExpect(jsonPath("$.errors.currency").value("must not be null"))
                .andExpect(jsonPath("$.errors.purchaseChannel").value("must not be null"));

        verifyNoInteractions(collectorPurchaseService);
    }

    @Test
    void createPurchase_shouldReturnBadRequest_whenPurchaseDateIsInTheFuture() throws Exception {
        CollectorPurchaseReq request = new CollectorPurchaseReq(LocalDate.of(2099, 1, 1), "yoyaKuNow", null, null, null,
                null, null, null, null);

        mockMvc.perform(post(PURCHASES_CREATION)
                .with(jwt().jwt(jwt -> jwt.subject("123")).authorities(new SimpleGrantedAuthority("purchases:create")))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
                .andExpect(jsonPath("$.instance").value(PURCHASES_CREATION))
                .andExpect(jsonPath("$.status").value("400")).andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors.purchaseDate").value("must be a date in the past or in the present"))
                .andExpect(jsonPath("$.errors.figurines").value("must not be empty"))
                .andExpect(jsonPath("$.errors.currency").value("must not be null"))
                .andExpect(jsonPath("$.errors.purchaseChannel").value("must not be null"));

        verifyNoInteractions(collectorPurchaseService);
    }

    @Test
    void createPurchase_shouldReturnBadRequest_whenSellerAndPurchaseDateAreValidButOtherRequiredFieldsAreMissing()
            throws Exception {
        CollectorPurchaseReq request = new CollectorPurchaseReq(LocalDate.of(2026, 1, 1), "yoyaKuNow", null, null, null,
                null, null, null, null);

        mockMvc.perform(post(PURCHASES_CREATION)
                .with(jwt().jwt(jwt -> jwt.subject("123")).authorities(new SimpleGrantedAuthority("purchases:create")))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
                .andExpect(jsonPath("$.instance").value(PURCHASES_CREATION))
                .andExpect(jsonPath("$.status").value("400")).andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors.figurines").value("must not be empty"))
                .andExpect(jsonPath("$.errors.currency").value("must not be null"))
                .andExpect(jsonPath("$.errors.purchaseChannel").value("must not be null"));

        verifyNoInteractions(collectorPurchaseService);
    }

    @Test
    void createPurchase_shouldReturnBadRequest_whenCurrencySellerAndPurchaseDateAreValidButOtherRequiredFieldsAreMissing()
            throws Exception {
        CollectorPurchaseReq request = new CollectorPurchaseReq(LocalDate.of(2026, 1, 1), "yoyaKuNow", null,
                Currency.getInstance("JPY"), null, null, null, null, null);

        mockMvc.perform(post(PURCHASES_CREATION)
                .with(jwt().jwt(jwt -> jwt.subject("123")).authorities(new SimpleGrantedAuthority("purchases:create")))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
                .andExpect(jsonPath("$.instance").value(PURCHASES_CREATION))
                .andExpect(jsonPath("$.status").value("400")).andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors.figurines").value("must not be empty"))
                .andExpect(jsonPath("$.errors.purchaseChannel").value("must not be null"));

        verifyNoInteractions(collectorPurchaseService);
    }

    @Test
    void createPurchase_shouldReturnBadRequest_whenFigurinesAreMissing() throws Exception {
        CollectorPurchaseReq request = new CollectorPurchaseReq(LocalDate.of(2026, 1, 1), "yoyaKuNow", null,
                Currency.getInstance("JPY"), PurchaseChannel.ONLINE, null, null, null, null);

        mockMvc.perform(post(PURCHASES_CREATION)
                .with(jwt().jwt(jwt -> jwt.subject("123")).authorities(new SimpleGrantedAuthority("purchases:create")))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
                .andExpect(jsonPath("$.instance").value(PURCHASES_CREATION))
                .andExpect(jsonPath("$.status").value("400")).andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors.figurines").value("must not be empty"));

        verifyNoInteractions(collectorPurchaseService);
    }

    @Test
    void createPurchase_shouldReturnNotFound_whenCollectorDoesNotExist() throws Exception {
        CollectorPurchaseReq request = new CollectorPurchaseReq(LocalDate.of(2026, 1, 1), "yoyaKuNow", "FZCAQSZTC",
                Currency.getInstance("JPY"), PurchaseChannel.ONLINE, ShippingStatus.SHIPPED, "884469419291", "FedEX",
                List.of(new CollectorPurchaseFigurineReq(33L, 2, new BigDecimal("19000"), PurchaseType.PREORDER),
                        new CollectorPurchaseFigurineReq(55L, 1, new BigDecimal("24000"), PurchaseType.PREORDER)));

        when(collectorPurchaseService.createPurchase(0L, COLLECTION_ID, request))
                .thenThrow(new CollectorNotFoundException(0L));

        // subject is sent with empty value.
        mockMvc.perform(post(PURCHASES_CREATION)
                .with(jwt().jwt(jwt -> jwt.subject("")).authorities(new SimpleGrantedAuthority("purchases:create")))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Collector with id 0 was not found"))
                .andExpect(jsonPath("$.instance").value(PURCHASES_CREATION))
                .andExpect(jsonPath("$.status").value("404"))
                .andExpect(jsonPath("$.title").value("Collector not found")).andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errorCode").value("COLLECTOR_NOT_FOUND"));

        verify(collectorPurchaseService).createPurchase(0L, COLLECTION_ID, request);
    }

    @Test
    void createPurchase_shouldReturnNotFound_whenCollectorPurchaseFigurinesAreNotFound() throws Exception {
        CollectorPurchaseReq request = new CollectorPurchaseReq(LocalDate.of(2026, 1, 1), "yoyaKuNow", "FZCAQSZTC",
                Currency.getInstance("JPY"), PurchaseChannel.ONLINE, ShippingStatus.SHIPPED, "884469419291", "FedEX",
                List.of(new CollectorPurchaseFigurineReq(33L, 2, new BigDecimal("19000"), PurchaseType.PREORDER),
                        new CollectorPurchaseFigurineReq(55L, 1, new BigDecimal("24000"), PurchaseType.PREORDER)));

        when(collectorPurchaseService.createPurchase(123L, COLLECTION_ID, request))
                .thenThrow(new CollectorPurchaseFigurineNotFoundException(List.of(33L, 55L)));

        mockMvc.perform(post(PURCHASES_CREATION, COLLECTION_ID)
                .with(jwt().jwt(jwt -> jwt.subject("123")).authorities(new SimpleGrantedAuthority("purchases:create")))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Collector purchase figurines with IDs [33, 55] were not found"))
                .andExpect(jsonPath("$.instance").value(PURCHASES_CREATION))
                .andExpect(jsonPath("$.status").value("404"))
                .andExpect(jsonPath("$.title").value("Collector purchase figurines not found"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errorCode").value("COLLECTOR_PURCHASE_FIGURINE_NOT_FOUND"));

        verify(collectorPurchaseService).createPurchase(123L, COLLECTION_ID, request);
    }

    @Test
    void createPurchase_shouldReturnCreated_whenRequestIsValid() throws Exception {
        Long collectorId = 123L;

        CollectorPurchaseReq request = new CollectorPurchaseReq(LocalDate.of(2026, 1, 1), "yoyaKuNow", "FZCAQSZTC",
                Currency.getInstance("JPY"), PurchaseChannel.ONLINE, ShippingStatus.SHIPPED, "884469419291", "FedEX",
                List.of(new CollectorPurchaseFigurineReq(33L, 2, new BigDecimal("19000"), PurchaseType.PREORDER),
                        new CollectorPurchaseFigurineReq(55L, 1, new BigDecimal("24000"), PurchaseType.PREORDER)));

        CollectorPurchaseResp response = new CollectorPurchaseResp(999L, LocalDate.of(2026, 1, 1), "yoyaKuNow",
                "FZCAQSZTC", "JPY", new BigDecimal("62000"), PurchaseChannel.ONLINE, ShippingStatus.SHIPPED,
                "884469419291", "FedEX", null, LocalDate.now(), null, List.of());

        when(collectorPurchaseService.createPurchase(collectorId, COLLECTION_ID, request)).thenReturn(response);

        mockMvc.perform(post(PURCHASES_CREATION, COLLECTION_ID)
                .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(collectorId)))
                        .authorities(new SimpleGrantedAuthority("purchases:create")))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.purchaseId").value(999L))
                .andExpect(jsonPath("$.seller").value("yoyaKuNow"))
                .andExpect(jsonPath("$.purchaseDate").value("2026-01-01"))
                .andExpect(jsonPath("$.orderNumber").value("FZCAQSZTC")).andExpect(jsonPath("$.currency").value("JPY"))
                .andExpect(jsonPath("$.totalAmount").value("62000"))
                .andExpect(jsonPath("$.purchaseChannel").value("ONLINE"))
                .andExpect(jsonPath("$.shippingStatus").value("SHIPPED"))
                .andExpect(jsonPath("$.trackingNumber").value("884469419291"))
                .andExpect(jsonPath("$.carrier").value("FedEX")).andExpect(jsonPath("$.shippedDate").exists())
                .andExpect(jsonPath("$.deliveredDate").doesNotExist());

        verify(collectorPurchaseService).createPurchase(collectorId, COLLECTION_ID, request);
    }

    @Test
    void retrievePurchases_shouldReturnPurchases() throws Exception {
        Long collectorId = 123L;

        PurchaseSummaryResp summary = new PurchaseSummaryResp("JPY", new BigDecimal("62000"));

        List<CollectorPurchaseResp> purchases = List.of(new CollectorPurchaseResp(999L, LocalDate.of(2026, 1, 1),
                "yoyaKuNow", "FZCAQSZTC", "JPY", new BigDecimal("62000"), PurchaseChannel.ONLINE,
                ShippingStatus.SHIPPED, "884469419291", "FedEX", null, LocalDate.now(), null, List.of()));

        CollectorPurchaseSummaryResp response = new CollectorPurchaseSummaryResp(summary, purchases);

        when(collectorPurchaseService.retrievePurchases(collectorId)).thenReturn(response);

        mockMvc.perform(get(PURCHASES)
                .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(collectorId)))
                        .authorities(new SimpleGrantedAuthority("purchases:read")))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.currency").value("JPY"))
                .andExpect(jsonPath("$.summary.totalAmount").value("62000"))
                .andExpect(jsonPath("$.purchases[0].purchaseId").value(999L))
                .andExpect(jsonPath("$.purchases[0].purchaseDate").value("2026-01-01"))
                .andExpect(jsonPath("$.purchases[0].seller").value("yoyaKuNow"))
                .andExpect(jsonPath("$.purchases[0].orderNumber").value("FZCAQSZTC"))
                .andExpect(jsonPath("$.purchases[0].currency").value("JPY"))
                .andExpect(jsonPath("$.purchases[0].totalAmount").value("62000"))
                .andExpect(jsonPath("$.purchases[0].purchaseChannel").value("ONLINE"))
                .andExpect(jsonPath("$.purchases[0].shippingStatus").value("SHIPPED"))
                .andExpect(jsonPath("$.purchases[0].trackingNumber").value("884469419291"))
                .andExpect(jsonPath("$.purchases[0].carrier").value("FedEX"))
                .andExpect(jsonPath("$.purchases[0].shippedDate").exists())
                .andExpect(jsonPath("$.purchases[0].deliveredDate").doesNotExist());

        verify(collectorPurchaseService).retrievePurchases(collectorId);
    }

    @Test
    void retrievePurchases_shouldReturnNotFound_whenPurchaseDoesNotExist() throws Exception {
        Long collectorId = 123L;
        Long purchaseId = 0L;

        when(collectorPurchaseService.retrievePurchase(collectorId, purchaseId))
                .thenThrow(new CollectorPurchaseNotFoundException(purchaseId));

        // subject is sent with empty value.
        mockMvc.perform(get(PURCHASES_RETRIEVAL_BY_ID, purchaseId)
                .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(collectorId)))
                        .authorities(new SimpleGrantedAuthority("purchases:read")))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Collector purchase with id 0 was not found"))
                .andExpect(jsonPath("$.instance").value("/collectors/purchases/0"))
                .andExpect(jsonPath("$.status").value("404"))
                .andExpect(jsonPath("$.title").value("Collector purchase not found"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errorCode").value("COLLECTOR_PURCHASE_NOT_FOUND"));

        verify(collectorPurchaseService).retrievePurchase(collectorId, purchaseId);
    }

    @Test
    void retrievePurchases_shouldReturnPurchase_whenPurchaseExists() throws Exception {
        Long collectorId = 123L;
        Long purchaseId = 999L;

        CollectorPurchaseResp response = new CollectorPurchaseResp(999L, LocalDate.of(2026, 1, 1), "yoyaKuNow",
                "FZCAQSZTC", "JPY", new BigDecimal("62000"), PurchaseChannel.ONLINE, ShippingStatus.SHIPPED,
                "884469419291", "FedEX", null, LocalDate.now(), null, List.of());

        when(collectorPurchaseService.retrievePurchase(collectorId, purchaseId)).thenReturn(response);

        mockMvc.perform(get(PURCHASES_RETRIEVAL_BY_ID, purchaseId)
                .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(collectorId)))
                        .authorities(new SimpleGrantedAuthority("purchases:read")))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$.purchaseId").value(999L))
                .andExpect(jsonPath("$.purchaseDate").value("2026-01-01"))
                .andExpect(jsonPath("$.seller").value("yoyaKuNow"))
                .andExpect(jsonPath("$.orderNumber").value("FZCAQSZTC")).andExpect(jsonPath("$.currency").value("JPY"))
                .andExpect(jsonPath("$.totalAmount").value("62000"))
                .andExpect(jsonPath("$.purchaseChannel").value("ONLINE"))
                .andExpect(jsonPath("$.shippingStatus").value("SHIPPED"))
                .andExpect(jsonPath("$.trackingNumber").value("884469419291"))
                .andExpect(jsonPath("$.carrier").value("FedEX")).andExpect(jsonPath("$.shippedDate").exists())
                .andExpect(jsonPath("$.deliveredDate").doesNotExist());

        verify(collectorPurchaseService).retrievePurchase(collectorId, purchaseId);
    }

    @Test
    void updatePurchase_shouldReturnOK_whenRequestIsValid() throws Exception {
        Long collectorId = 123L;
        Long purchaseId = 321L;

        CollectorPurchaseReq request = new CollectorPurchaseReq(LocalDate.of(2026, 1, 1), "yoyaKuNow", "FZCAQSZTC",
                Currency.getInstance("JPY"), PurchaseChannel.ONLINE, ShippingStatus.SHIPPED, "884469419291", "FedEX",
                List.of(new CollectorPurchaseFigurineReq(33L, 2, new BigDecimal("19000"), PurchaseType.PREORDER),
                        new CollectorPurchaseFigurineReq(55L, 1, new BigDecimal("24000"), PurchaseType.PREORDER)));

        CollectorPurchaseResp response = new CollectorPurchaseResp(999L, LocalDate.of(2026, 1, 1), "yoyaKuNow",
                "FZCAQSZTC", "JPY", new BigDecimal("62000"), PurchaseChannel.ONLINE, ShippingStatus.SHIPPED,
                "884469419291", "FedEX", null, LocalDate.now(), null, List.of());

        when(collectorPurchaseService.updatePurchase(collectorId, purchaseId, request)).thenReturn(response);

        mockMvc.perform(put(PURCHASES_UPDATE_BY_ID, purchaseId)
                .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(collectorId)))
                        .authorities(new SimpleGrantedAuthority("purchases:update")))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.purchaseId").value(999L))
                .andExpect(jsonPath("$.seller").value("yoyaKuNow"))
                .andExpect(jsonPath("$.purchaseDate").value("2026-01-01"))
                .andExpect(jsonPath("$.orderNumber").value("FZCAQSZTC")).andExpect(jsonPath("$.currency").value("JPY"))
                .andExpect(jsonPath("$.totalAmount").value("62000"))
                .andExpect(jsonPath("$.purchaseChannel").value("ONLINE"))
                .andExpect(jsonPath("$.shippingStatus").value("SHIPPED"))
                .andExpect(jsonPath("$.trackingNumber").value("884469419291"))
                .andExpect(jsonPath("$.carrier").value("FedEX")).andExpect(jsonPath("$.shippedDate").exists())
                .andExpect(jsonPath("$.deliveredDate").doesNotExist());

        verify(collectorPurchaseService).updatePurchase(collectorId, purchaseId, request);
    }

    @Test
    void updatePurchaseStatus_shouldReturnBadRequest_whenInvalidShippingStatus() throws Exception {
        Long collectorId = 123L;
        Long purchaseId = 321L;

        ShippingStatusReq request = new ShippingStatusReq(ShippingStatus.SHIPPED);

        when(collectorPurchaseService.updatePurchaseShippingStatus(collectorId, purchaseId, ShippingStatus.SHIPPED))
                .thenThrow(new CollectorPurchaseInvalidShippingStatusException());

        mockMvc.perform(patch(PURCHASES_PARTIAL_UPDATE_BY_ID, purchaseId)
                .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(collectorId)))
                        .authorities(new SimpleGrantedAuthority("purchases:update")))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Collector purchase has an invalid shipping status"))
                .andExpect(jsonPath("$.instance").value("/collectors/purchases/321/shipping-status"))
                .andExpect(jsonPath("$.status").value("400"))
                .andExpect(jsonPath("$.title").value("Collector purchase has an invalid shipping status"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errorCode").value("COLLECTOR_PURCHASE_INVALID_SHIPPING_STATUS"));
    }

    @Test
    void updatePurchaseStatus_shouldReturnOk_whenValidShippingStatus() throws Exception {
        Long collectorId = 123L;
        Long purchaseId = 321L;

        ShippingStatusReq request = new ShippingStatusReq(ShippingStatus.SHIPPED);

        when(collectorPurchaseService.updatePurchaseShippingStatus(collectorId, purchaseId, ShippingStatus.SHIPPED))
                .thenReturn(new CollectorPurchaseResp(999L, LocalDate.of(2026, 1, 1), "yoyaKuNow", "FZCAQSZTC", "JPY",
                        new BigDecimal("62000"), PurchaseChannel.ONLINE, ShippingStatus.SHIPPED, "884469419291",
                        "FedEX", null, LocalDate.now(), null, List.of()));

        mockMvc.perform(patch(PURCHASES_PARTIAL_UPDATE_BY_ID, purchaseId)
                .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(collectorId)))
                        .authorities(new SimpleGrantedAuthority("purchases:update")))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.purchaseId").value(999L))
                .andExpect(jsonPath("$.purchaseDate").value("2026-01-01"))
                .andExpect(jsonPath("$.seller").value("yoyaKuNow"))
                .andExpect(jsonPath("$.orderNumber").value("FZCAQSZTC")).andExpect(jsonPath("$.currency").value("JPY"))
                .andExpect(jsonPath("$.totalAmount").value("62000"))
                .andExpect(jsonPath("$.purchaseChannel").value("ONLINE"))
                .andExpect(jsonPath("$.shippingStatus").value("SHIPPED"))
                .andExpect(jsonPath("$.trackingNumber").value("884469419291"))
                .andExpect(jsonPath("$.carrier").value("FedEX")).andExpect(jsonPath("$.shippedDate").exists())
                .andExpect(jsonPath("$.deliveredDate").doesNotExist());
    }

    @Test
    void deletePurchase_shouldReturnNoContent_whenSuccessful() throws Exception {
        Long collectorId = 123L;
        Long purchaseId = 321L;

        doNothing().when(collectorPurchaseService).deletePurchase(collectorId, purchaseId);

        mockMvc.perform(delete(PURCHASES_DELETION_BY_ID, purchaseId)
                .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(collectorId)))
                        .authorities(new SimpleGrantedAuthority("purchases:delete")))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent());

        verify(collectorPurchaseService).deletePurchase(collectorId, purchaseId);
    }
}
