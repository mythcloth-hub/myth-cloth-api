package com.mesofi.mythclothapi.stores;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
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

import com.mesofi.mythclothapi.messaging.pricing.model.StoreName;
import com.mesofi.mythclothapi.security.config.SecurityConfig;
import com.mesofi.mythclothapi.stores.dto.StoreReq;
import com.mesofi.mythclothapi.stores.dto.StoreResp;

import tools.jackson.databind.ObjectMapper;

@Import(SecurityConfig.class)
@WebMvcTest(value = StoreController.class)
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StoreService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void createStore_shouldReturn201AndLocationHeader() throws Exception {
        StoreReq request = createStoreRequest();
        StoreResp response = createStoreResponse(1L, "Nin-Nin-Game", true);

        when(service.createStore(any(StoreReq.class))).thenReturn(response);

        mockMvc.perform(post("/stores")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("stores:write")))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "http://localhost/stores/1"))
                .andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.name").value("Nin-Nin-Game"))
                .andExpect(jsonPath("$.storeName").value("NIN_NIN_GAME")).andExpect(jsonPath("$.country").value("JP"))
                .andExpect(jsonPath("$.active").value(true));

        verify(service).createStore(any(StoreReq.class));
    }

    @Test
    void retrieveStore_shouldReturn200AndStorePayload() throws Exception {
        StoreResp response = createStoreResponse(7L, "Myth Factory", true);

        when(service.retrieveStore(7L)).thenReturn(response);

        mockMvc.perform(get("/stores/{id}", 7L)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.name").value("Myth Factory"))
                .andExpect(jsonPath("$.storeName").value("MYTH_FACTORY")).andExpect(jsonPath("$.country").value("JP"))
                .andExpect(jsonPath("$.active").value(true));

        verify(service).retrieveStore(7L);
    }

    @Test
    void retrieveStores_shouldReturn200AndList() throws Exception {
        StoreResp first = createStoreResponse(1L, "Nin-Nin-Game", true);
        StoreResp second = createStoreResponse(2L, "Myth Factory", false);

        when(service.retrieveStores()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/stores")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L)).andExpect(jsonPath("$[0].name").value("Nin-Nin-Game"))
                .andExpect(jsonPath("$[1].id").value(2L)).andExpect(jsonPath("$[1].active").value(false));

        verify(service).retrieveStores();
    }

    @Test
    void updateStore_shouldReturn200AndUpdatedPayload() throws Exception {
        StoreReq request = createStoreRequest();
        StoreResp response = createStoreResponse(3L, "Luna Park", false);

        when(service.updateStore(eq(3L), any(StoreReq.class))).thenReturn(response);

        mockMvc.perform(put("/stores/{id}", 3L)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("stores:update")))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.name").value("Luna Park")).andExpect(jsonPath("$.storeName").value("LUNA_PARK"))
                .andExpect(jsonPath("$.active").value(false));

        verify(service).updateStore(eq(3L), any(StoreReq.class));
    }

    @Test
    void deactivateStore_shouldReturn204AndCallService() throws Exception {
        mockMvc.perform(delete("/stores/{id}", 9L).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("stores:delete")))).andExpect(status().isNoContent());

        verify(service).deactivateStore(9L);
    }

    private StoreReq createStoreRequest() {
        return new StoreReq("Nin-Nin-Game", StoreName.NIN_NIN_GAME, URI.create("https://www.nin-nin-game.com"),
                URI.create("https://www.nin-nin-game.com/img/logo.png"), Currency.getInstance("MXN"), "JP", true);
    }

    private StoreResp createStoreResponse(long id, String name, boolean active) {
        String storeName = switch (name) {
            case "Nin-Nin-Game" -> "NIN_NIN_GAME";
            case "Myth Factory" -> "MYTH_FACTORY";
            case "Luna Park" -> "LUNA_PARK";
            default -> throw new IllegalArgumentException("Unsupported test store name: " + name);
        };

        return new StoreResp(id, name, storeName,
                "https://example.com/" + name.toLowerCase().replace(" ", "-") + ".com",
                "https://example.com/" + name.toLowerCase().replace(" ", "-") + ".png",
                Currency.getInstance("MXN").toString(), "JP", active);
    }
}
