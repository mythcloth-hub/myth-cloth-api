package com.mesofi.mythclothapi.collectorspurchases;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.mesofi.mythclothapi.collectorproviders.model.ProviderType;
import com.mesofi.mythclothapi.collectors.dto.CollectorLoginReq;
import com.mesofi.mythclothapi.collectors.dto.CollectorLoginResp;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseResp;
import com.mesofi.mythclothapi.collectorspurchases.model.PurchaseChannel;
import com.mesofi.mythclothapi.collectorspurchases.model.ShippingStatus;
import com.mesofi.mythclothapi.security.service.SecurityDataService;
import com.mesofi.mythclothapi.support.ControllerBaseIT;

@AutoConfigureMockMvc
@Sql(scripts = "/cleanup-purchases-it.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CollectorPurchaseControllerIT extends ControllerBaseIT {

    private static final Logger log = LoggerFactory.getLogger(CollectorPurchaseControllerIT.class);

    private static final String AUTH_PROVIDER = "/collectors/auth/{provider}";
    private static final String PURCHASES = "/purchases";

    private static final WireMockServer GOOGLE_API = startGoogleApi();

    @Autowired
    private SecurityDataService securityDataService;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("myth-cloth.google.oauth-url", GOOGLE_API::baseUrl);
    }

    @BeforeEach
    void setUp() {
        securityDataService.initializeSecurityData();
        GOOGLE_API.resetAll();
        GOOGLE_API.stubFor(get(urlPathEqualTo("/tokeninfo")).withQueryParam("id_token", equalTo("dummy-id-token"))
                .willReturn(okJson("""
                        {
                            "iss": "https://accounts.google.com",
                            "aud": "90909090",
                            "sub": "google-user-123",
                            "email": "my-user@gmail.com",
                            "email_verified": "true",
                            "name": "Google Collector",
                            "picture": "https://example.com/google-user-123.png",
                            "exp": "4102444800"
                        }
                        """)));
    }

    @AfterAll
    static void stopWireMock() {
        GOOGLE_API.stop();
    }

    @Test
    @DisplayName("Register new purchase for both channels")
    void registerNewPurchaseForBothChannels() {

        // 1. The user authenticates using the Google provider and obtains a token
        CollectorLoginResp loginResp = authenticateCollectorWithGoogle();

        CollectorPurchaseResp onlinePurchaseResp = registerOnlinePurchase(loginResp.accessToken());
        log.info("New online purchase registered: {}", onlinePurchaseResp);

        CollectorPurchaseResp inStorePurchaseResp = registerInStorePurchase(loginResp.accessToken());
        log.info("New in-store purchase registered: {}", inStorePurchaseResp);
    }

    private CollectorLoginResp authenticateCollectorWithGoogle() {
        CollectorLoginReq request = new CollectorLoginReq("dummy-id-token", null, null, null);

        ResponseEntity<CollectorLoginResp> response = rest.post().uri(AUTH_PROVIDER, ProviderType.GOOGLE).body(request)
                .retrieve().toEntity(CollectorLoginResp.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(OK);
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().collectorId()).isPositive();
        Assertions.assertThat(response.getBody().displayName()).isEqualTo("Google Collector");
        Assertions.assertThat(response.getBody().email()).isEqualTo("my-user@gmail.com");
        Assertions.assertThat(response.getBody().role()).isEqualTo("Collector");
        Assertions.assertThat(response.getBody().accessToken()).isNotBlank();
        Assertions.assertThat(response.getBody().tokenType()).isEqualTo("Bearer");
        Assertions.assertThat(response.getBody().expiresInSeconds()).isPositive();

        return response.getBody();
    }

    private CollectorPurchaseResp registerInStorePurchase(String jwtToken) {
        CollectorPurchaseReq request = new CollectorPurchaseReq(LocalDate.now(), "RockShow", null,
                Currency.getInstance("MXN"), PurchaseChannel.PHYSICAL_STORE, null, null, null);

        CollectorPurchaseResp body = sendRequestAndGetResponse(jwtToken, request);
        assertThat(body.purchaseId()).isNotNull();
        assertThat(body.purchaseId()).isPositive();
        assertThat(body.seller()).isEqualTo("RockShow");
        assertThat(body.orderNumber()).isNull();
        assertThat(body.currency()).isEqualTo("MXN");
        assertThat(body.totalAmount()).isEqualTo(new BigDecimal("1"));
        assertThat(body.purchaseChannel()).isEqualTo(PurchaseChannel.PHYSICAL_STORE);
        assertThat(body.shippingStatus()).isNull();
        assertThat(body.trackingNumber()).isNull();
        assertThat(body.carrier()).isNull();
        assertThat(body.deliveredDate()).isNull();
        assertThat(body.shippedDate()).isNull();

        return body;
    }

    private CollectorPurchaseResp registerOnlinePurchase(final String jwtToken) {
        CollectorPurchaseReq request = new CollectorPurchaseReq(LocalDate.now(), "Mandarake", "XQKUHSCWV",
                Currency.getInstance("JPY"), PurchaseChannel.ONLINE, ShippingStatus.DELIVERED, "1ZV912320456954189",
                "FEDEX");

        CollectorPurchaseResp body = sendRequestAndGetResponse(jwtToken, request);
        assertThat(body.purchaseId()).isNotNull();
        assertThat(body.purchaseId()).isPositive();
        assertThat(body.seller()).isEqualTo("Mandarake");
        assertThat(body.orderNumber()).isEqualTo("XQKUHSCWV");
        assertThat(body.currency()).isEqualTo("JPY");
        assertThat(body.totalAmount()).isEqualTo(new BigDecimal("1"));
        assertThat(body.purchaseChannel()).isEqualTo(PurchaseChannel.ONLINE);
        assertThat(body.shippingStatus()).isEqualTo(ShippingStatus.DELIVERED);
        assertThat(body.trackingNumber()).isEqualTo("1ZV912320456954189");
        assertThat(body.carrier()).isEqualTo("FEDEX");
        assertThat(body.deliveredDate()).isNotNull();
        assertThat(body.shippedDate()).isNull();

        return body;
    }

    private CollectorPurchaseResp sendRequestAndGetResponse(final String jwtToken, final CollectorPurchaseReq request) {
        // The jwtToken is used to authenticate the request, and the request body
        // contains the purchase details.
        ResponseEntity<CollectorPurchaseResp> response = rest.post().uri(PURCHASES)
                .header("Authorization", "Bearer " + jwtToken).body(request).retrieve()
                .toEntity(CollectorPurchaseResp.class);

        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        CollectorPurchaseResp body = Objects.requireNonNull(response.getBody(),
                "Purchase response body should not be null");

        assertThat(body).isNotNull();

        return body;
    }

    private static WireMockServer startGoogleApi() {
        WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        return wireMockServer;
    }
}
