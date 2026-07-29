package com.mesofi.mythclothapi.figurinestores;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import com.mesofi.mythclothapi.catalogs.dto.CatalogReq;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.distributors.model.DistributorName;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;
import com.mesofi.mythclothapi.figurines.dto.DistributorReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreMatchedResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStorePriceResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreUnmatchedResp;
import com.mesofi.mythclothapi.it.ControllerBaseIT;
import com.mesofi.mythclothapi.messaging.pricing.model.LineUP;
import com.mesofi.mythclothapi.messaging.pricing.model.ListingStatus;
import com.mesofi.mythclothapi.messaging.pricing.model.StoreListing;
import com.mesofi.mythclothapi.messaging.pricing.model.StoreName;
import com.mesofi.mythclothapi.stores.dto.StoreReq;
import com.mesofi.mythclothapi.stores.dto.StoreResp;

@Sql(scripts = "/cleanup-store-it.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class FigurineStoreServiceIT extends ControllerBaseIT {

    private static final Logger log = LoggerFactory.getLogger(FigurineStoreServiceIT.class);

    private static final String STORES_ENDPOINT = "/stores";
    private static final String FIGURINES_ENDPOINT = "/figurines";
    private static final String DISTRIBUTORS_ENDPOINT = "/distributors";
    private static final String CATALOGS_ENDPOINT = "/catalogs";
    private static final String LINEUP_RESOURCE = "/lineups";
    private static final String SERIES_RESOURCE = "/series";
    private static final String FIGURINE_STORE_RESOURCE = "/figurine-stores";
    private static final String UNMATCHED_LISTINGS_ENDPOINT = FIGURINE_STORE_RESOURCE + "/unmatched-listings";
    private static final String MATCHED_LISTINGS_ENDPOINT = FIGURINE_STORE_RESOURCE + "/matched-listings";

    @Autowired
    private FigurineStoreService figurineStoreService;

    @Test
    @DisplayName("NIN-NIN-GAME -> Should process store listings")
    void shouldProcessStoreListingsFlow() {

        StoreReq ninNinStore = new StoreReq("Nin-Nin-Game", StoreName.NIN_NIN_GAME,
                URI.create("https://www.nin-nin-game.com"), URI.create("https://www.nin-nin-game.com/img/logo.png"),
                Currency.getInstance("MXN"), "JP", true);

        com.mesofi.mythclothapi.distributors.dto.DistributorReq request = new com.mesofi.mythclothapi.distributors.dto.DistributorReq(
                DistributorName.BANDAI, CountryCode.JP, "https://tamashii.jp/");

        // CREATE
        Long storeId = createStore(ninNinStore);
        Long lineUpMythId = createCatalog(LINEUP_RESOURCE, new CatalogReq("Myth Cloth"));
        Long seriesId = createCatalog(SERIES_RESOURCE, new CatalogReq("Saint Seiya"));
        Long distributorId = createDistributor(request);

        List<DistributorReq> distributors = List.of(new DistributorReq(distributorId, CurrencyCode.JPY, 5000.0, null,
                null, LocalDate.of(2011, 6, 18), true));

        FigurineReq figurineReq = new FigurineReq("Phoenix Ikki", distributors, "https://tamashiiweb.com/item/1502",
                null, lineUpMythId, seriesId, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null);
        FigurineResp figurineFound = createFigurines(figurineReq);

        // some figurines were processed ...
        createNinNinGameStore().forEach(listing -> figurineStoreService.processStorePricing(listing));

        // READ
        List<FigurineStoreUnmatchedResp> unmatchedFigurines = readUnMatchedFigurines();

        for (FigurineStoreUnmatchedResp unmatchedFigurine : unmatchedFigurines) {
            log.info("Matching listing [{}] - {} <===> figurineId [{}] - {}", unmatchedFigurine.id(),
                    unmatchedFigurine.originalName(), figurineFound.id(), figurineFound.displayableName());

            matchUnmatchedListingToFigurine(unmatchedFigurine.id(), figurineFound.id());
        }

        // the unmatched list should be empty after the matching process.
        unmatchedFigurines = readUnMatchedFigurines();
        assertThat(unmatchedFigurines).isEmpty();

        List<FigurineStoreMatchedResp> matchedFigurines = readMatchedFigurines(storeId);
        assertThat(matchedFigurines).hasSize(2)
                .extracting(FigurineStoreMatchedResp::figurineId, FigurineStoreMatchedResp::figurineDisplayableName,
                        FigurineStoreMatchedResp::figurineLineUp, FigurineStoreMatchedResp::storeId,
                        FigurineStoreMatchedResp::storeCurrency, FigurineStoreMatchedResp::storeOriginalName,
                        FigurineStoreMatchedResp::storeProductImageUrl, FigurineStoreMatchedResp::storeProductUrl,
                        FigurineStoreMatchedResp::storeStatus,
                        resp -> resp.storePrices().stream().map(FigurineStorePriceResp::realTimePrice).toList())
                .containsExactlyInAnyOrder(tuple(figurineFound.id(), figurineFound.displayableName(),
                        figurineFound.lineUp().description(), storeId, Currency.getInstance("MXN"),
                        "Saint Seiya Myth Cloth - Bronze Saint Phoenix Ikki [Used]",
                        "https://media2.nin-nin-game.com/88515-pos_product/saint-seiya-myth-cloth-bronze-saint-phoenix-ikki-used.jpg",
                        "https://www.nin-nin-game.com/en/myth-cloth-saint-seiya/15903-saint-seiya-myth-cloth-bronze-saint-phoenix-ikki-used.html",
                        ListingStatus.OUT_OF_STOCK, List.of(new BigDecimal("3098.00"))),
                        tuple(figurineFound.id(), figurineFound.displayableName(), figurineFound.lineUp().description(),
                                storeId, Currency.getInstance("MXN"),
                                "Saint Seiya Myth Cloth - Bronze Saint Phoenix Ikki",
                                "https://media3.nin-nin-game.com/88514-pos_product/saint-seiya-myth-cloth-bronze-saint-phoenix-ikki.jpg",
                                "https://www.nin-nin-game.com/en/myth-cloth-saint-seiya/2804-saint-seiya-myth-cloth-bronze-saint-phoenix-ikki.html",
                                ListingStatus.OUT_OF_STOCK, List.of(new BigDecimal("5013.00"))));
    }

    private List<StoreListing> createNinNinGameStore() {
        return List.of(new StoreListing(StoreName.NIN_NIN_GAME, LineUP.MYTH_CLOTH,
                "Saint Seiya Myth Cloth - Bronze Saint Phoenix Ikki [Used]", "bronze saint phoenix ikki [used]",
                "https://media2.nin-nin-game.com/88515-pos_product/saint-seiya-myth-cloth-bronze-saint-phoenix-ikki-used.jpg",
                "https://www.nin-nin-game.com/en/myth-cloth-saint-seiya/15903-saint-seiya-myth-cloth-bronze-saint-phoenix-ikki-used.html",
                new BigDecimal(3098), null, new BigDecimal(3098), Currency.getInstance("MXN"),
                ListingStatus.OUT_OF_STOCK, Instant.parse("2026-07-28T17:05:17.623880Z")),
                new StoreListing(StoreName.NIN_NIN_GAME, LineUP.MYTH_CLOTH,
                        "Saint Seiya Myth Cloth - Bronze Saint Phoenix Ikki", "bronze saint phoenix ikki",
                        "https://media3.nin-nin-game.com/88514-pos_product/saint-seiya-myth-cloth-bronze-saint-phoenix-ikki.jpg",
                        "https://www.nin-nin-game.com/en/myth-cloth-saint-seiya/2804-saint-seiya-myth-cloth-bronze-saint-phoenix-ikki.html",
                        new BigDecimal(5013), null, new BigDecimal(5013), Currency.getInstance("MXN"),
                        ListingStatus.OUT_OF_STOCK, Instant.parse("2026-07-28T17:05:17.625304Z")));
    }

    private Long createStore(StoreReq request) {
        ResponseEntity<StoreResp> response = rest.post().uri(STORES_ENDPOINT).body(request).retrieve()
                .toEntity(StoreResp.class);

        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();

        return response.getBody().id();
    }

    private Long createCatalog(String resource, CatalogReq request) {

        ResponseEntity<CatalogResp> response = rest.post().uri(CATALOGS_ENDPOINT + resource).body(request).retrieve()
                .toEntity(CatalogResp.class);

        assertThat(response.getStatusCode()).as("Catalog creation should return HTTP 201").isEqualTo(CREATED);
        CatalogResp body = response.getBody();

        assertThat(body).as("Created catalog response should exist").isNotNull();

        return body.id();
    }

    private FigurineResp createFigurines(FigurineReq request) {
        ResponseEntity<FigurineResp> response = rest.post().uri(FIGURINES_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON).body(request).retrieve().toEntity(FigurineResp.class);

        assertThat(response.getStatusCode()).as("Figurine creation should return HTTP 201").isEqualTo(CREATED);
        FigurineResp body = response.getBody();

        assertThat(body).as("Created figurine response should exist").isNotNull();

        return body;
    }

    private Long createDistributor(com.mesofi.mythclothapi.distributors.dto.DistributorReq request) {
        ResponseEntity<DistributorResp> response = rest.post().uri(DISTRIBUTORS_ENDPOINT).body(request).retrieve()
                .toEntity(DistributorResp.class);

        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();

        return response.getBody().id();
    }

    private List<FigurineStoreUnmatchedResp> readUnMatchedFigurines() {
        ResponseEntity<List<FigurineStoreUnmatchedResp>> response = rest.get().uri(UNMATCHED_LISTINGS_ENDPOINT)
                .retrieve().toEntity(new ParameterizedTypeReference<>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();

        return response.getBody();
    }

    private void matchUnmatchedListingToFigurine(Long unmatchedListingId, Long figurineId) {
        String matchEndpoint = UNMATCHED_LISTINGS_ENDPOINT + "/" + unmatchedListingId + "/figurines/" + figurineId
                + "/match";
        ResponseEntity<Void> response = rest.post().uri(matchEndpoint).retrieve().toEntity(Void.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
    }

    private List<FigurineStoreMatchedResp> readMatchedFigurines(Long storeId) {
        ResponseEntity<List<FigurineStoreMatchedResp>> response = rest.get()
                .uri(MATCHED_LISTINGS_ENDPOINT + "/stores/" + storeId).retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();

        return response.getBody();
    }
}
