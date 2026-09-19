package com.mesofi.mythclothapi.collectorspurchases;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.mesofi.mythclothapi.collectorproviders.model.ProviderType;
import com.mesofi.mythclothapi.collectors.dto.CollectorLoginReq;
import com.mesofi.mythclothapi.collectors.dto.CollectorLoginResp;
import com.mesofi.mythclothapi.collectorscollections.dto.AssignFigurinesReq;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectionAssignmentMode;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionFigurineResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionReq;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionResp;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseFigurineReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseFigurineResp;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseResp;
import com.mesofi.mythclothapi.collectorspurchases.model.PurchaseChannel;
import com.mesofi.mythclothapi.collectorspurchases.model.PurchaseType;
import com.mesofi.mythclothapi.collectorspurchases.model.ShippingStatus;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.figurines.dto.PaginatedResp;
import com.mesofi.mythclothapi.security.service.SecurityDataService;
import com.mesofi.mythclothapi.support.ControllerBaseIT;
import com.mesofi.mythclothapi.support.PageResponse;

@AutoConfigureMockMvc
@TestPropertySource(properties = "myth-cloth.figurine-import.csv-source=min")
@Sql(scripts = "/seed-catalogs.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-purchases-it.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CollectorPurchaseControllerIT extends ControllerBaseIT {

    private static final Logger log = LoggerFactory.getLogger(CollectorPurchaseControllerIT.class);

    private static final String AUTH_PROVIDER = "/collectors/auth/{provider}";
    private static final String LOAD = "/figurines/load";
    private static final String FIGURINES = "/figurines?page=0&size=24";
    private static final String ASSIGN_FIGURINES_TO_COLLECTION = "/collections/assign-figurines";
    private static final String COLLECTIONS = "/collections";
    private static final String COLLECTIONS_FIGURINES = "/collections/{collectionId}/figurines?page=0&size=40";
    private static final String PURCHASES = "/collectors/purchases";
    private static final String PURCHASES_CREATION = PURCHASES + "/collections/{collectionId}";
    private static final String PURCHASES_RETRIEVAL_BY_ID = PURCHASES + "/{purchaseId}";
    private static final String PURCHASES_UPDATE_BY_ID = PURCHASES + "/{purchaseId}";
    private static final String PURCHASES_DELETION_BY_ID = PURCHASES + "/{purchaseId}";

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
    @DisplayName("Register new purchase for multiple channels")
    void registerNewPurchaseForMultipleChannels() {

        // 1. The user authenticates using the Google provider and obtains a token
        log.info("1. Authenticating collector with Google provider");
        CollectorLoginResp loginResp = authenticateCollectorWithGoogle();

        // 2. The user imports some figurines into the catalog
        log.info("2. Importing figurines into the catalog");
        List<FigurineResp> allFigurines = importFigurinesIntoCatalog(loginResp.accessToken());

        // 3. Once the figurines are in the catalog, the user creates a collection and
        // adds some figurines to it.
        log.info("3. Creating a collection and adding figurines to it");
        CollectorCollectionResp collectionResp = createAndAddFigurinesToCollection(loginResp.accessToken(),
                allFigurines);

        // 4. Having the collection with figurines, we retrieve the figurines from the
        // collection to register them in a purchase.
        log.info("4. Retrieving figurines from the collection to register them in a purchase");
        List<CollectorCollectionFigurineResp> collectionFigurineRespList = retrieveFigurinesFromCollection(
                loginResp.accessToken(), collectionResp);

        // 5. The user registers a new purchase for the collector.
        log.info("5. Registering new purchases for the collector");
        CollectorPurchaseResp onlinePurchaseResp = registerOnlinePurchase(loginResp.accessToken(), collectionResp.id(),
                collectionFigurineRespList);
        log.info("New online purchase registered: {}", onlinePurchaseResp);

        CollectorPurchaseResp inStorePurchaseResp = registerInStorePurchase(loginResp.accessToken(),
                collectionResp.id(), collectionFigurineRespList);
        log.info("New in-store purchase registered: {}", inStorePurchaseResp);

        // 6. The user retrieves all purchases for the collector to verify that both
        // purchases were registered.
        log.info("6. Retrieving all purchases for the collector to verify that both purchases were registered");
        List<CollectorPurchaseResp> purchases = retrieveExistingPurchasesForCollector(loginResp.accessToken(), true);

        // 7. The user retrieves a specific purchase by its ID to verify that the
        // details are correct.
        log.info("7. Retrieving a specific purchase by its ID to verify that the details are correct");
        CollectorPurchaseResp retrievedOnlinePurchase = retrievePurchaseById(loginResp.accessToken(),
                purchases.getFirst().purchaseId());
        assertThat(retrievedOnlinePurchase).isEqualTo(onlinePurchaseResp);
        CollectorPurchaseResp retrievedInStorePurchase = retrievePurchaseById(loginResp.accessToken(),
                purchases.getLast().purchaseId());
        assertThat(retrievedInStorePurchase).isEqualTo(inStorePurchaseResp);

        // 8.1. The user performs multiple edits on the purchases, such as updating the
        // shipping status, changing the order number, and modifying the total amount.
        // The user verifies that the changes are reflected correctly in the retrieved
        // purchase details.
        log.info("8.1. Performing multiple edits on the purchases and verifying the changes");
        updateExistingOnlinePurchasesAndVerifyChanges(loginResp.accessToken(), purchases.getFirst(),
                collectionFigurineRespList);
        // 8.2. Now we do some updates on the in-store purchase, such as changing the
        // seller.
        log.info("8.2. Performing updates on the in-store purchase and verifying the changes");
        updateExistingInStorePurchasesAndVerifyChanges(loginResp.accessToken(), purchases.getLast(),
                collectionFigurineRespList);

        // 9. Retrieve again all the existing purchases for the collector to delete them
        // and verify that the purchases were deleted successfully.
        log.info("9. Retrieving all existing purchases for the collector to delete them");
        List<CollectorPurchaseResp> existingPurchases = retrieveExistingPurchasesForCollector(loginResp.accessToken(),
                false);

        // 10. Finally, the user deletes all existing purchases for the collector and
        // verifies that the purchases were deleted successfully.
        log.info(
                "10. Deleting all existing purchases for the collector and verifying that the purchases were deleted successfully");
        deleteExistingPurchasesAndVerifyDeletion(loginResp.accessToken(), existingPurchases);
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
        ResponseEntity<PaginatedResp> response = rest.get().uri(FIGURINES).headers(bearerToken(jwtCollector)).retrieve()
                .toEntity(PaginatedResp.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        Objects.requireNonNull(response.getBody(), "Response body should not be null");
        Objects.requireNonNull(response.getBody().content(), "Response body content should not be null");
        assertThat(response.getBody().content().size()).isEqualTo(21); // Assuming the CSV source has 21 figurines

        return response.getBody().content();

    }

    /**
     * Creates a new collection for the collector and adds selected figurines to it.
     *
     * @param jwtCollector
     *            The JWT token of the collector.
     * @param allFigurines
     *            The list of all figurines available in the catalog.
     * @return The CollectorCollectionResp representing the created collection with
     *         assigned figurines.
     */
    private CollectorCollectionResp createAndAddFigurinesToCollection(final String jwtCollector,
            List<FigurineResp> allFigurines) {

        // Only select a few figurines to add to the collection, for example, the first
        // 5 figurines
        List<Long> selectedFigurineIds = List.of(allFigurines.get(0).id(), allFigurines.get(5).id(),
                allFigurines.get(10).id(), allFigurines.get(15).id(), allFigurines.get(20).id());

        AssignFigurinesReq assignFigurinesReq = new AssignFigurinesReq(selectedFigurineIds,
                CollectionAssignmentMode.CREATE, null,
                new CollectorCollectionReq(false, "First Collection", null, null));

        rest.post().uri(ASSIGN_FIGURINES_TO_COLLECTION).headers(bearerToken(jwtCollector)).body(assignFigurinesReq)
                .retrieve().toEntity(Void.class);

        // Retrieve the existing collections for the collector to verify that the
        // collection was created and figurines were assigned
        ResponseEntity<List<CollectorCollectionResp>> response = rest.get().uri(COLLECTIONS)
                .headers(bearerToken(jwtCollector)).retrieve().toEntity(new ParameterizedTypeReference<>() {
                });

        Assertions.assertThat(response.getStatusCode()).isEqualTo(OK);
        Assertions.assertThat(response.getBody()).isNotNull();
        List<CollectorCollectionResp> collections = response.getBody();
        assertThat(collections).isNotNull();
        assertThat(collections.size()).isEqualTo(1);
        return collections.getFirst(); // this scenario only handles one collection, so we can safely return the first
                                        // one
    }

    /**
     * Retrieves the figurines from a specific collection for the collector.
     *
     * @param jwtCollector
     *            The JWT token of the collector.
     * @param collection
     *            The CollectorCollectionResp representing the collection from which
     *            to retrieve figurines.
     * @return A list of CollectorCollectionFigurineResp representing the figurines
     *         in the specified collection.
     */
    private List<CollectorCollectionFigurineResp> retrieveFigurinesFromCollection(final String jwtCollector,
            CollectorCollectionResp collection) {
        ResponseEntity<PageResponse<CollectorCollectionFigurineResp>> response = rest.get()
                .uri(COLLECTIONS_FIGURINES, collection.id()).headers(bearerToken(jwtCollector)).retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        Objects.requireNonNull(response.getBody(), "Response body should not be null");
        Objects.requireNonNull(response.getBody().content(), "Response body content should not be null");

        return response.getBody().content();
    }

    /**
     * Registers a new online purchase for the collector using the figurines from
     * the specified collection.
     *
     * @param jwtCollector
     *            The JWT token of the collector.
     * @param collectionId
     *            The ID of the collection from which to register the purchase.
     * @param collectionFigurineRespList
     *            The list of figurines in the specified collection.
     * @return The CollectorPurchaseResp representing the registered online
     *         purchase.
     */
    private CollectorPurchaseResp registerOnlinePurchase(final String jwtCollector, final Long collectionId,
            final List<CollectorCollectionFigurineResp> collectionFigurineRespList) {

        // The user can only register purchases on the figurines the collector owns, so
        // we will use the figurines from the collection to register the purchase.
        List<CollectorCollectionFigurineResp> owningFigurines = collectionFigurineRespList.stream()
                .filter(CollectorCollectionFigurineResp::isCollected).toList();

        // I only need to register purchases for some of the owning figurines, so I will
        // select the first and last figurines from the owning figurines list to
        // register a purchase for them.
        CollectorCollectionFigurineResp firstFigurine = owningFigurines.getFirst();
        CollectorCollectionFigurineResp lastFigurine = owningFigurines.getLast();

        List<CollectorPurchaseFigurineReq> purchaseFigurineReqs = Stream.of(firstFigurine, lastFigurine)
                .map(ccf -> new CollectorPurchaseFigurineReq(ccf.collectionFigurineId(), ccf.ownedQuantity(),
                        new BigDecimal("6400.00"), PurchaseType.RETAIL))
                .toList();

        CollectorPurchaseReq request = new CollectorPurchaseReq(LocalDate.now(), "Mandarake", "XQKUHSCWV",
                Currency.getInstance("JPY"), PurchaseChannel.ONLINE, ShippingStatus.DELIVERED, "1ZV912320456954189",
                "FedEx", purchaseFigurineReqs);

        CollectorPurchaseResp body = createPurchaseAndGetResponse(jwtCollector, collectionId, request);
        assertThat(body.purchaseId()).isNotNull();
        assertThat(body.purchaseId()).isPositive();
        assertThat(body.seller()).isEqualTo("Mandarake");
        assertThat(body.orderNumber()).isEqualTo("XQKUHSCWV");
        assertThat(body.currency()).isEqualTo("JPY");
        assertThat(body.totalAmount()).isEqualTo(new BigDecimal("12800.00"));
        assertThat(body.purchaseChannel()).isEqualTo(PurchaseChannel.ONLINE);
        assertThat(body.shippingStatus()).isEqualTo(ShippingStatus.DELIVERED);
        assertThat(body.trackingNumber()).isEqualTo("1ZV912320456954189");
        assertThat(body.carrier()).isEqualTo("FedEx");
        assertThat(body.deliveredDate()).isNotNull();
        assertThat(body.shippedDate()).isNull();

        return body;
    }

    /**
     * Registers a new in-store purchase for the collector using the figurines from
     * the specified collection.
     *
     * @param jwtCollector
     *            The JWT token of the collector.
     * @param collectionId
     *            The ID of the collection from which to register the purchase.
     * @param collectionFigurineRespList
     *            The list of figurines in the specified collection.
     * @return The CollectorPurchaseResp representing the registered in-store
     *         purchase.
     */
    private CollectorPurchaseResp registerInStorePurchase(final String jwtCollector, final Long collectionId,
            final List<CollectorCollectionFigurineResp> collectionFigurineRespList) {

        // The user can only register purchases on the figurines the collector owns, so
        // we will use the figurines from the collection to register the purchase.
        List<CollectorCollectionFigurineResp> owningFigurines = collectionFigurineRespList.stream()
                .filter(CollectorCollectionFigurineResp::isCollected).toList();

        // I only need to register purchases for some of the owning figurines, so I will
        // select the second figurine from the owning figurines list to register a
        // purchase for it.
        CollectorCollectionFigurineResp secondFigurine = owningFigurines.get(1);

        List<CollectorPurchaseFigurineReq> purchaseFigurineReqs = Stream.of(secondFigurine)
                .map(ccf -> new CollectorPurchaseFigurineReq(ccf.collectionFigurineId(), ccf.ownedQuantity(),
                        new BigDecimal("1200.00"), PurchaseType.SECOND_HAND))
                .toList();

        CollectorPurchaseReq request = new CollectorPurchaseReq(LocalDate.now(), "Rockshow", null,
                Currency.getInstance("MXN"), PurchaseChannel.PHYSICAL_STORE, null, null, null, purchaseFigurineReqs);

        CollectorPurchaseResp body = createPurchaseAndGetResponse(jwtCollector, collectionId, request);
        assertThat(body.purchaseId()).isNotNull();
        assertThat(body.purchaseId()).isPositive();
        assertThat(body.seller()).isEqualTo("Rockshow");
        assertThat(body.orderNumber()).isNull();
        assertThat(body.currency()).isEqualTo("MXN");
        assertThat(body.totalAmount()).isEqualTo(new BigDecimal("1200.00"));
        assertThat(body.purchaseChannel()).isEqualTo(PurchaseChannel.PHYSICAL_STORE);
        assertThat(body.shippingStatus()).isNull();
        assertThat(body.trackingNumber()).isNull();
        assertThat(body.carrier()).isNull();
        assertThat(body.deliveredDate()).isNull();
        assertThat(body.shippedDate()).isNull();

        return body;
    }

    /**
     * Retrieves all existing purchases for the collector and performs assertions to
     * verify the correctness of the retrieved data.
     *
     * @param jwtCollector
     *            The JWT token of the collector.
     * @param assertPurchases
     *            A boolean flag indicating whether to perform assertions on the
     *            retrieved purchases.
     * @return A list of CollectorPurchaseResp representing the existing purchases
     *         for the collector.
     */
    private List<CollectorPurchaseResp> retrieveExistingPurchasesForCollector(final String jwtCollector,
            final boolean assertPurchases) {

        ResponseEntity<List<CollectorPurchaseResp>> response = rest.get().uri(PURCHASES)
                .headers(bearerToken(jwtCollector)).retrieve().toEntity(new ParameterizedTypeReference<>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        List<CollectorPurchaseResp> purchases = response.getBody();
        assertThat(purchases).isNotNull();
        Objects.requireNonNull(purchases, "Purchases response body should not be null");

        if (!assertPurchases) {
            return purchases;
        }

        assertThat(purchases.size()).isEqualTo(2);

        // online purchase assertions
        assertThat(purchases.getFirst()).isNotNull()
                .extracting(CollectorPurchaseResp::purchaseId, CollectorPurchaseResp::purchaseDate,
                        CollectorPurchaseResp::seller, CollectorPurchaseResp::orderNumber,
                        CollectorPurchaseResp::currency, CollectorPurchaseResp::totalAmount,
                        CollectorPurchaseResp::purchaseChannel, CollectorPurchaseResp::shippingStatus,
                        CollectorPurchaseResp::trackingNumber, CollectorPurchaseResp::carrier,
                        CollectorPurchaseResp::shippedDate, CollectorPurchaseResp::deliveredDate,
                        CollectorPurchaseResp::figurines)
                .containsExactly(1L, LocalDate.now(), "Mandarake", "XQKUHSCWV", "JPY", new BigDecimal("12800.00"),
                        PurchaseChannel.ONLINE, ShippingStatus.DELIVERED, "1ZV912320456954189", "FedEx", null,
                        LocalDate.now(),
                        List.of(new CollectorPurchaseFigurineResp(1L, 1L, 1, new BigDecimal("6400.00"),
                                PurchaseType.RETAIL),
                                new CollectorPurchaseFigurineResp(2L, 16L, 1, new BigDecimal("6400.00"),
                                        PurchaseType.RETAIL)));

        // in-store purchase assertions
        assertThat(purchases.getLast()).isNotNull()
                .extracting(CollectorPurchaseResp::purchaseId, CollectorPurchaseResp::purchaseDate,
                        CollectorPurchaseResp::seller, CollectorPurchaseResp::orderNumber,
                        CollectorPurchaseResp::currency, CollectorPurchaseResp::totalAmount,
                        CollectorPurchaseResp::purchaseChannel, CollectorPurchaseResp::shippingStatus,
                        CollectorPurchaseResp::trackingNumber, CollectorPurchaseResp::carrier,
                        CollectorPurchaseResp::shippedDate, CollectorPurchaseResp::deliveredDate,
                        CollectorPurchaseResp::figurines)
                .containsExactly(2L, LocalDate.now(), "Rockshow", null, "MXN", new BigDecimal("1200.00"),
                        PurchaseChannel.PHYSICAL_STORE, null, null, null, null, null,
                        List.of(new CollectorPurchaseFigurineResp(3L, 8L, 1, new BigDecimal("1200.00"),
                                PurchaseType.SECOND_HAND)));

        return purchases;
    }

    /**
     * Retrieves a specific purchase by its ID for the given collector.
     *
     * @param jwtCollector
     *            The JWT token of the collector.
     * @param purchaseId
     *            The ID of the purchase to retrieve.
     * @return The CollectorPurchaseResp representing the retrieved purchase.
     */
    private CollectorPurchaseResp retrievePurchaseById(String jwtCollector, Long purchaseId) {
        ResponseEntity<CollectorPurchaseResp> response = rest.get().uri(PURCHASES_RETRIEVAL_BY_ID, purchaseId)
                .headers(bearerToken(jwtCollector)).retrieve().toEntity(CollectorPurchaseResp.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        Objects.requireNonNull(response.getBody(), "Purchase response body should not be null");
        assertThat(response.getBody()).isNotNull();

        return response.getBody();
    }

    private void updateExistingOnlinePurchasesAndVerifyChanges(String jwtCollector, CollectorPurchaseResp online,
            List<CollectorCollectionFigurineResp> collectionFigurines) {
        // Update the first purchase (online purchase)
        CollectorPurchaseFigurineResp existingFirstFigurine = online.figurines().getFirst();
        CollectorPurchaseFigurineResp existingLastFigurine = online.figurines().getLast();
        CollectorCollectionFigurineResp newCollectionFigurine = collectionFigurines.stream()
                .filter(CollectorCollectionFigurineResp::isCollected)
                .filter(collectionFigurine -> online.figurines().stream().noneMatch(purchaseFigurine -> purchaseFigurine
                        .collectionFigurineId() == collectionFigurine.collectionFigurineId()))
                .findFirst().orElseThrow();

        List<CollectorPurchaseFigurineReq> updatedFigurines = List.of(
                // UPDATE quantity 1 → 2
                new CollectorPurchaseFigurineReq(existingFirstFigurine.collectionFigurineId(), 2,
                        new BigDecimal("6400.00"), PurchaseType.RETAIL),
                // No change
                new CollectorPurchaseFigurineReq(existingLastFigurine.collectionFigurineId(), 1,
                        new BigDecimal("6400.00"), PurchaseType.RETAIL),
                // ADD new figurine
                new CollectorPurchaseFigurineReq(newCollectionFigurine.collectionFigurineId(), 1,
                        new BigDecimal("1111.00"), PurchaseType.SECOND_HAND));

        CollectorPurchaseReq request = new CollectorPurchaseReq(LocalDate.of(2026, 3, 3), "Jungle", "NEW-XQKUHSCWV-NEW",
                Currency.getInstance("USD"), online.purchaseChannel(), ShippingStatus.NOT_SHIPPED, "877394518353",
                "UPS", updatedFigurines);

        ResponseEntity<CollectorPurchaseResp> response = rest.put().uri(PURCHASES_UPDATE_BY_ID, online.purchaseId())
                .headers(bearerToken(jwtCollector)).body(request).retrieve().toEntity(CollectorPurchaseResp.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        Objects.requireNonNull(response.getBody(), "Purchase response body should not be null");
        assertThat(response.getBody()).isNotNull();

        assertThat(response.getBody()).isNotNull()
                .extracting(CollectorPurchaseResp::purchaseId, CollectorPurchaseResp::purchaseDate,
                        CollectorPurchaseResp::seller, CollectorPurchaseResp::orderNumber,
                        CollectorPurchaseResp::currency, CollectorPurchaseResp::totalAmount,
                        CollectorPurchaseResp::purchaseChannel, CollectorPurchaseResp::shippingStatus,
                        CollectorPurchaseResp::trackingNumber, CollectorPurchaseResp::carrier,
                        CollectorPurchaseResp::shippedDate, CollectorPurchaseResp::deliveredDate)
                .containsExactly(online.purchaseId(), LocalDate.of(2026, 3, 3), "Jungle", "NEW-XQKUHSCWV-NEW", "USD",
                        new BigDecimal("20311.00"), PurchaseChannel.ONLINE, ShippingStatus.NOT_SHIPPED, "877394518353",
                        "UPS", null, null);

        Assertions.assertThat(response.getBody().figurines()).hasSize(3);
        Assertions.assertThat(response.getBody().figurines())
                .filteredOn(purchaseFigurine -> purchaseFigurine.collectionFigurineId() == existingFirstFigurine
                        .collectionFigurineId())
                .singleElement()
                .satisfies(purchaseFigurine -> assertThat(purchaseFigurine)
                        .extracting(CollectorPurchaseFigurineResp::quantity, CollectorPurchaseFigurineResp::pricePaid,
                                CollectorPurchaseFigurineResp::purchaseType)
                        .containsExactly(2, new BigDecimal("6400.00"), PurchaseType.RETAIL));
        Assertions.assertThat(response.getBody().figurines())
                .filteredOn(purchaseFigurine -> purchaseFigurine.collectionFigurineId() == existingLastFigurine
                        .collectionFigurineId())
                .singleElement()
                .satisfies(purchaseFigurine -> assertThat(purchaseFigurine)
                        .extracting(CollectorPurchaseFigurineResp::quantity, CollectorPurchaseFigurineResp::pricePaid,
                                CollectorPurchaseFigurineResp::purchaseType)
                        .containsExactly(1, new BigDecimal("6400.00"), PurchaseType.RETAIL));
        Assertions.assertThat(response.getBody().figurines())
                .filteredOn(purchaseFigurine -> purchaseFigurine.collectionFigurineId() == newCollectionFigurine
                        .collectionFigurineId())
                .singleElement().satisfies(purchaseFigurine -> assertThat(purchaseFigurine.id()).isPositive());
        Assertions.assertThat(response.getBody().figurines())
                .filteredOn(purchaseFigurine -> purchaseFigurine.collectionFigurineId() == newCollectionFigurine
                        .collectionFigurineId())
                .singleElement()
                .satisfies(purchaseFigurine -> assertThat(purchaseFigurine)
                        .extracting(CollectorPurchaseFigurineResp::quantity, CollectorPurchaseFigurineResp::pricePaid,
                                CollectorPurchaseFigurineResp::purchaseType)
                        .containsExactly(1, new BigDecimal("1111.00"), PurchaseType.SECOND_HAND));
    }

    /**
     * Updates an existing in-store purchase for the collector and verifies the
     * changes.
     *
     * @param jwtCollector
     *            The JWT token of the collector.
     * @param inStore
     *            The CollectorPurchaseResp representing the existing in-store
     *            purchase to be updated.
     * @param collectionFigurines
     *            The list of figurines in the collector's collection.
     */
    private void updateExistingInStorePurchasesAndVerifyChanges(String jwtCollector, CollectorPurchaseResp inStore,
            List<CollectorCollectionFigurineResp> collectionFigurines) {
        // Update the in-store purchase by changing the orderDate, seller and other
        // collector figurines.

        // For this example, this collectorFigurineId must be one. It's safe to extract
        // the first one
        Long existingCollectionFigurineId = inStore.figurines().getFirst().collectionFigurineId();

        List<CollectorCollectionFigurineResp> collectedFigurines = collectionFigurines.stream()
                .filter(CollectorCollectionFigurineResp::isCollected).toList();

        // I can add any of the available figurines whose collectionFigurineId is not
        // the same as the existing one in the purchase.
        Long newCollectionFigurineId = collectedFigurines.stream()
                .map(CollectorCollectionFigurineResp::collectionFigurineId)
                .filter(l -> !(l.equals(existingCollectionFigurineId))).findFirst()
                .orElseThrow(() -> new IllegalStateException("No available figurines to add to the purchase"));

        List<CollectorPurchaseFigurineReq> updatedFigurines = List.of(new CollectorPurchaseFigurineReq(
                newCollectionFigurineId, 10, new BigDecimal("1110.00"), PurchaseType.RETAIL));

        CollectorPurchaseReq request = new CollectorPurchaseReq(LocalDate.of(2026, 6, 6), "AnotherSeller",
                inStore.orderNumber(), Currency.getInstance(inStore.currency()), inStore.purchaseChannel(), null, null,
                null, updatedFigurines);

        ResponseEntity<CollectorPurchaseResp> response = rest.put().uri(PURCHASES_UPDATE_BY_ID, inStore.purchaseId())
                .headers(bearerToken(jwtCollector)).body(request).retrieve().toEntity(CollectorPurchaseResp.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        Objects.requireNonNull(response.getBody(), "Purchase response body should not be null");
        assertThat(response.getBody()).isNotNull();

        assertThat(response.getBody()).isNotNull()
                .extracting(CollectorPurchaseResp::purchaseId, CollectorPurchaseResp::purchaseDate,
                        CollectorPurchaseResp::seller, CollectorPurchaseResp::orderNumber,
                        CollectorPurchaseResp::currency, CollectorPurchaseResp::totalAmount,
                        CollectorPurchaseResp::purchaseChannel, CollectorPurchaseResp::shippingStatus,
                        CollectorPurchaseResp::trackingNumber, CollectorPurchaseResp::carrier,
                        CollectorPurchaseResp::shippedDate, CollectorPurchaseResp::deliveredDate)
                .containsExactly(inStore.purchaseId(), LocalDate.of(2026, 6, 6), "AnotherSeller", null, "MXN",
                        new BigDecimal("11100.00"), PurchaseChannel.PHYSICAL_STORE, null, null, null, null, null);

        Assertions.assertThat(response.getBody().figurines()).hasSize(1);
        assertThat(response.getBody().figurines().getFirst()).isNotNull()
                .extracting(CollectorPurchaseFigurineResp::collectionFigurineId,
                        CollectorPurchaseFigurineResp::quantity, CollectorPurchaseFigurineResp::pricePaid,
                        CollectorPurchaseFigurineResp::purchaseType)
                .containsExactly(newCollectionFigurineId, 10, new BigDecimal("1110.00"), PurchaseType.RETAIL);
    }

    /**
     * Deletes existing purchases for the collector and verifies that the deletion
     * was successful.
     *
     * @param jwtCollector
     *            The JWT token of the collector.
     * @param existingPurchases
     *            The list of existing purchases to be deleted.
     */
    private void deleteExistingPurchasesAndVerifyDeletion(String jwtCollector,
            List<CollectorPurchaseResp> existingPurchases) {

        for (CollectorPurchaseResp purchase : existingPurchases) {

            ResponseEntity<Void> response = rest.delete().uri(PURCHASES_DELETION_BY_ID, purchase.purchaseId())
                    .headers(bearerToken(jwtCollector)).retrieve().toEntity(Void.class);

            Assertions.assertThat(response.getStatusCode()).as("Purchase deletion should return HTTP 204")
                    .isEqualTo(NO_CONTENT);
        }

        // Verify that all purchases have been deleted
        List<CollectorPurchaseResp> remainingPurchases = retrieveExistingPurchasesForCollector(jwtCollector, false);
        Assertions.assertThat(remainingPurchases).isEmpty();
    }

    /**
     * Creates a new purchase for the collector and returns the response.
     *
     * @param jwtCollector
     *            The JWT token of the collector.
     * @param collectionId
     *            The ID of the collection for which to create the purchase.
     * @param request
     *            The CollectorPurchaseReq containing the purchase details.
     * @return The CollectorPurchaseResp representing the created purchase.
     */
    private CollectorPurchaseResp createPurchaseAndGetResponse(final String jwtCollector, final Long collectionId,
            final CollectorPurchaseReq request) {
        // The jwtToken is used to authenticate the request, and the request body
        // contains the purchase details.
        ResponseEntity<CollectorPurchaseResp> response = rest.post().uri(PURCHASES_CREATION, collectionId)
                .headers(bearerToken(jwtCollector)).body(request).retrieve().toEntity(CollectorPurchaseResp.class);

        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        Objects.requireNonNull(response.getBody(), "Purchase response body should not be null");
        assertThat(response.getBody()).isNotNull();

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
