package com.mesofi.mythclothapi.figurinestores;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreHistoricalPriceResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreHistoricalResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreMatchedResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreMatchedSummaryResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStorePriceResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreUnmatchedResp;
import com.mesofi.mythclothapi.messaging.pricing.model.ListingStatus;
import com.mesofi.mythclothapi.security.config.SecurityConfig;

@Import(SecurityConfig.class)
@WebMvcTest(FigurineStoreController.class)
class FigurineStoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FigurineStoreService figurineStoreService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void retrieveMatchedFigurineListingSummary_shouldReturn200AndSummaries() throws Exception {
        List<FigurineStoreMatchedSummaryResp> response = List.of(new FigurineStoreMatchedSummaryResp(1L, "Nin-Nin-Game",
                "https://nin-nin-game.com", "https://nin-nin-game.com/logo.png", Currency.getInstance("USD"), "JP", 3));

        when(figurineStoreService.retrieveMatchedFigurineListingSummary()).thenReturn(response);

        mockMvc.perform(get("/figurine-stores/matched-listings/summary").with(jwt().authorities(
                new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("figurines:stores:read"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].storeId").value(1))
                .andExpect(jsonPath("$[0].storeName").value("Nin-Nin-Game"))
                .andExpect(jsonPath("$[0].matchedFigurineCount").value(3));

        verify(figurineStoreService).retrieveMatchedFigurineListingSummary();
    }

    @Test
    void retrieveMatchedFigurineListing_shouldReturn200AndList() throws Exception {
        List<FigurineStoreMatchedResp> response = List.of(new FigurineStoreMatchedResp(8L, 42L, "Aries", "MYTH_CLOTH",
                "https://example.com/figurine.jpg", "https://example.com/tamashii", 3L, Currency.getInstance("USD"),
                "Nin-Nin-Game", "https://example.com/store.jpg", "https://example.com/store/aries",
                ListingStatus.IN_STOCK, false, List.of(new FigurineStorePriceResp(new BigDecimal("99.99"), "USD"))));

        when(figurineStoreService.retrieveMatchedFigurineListing(3L)).thenReturn(response);

        mockMvc.perform(get("/figurine-stores/matched-listings/stores/{storeId}", 3L).with(jwt().authorities(
                new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("figurines:stores:read"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(8))
                .andExpect(jsonPath("$[0].figurineDisplayableName").value("Aries"))
                .andExpect(jsonPath("$[0].storePrices[0].realTimePrice").value(99.99));

        verify(figurineStoreService).retrieveMatchedFigurineListing(3L);
    }

    @Test
    void manuallyUnmatchFigurineListing_shouldDelegateToService() throws Exception {
        doNothing().when(figurineStoreService).manuallyUnmatchFigurineListing(7L);

        mockMvc.perform(post("/figurine-stores/matched-listings/figurine-store/{figurineStoreId}", 7L)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("figurines:stores:assign"))))
                .andExpect(status().isOk());

        verify(figurineStoreService).manuallyUnmatchFigurineListing(7L);
    }

    @Test
    void retrieveUnmatchedFigurineListings_shouldReturn200AndList() throws Exception {
        List<FigurineStoreUnmatchedResp> response = List
                .of(new FigurineStoreUnmatchedResp(5L, 2L, "https://example.com/store", "https://example.com/logo.png",
                        "Original Aries", "https://example.com/image.jpg", "https://example.com/product", false));

        when(figurineStoreService.retrieveUnmatchedFigurineListings()).thenReturn(response);

        mockMvc.perform(get("/figurine-stores/unmatched-listings").with(jwt().authorities(
                new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("figurines:stores:read"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].originalName").value("Original Aries"));

        verify(figurineStoreService).retrieveUnmatchedFigurineListings();
    }

    @Test
    void matchUnmatchedListingToFigurine_shouldDelegateToService() throws Exception {
        doNothing().when(figurineStoreService).matchUnmatchedListingToFigurine(11L, 42L);

        mockMvc.perform(
                post("/figurine-stores/unmatched-listings/{unmatchedListingId}/figurines/{figurineId}/match", 11L, 42L)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"),
                                new SimpleGrantedAuthority("figurines:stores:assign"))))
                .andExpect(status().isOk());

        verify(figurineStoreService).matchUnmatchedListingToFigurine(11L, 42L);
    }

    @Test
    void ignoreUnmatchedFigurineListing_shouldDelegateToService() throws Exception {
        doNothing().when(figurineStoreService).ignoreUnmatchedFigurineListing(22L, true);

        mockMvc.perform(patch("/figurine-stores/unmatched-listings/{unmatchedListingId}/ignored/{ignored}", 22L, true)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("figurines:stores:ignore"))))
                .andExpect(status().isOk());

        verify(figurineStoreService).ignoreUnmatchedFigurineListing(22L, true);
    }

    @Test
    void retrieveAverageRealtimePrice_shouldReturnCurrentPrice() throws Exception {
        FigurineStorePriceResp response = new FigurineStorePriceResp(new BigDecimal("51.00"), "USD");

        when(figurineStoreService.retrieveAverageRealtimePrice(42L, Currency.getInstance("USD"))).thenReturn(response);

        mockMvc.perform(get("/figurine-stores/figurines/{figurineId}/prices/current", 42L).param("currency", "USD")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("figurines:stores:read-current-prices"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.realTimePrice").value(51.00))
                .andExpect(jsonPath("$.currency").value("USD"));

        verify(figurineStoreService).retrieveAverageRealtimePrice(42L, Currency.getInstance("USD"));
    }

    @Test
    void retrieveHistoricalPrices_shouldReturnHistoricalDataAcrossAllStores() throws Exception {
        FigurineStoreHistoricalResp response = new FigurineStoreHistoricalResp("Aries", "USD",
                List.of(new FigurineStoreHistoricalPriceResp("Nin-Nin-Game", "https://logo.png", "https://product-page",
                        new BigDecimal("120.00"), Instant.parse("2025-03-11T12:00:00Z"))));

        when(figurineStoreService.retrieveHistoricalPrices(42L, null, Currency.getInstance("USD")))
                .thenReturn(response);

        mockMvc.perform(get("/figurine-stores/figurines/{figurineId}/prices/history", 42L).param("currency", "USD")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("figurines:stores:read-historical-prices"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Aries"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.prices[0].storeName").value("Nin-Nin-Game"));

        verify(figurineStoreService).retrieveHistoricalPrices(42L, null, Currency.getInstance("USD"));
    }

    @Test
    void retrieveHistoricalPrices_shouldReturnFilteredStoreData() throws Exception {
        FigurineStoreHistoricalResp response = new FigurineStoreHistoricalResp("Aries", "JPY",
                List.of(new FigurineStoreHistoricalPriceResp("Nin-Nin-Game", "https://logo.png", "https://product-page",
                        new BigDecimal("15000.00"), Instant.parse("2025-03-11T12:00:00Z"))));

        when(figurineStoreService.retrieveHistoricalPrices(42L, 3L, Currency.getInstance("JPY"))).thenReturn(response);

        mockMvc.perform(get("/figurine-stores/figurines/{figurineId}/prices/history", 42L).param("storeId", "3")
                .param("currency", "JPY")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("figurines:stores:read-historical-prices"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.currency").value("JPY"))
                .andExpect(jsonPath("$.prices[0].price").value(15000.00));

        verify(figurineStoreService).retrieveHistoricalPrices(42L, 3L, Currency.getInstance("JPY"));
    }
}
