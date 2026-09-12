package com.mesofi.mythclothapi.collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import com.mesofi.mythclothapi.collectors.dto.CollectorSignupReq;
import com.mesofi.mythclothapi.collectors.dto.CollectorSignupResp;
import com.mesofi.mythclothapi.demo.DemoProperties;
import com.mesofi.mythclothapi.support.ControllerBaseIT;

@AutoConfigureMockMvc
@Sql(scripts = "/cleanup-collector-it.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CollectorControllerIT extends ControllerBaseIT {

    private static final String AUTH_PROVIDER = "/collectors/auth/{provider}";
    private static final String SIGNUP = "/collectors/signup";
    private static final WireMockServer FACEBOOK_API = startFacebookApi();

    @Autowired
    private DemoProperties demoProperties;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("myth-cloth.facebook.graph-url", FACEBOOK_API::baseUrl);
    }

    @BeforeEach
    void setUp() {
        FACEBOOK_API.resetAll();
        FACEBOOK_API.stubFor(get(urlPathEqualTo("/debug_token")).willReturn(okJson("""
                {
                    "data": {
                        "app_id": "0000",
                        "type": "USER",
                        "application": "MythCollection-Integration-Test",
                        "data_access_expires_at": 4102444800,
                        "expires_at": 4102444800,
                        "is_valid": true,
                        "scopes": ["email", "public_profile"],
                        "user_id": "fb-user-123"
                    }
                }
                """)));
        FACEBOOK_API.stubFor(get(urlPathEqualTo("/me")).willReturn(okJson("""
                {
                    "id": "fb-user-123",
                    "name": "Facebook Collector",
                    "email": "demo.integration@saintcollections.com"
                }
                """)));
    }

    @Test
    @DisplayName("Login with Facebook provider")
    void loginWithUsingFacebookProvider() {
        CollectorLoginResp userLoginResp = loginWithProvider(ProviderType.FACEBOOK);
        System.out.println("User logged in with Facebook: " + userLoginResp);
    }

    @Test
    @DisplayName("Login with Google provider")
    void loginWithUsingGoogleProvider() {

    }

    // @Test
    @DisplayName("Sign up using email and password")
    void signUpUsingEmailAndPassword() {
        CollectorSignupReq request = new CollectorSignupReq("Seiya Collector", "collector.signup@mythcloth.dev",
                "MythCloth1!");

        ResponseEntity<CollectorSignupResp> response = rest.post().uri(SIGNUP).body(request).retrieve()
                .toEntity(CollectorSignupResp.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().collectorId()).isPositive();
        assertThat(response.getBody().fullName()).isEqualTo(request.fullName());
        assertThat(response.getBody().email()).isEqualTo(request.email());
    }

    private CollectorLoginResp loginWithProvider(ProviderType providerType) {
        CollectorLoginReq request = new CollectorLoginReq("dummy-id-token", "dummy-access-token",
                demoProperties.email(), null);

        ResponseEntity<CollectorLoginResp> response = rest.post().uri(AUTH_PROVIDER, providerType).body(request)
                .retrieve().toEntity(CollectorLoginResp.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().collectorId()).isPositive();
        assertThat(response.getBody().displayName()).isEqualTo("Facebook Collector");
        assertThat(response.getBody().email()).isEqualTo(demoProperties.email());
        assertThat(response.getBody().role()).isEqualTo("Collector");
        assertThat(response.getBody().accessToken()).isNotBlank();
        assertThat(response.getBody().tokenType()).isEqualTo("Bearer");
        assertThat(response.getBody().expiresInSeconds()).isPositive();

        return response.getBody();
    }

    @AfterAll
    static void stopWireMock() {
        FACEBOOK_API.stop();
    }

    private static WireMockServer startFacebookApi() {
        WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        return wireMockServer;
    }
}
