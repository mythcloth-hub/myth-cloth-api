package com.mesofi.mythclothapi.figurines;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.mesofi.mythclothapi.collectorproviders.model.ProviderType;
import com.mesofi.mythclothapi.collectors.dto.CollectorLoginReq;
import com.mesofi.mythclothapi.collectors.dto.CollectorLoginResp;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.figurines.dto.PaginatedResp;
import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;
import com.mesofi.mythclothapi.security.service.SecurityDataService;
import com.mesofi.mythclothapi.support.ControllerBaseIT;

@AutoConfigureMockMvc
@TestPropertySource(properties = "myth-cloth.figurine-import.csv-source=min")
@Sql(scripts = "/seed-catalogs.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-figurine-search-it.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class FigurineSearchControllerIT extends ControllerBaseIT {

    private static final Logger log = LoggerFactory.getLogger(FigurineSearchControllerIT.class);

    private static final int MAX_FIGURINES = 21;
    private static final int MAX_FIGURINES_PER_PAGE = 5;

    private static final String AUTH_PROVIDER = "/collectors/auth/{provider}";
    private static final String LOAD = "/figurines/load";
    private static final String FIGURINES = "/figurines";

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

    /**
     * This test verifies the figurine search functionality by performing the
     * following steps:
     * <ol>
     * <li>Authenticates a collector using the Google provider and obtains a token.
     * </li>
     * <li>Imports figurines into the catalog.</li>
     * <li>Queries the figurines to ensure they are present in the catalog.</li>
     * <li>Searches for figurines by name, series, or catalog number.</li>
     * </ol>
     */
    @Test
    @DisplayName("Manage figurine search functionality")
    void manageFigurineSearch() {
        // 1. The user authenticates using the Google provider and obtains a token
        log.info("1. Authenticating collector with Google provider");
        String token = authenticateCollectorWithGoogle().accessToken();

        // 2. The user imports some figurines into the catalog
        log.info("2. Importing figurines into the catalog");
        List<FigurineResp> allFigurines = importFigurinesIntoCatalog(token);

        // 3. Now we query the figurines to ensure they are present in the catalog
        log.info("3. Querying the figurines to ensure they are present in the catalog");
        List<FigurineResp> existingFigurines = retrieveExistingFigurinesByPagination(token, allFigurines);

        // 4. The user can now search for figurines by name, series, or catalog number
        log.info("4. Searching for figurines by name, series, or catalog number");
        List<FigurineResp> filteredFigurines = retrieveExistingFigurinesWithFilters(token, existingFigurines);
        assertThat(filteredFigurines.size()).isEqualTo(3); // There are 3 figurines with "Seiya" in the name
    }

    /**
     * Authenticates a collector using the Google provider and returns the login
     * response.
     *
     * @return The CollectorLoginResp containing the collector's information and
     *         access token.
     */
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

    /**
     * Imports figurines into the catalog and returns the list of imported
     * figurines.
     *
     * @param jwtCollector
     *            The JWT token of the collector.
     * @return A list of FigurineResp representing the imported figurines.
     */
    private List<FigurineResp> importFigurinesIntoCatalog(final String jwtCollector) {
        // By default, the admin auth is used to load the figurines, no need to pass the
        // jwtCollector for this operation because the collector does not have the
        // required permissions to load the figurines into the catalog. The admin user
        // is used for this operation.
        ResponseEntity<Void> responseLoad = rest.post().uri(LOAD).retrieve().toEntity(Void.class);
        assertThat(responseLoad.getStatusCode()).isEqualTo(ACCEPTED);

        // query the imported figurines to ensure they are present in the catalog
        ResponseEntity<PaginatedResp> response = rest.get()
                .uri(uriBuilder -> uriBuilder.path(FIGURINES).queryParam("page", 0).queryParam("size", 30).build())
                .headers(bearerToken(jwtCollector)).retrieve().toEntity(PaginatedResp.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        Objects.requireNonNull(response.getBody(), "Response body should not be null");
        Objects.requireNonNull(response.getBody().content(), "Response body content should not be null");
        assertThat(response.getBody().content().size()).isEqualTo(MAX_FIGURINES); // Assuming the CSV source has 21
                                                                                    // figurines

        return response.getBody().content();

    }

    /**
     * Retrieves existing figurines from the catalog using pagination and compares
     * them with the imported figurines.
     *
     * @param jwtCollector
     *            The JWT token of the collector.
     * @param importedFigurines
     *            The list of imported figurines to compare against.
     * @return A list of FigurineResp representing all retrieved figurines.
     */
    private List<FigurineResp> retrieveExistingFigurinesByPagination(final String jwtCollector,
            List<FigurineResp> importedFigurines) {

        List<FigurineResp> allFigurines = new ArrayList<>();
        int currentPage = 0;
        while (true) {
            PaginatedResp response = getExistingFigurinesByPagination(jwtCollector, currentPage,
                    new LinkedMultiValueMap<>());
            currentPage++;
            allFigurines.addAll(response.content());
            if (currentPage >= response.size()) {
                break;
            }
        }

        // Imported figurines are the same as the retrieved figurines, the retrieved
        // figurines are paginated, so we need to compare the size and the content of
        // the lists
        assertThat(allFigurines.size()).isEqualTo(importedFigurines.size());
        assertThat(allFigurines).isEqualTo(importedFigurines);

        return allFigurines;
    }

    /**
     * Retrieves existing figurines from the catalog using filters (name, series, or
     * catalog number).
     *
     * @param jwtCollector
     *            The JWT token of the collector.
     * @param existingFigurines
     *            The list of existing figurines to filter against.
     * @return A list of FigurineResp representing the filtered figurines.
     */
    private List<FigurineResp> retrieveExistingFigurinesWithFilters(final String jwtCollector,
            List<FigurineResp> existingFigurines) {

        MultiValueMap<String, String> filters = new LinkedMultiValueMap<>();
        filters.add("name", "Seiya");

        PaginatedResp response = getExistingFigurinesByPagination(jwtCollector, 0, filters);
        assertThat(response.content().size()).isEqualTo(3); // There are 3 figurines with "Seiya" in the name
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(5); // Page size is 5
        assertThat(response.totalElements()).isEqualTo(3); // Total elements matching the filter
        assertThat(response.totalPages()).isEqualTo(1); // Total pages matching the filter

        List<FigurineResp> filteredFigurines = response.content();

        assertThat(filteredFigurines.getFirst()).isNotNull()
                .extracting(FigurineResp::id, FigurineResp::isCollected, FigurineResp::name,
                        FigurineResp::releaseStatus)
                .containsExactly(8L, true, "Pegasus Seiya", ReleaseStatus.ANNOUNCED);

        assertThat(filteredFigurines.get(1)).isNotNull()
                .extracting(FigurineResp::id, FigurineResp::isCollected, FigurineResp::name,
                        FigurineResp::releaseStatus)
                .containsExactly(11L, true, "Pegasus Seiya", ReleaseStatus.ANNOUNCED);

        assertThat(filteredFigurines.getLast()).isNotNull()
                .extracting(FigurineResp::id, FigurineResp::isCollected, FigurineResp::name,
                        FigurineResp::releaseStatus)
                .containsExactly(14L, true, "Pegasus Seiya", ReleaseStatus.RELEASED);

        return filteredFigurines;
    }

    private PaginatedResp getExistingFigurinesByPagination(final String jwtCollector, int page,
            MultiValueMap<String, String> filters) {

        ResponseEntity<PaginatedResp> response = rest.get()
                .uri(uriBuilder -> uriBuilder.path(FIGURINES).queryParam("page", page)
                        .queryParam("size", MAX_FIGURINES_PER_PAGE).queryParams(filters).build())
                .headers(bearerToken(jwtCollector)).retrieve().toEntity(PaginatedResp.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        Objects.requireNonNull(response.getBody(), "Response body should not be null");
        Objects.requireNonNull(response.getBody().content(), "Response body content should not be null");
        return response.getBody();
    }

    private static WireMockServer startGoogleApi() {
        WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        return wireMockServer;
    }

    private Consumer<HttpHeaders> bearerToken(String jwtToken) {
        return headers -> headers.set("Authorization", "Bearer %s".formatted(jwtToken));
    }
}
