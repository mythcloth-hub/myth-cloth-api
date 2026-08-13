package com.mesofi.mythclothapi.stores;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mesofi.mythclothapi.messaging.pricing.model.StoreName;
import com.mesofi.mythclothapi.stores.dto.StoreReq;
import com.mesofi.mythclothapi.stores.dto.StoreResp;
import com.mesofi.mythclothapi.stores.model.Store;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @InjectMocks
    private StoreService service;

    @Mock
    private StoreRepository repository;
    @Mock
    private StoreMapper mapper;

    @Test
    void createStore_shouldMapSaveAndReturnResponse() {
        StoreReq request = createRequest("Nin-Nin-Game", StoreName.NIN_NIN_GAME, "JP", true);
        Store mapped = store(1L, "Nin-Nin-Game", "NIN_NIN_GAME", true);
        Store saved = store(1L, "Nin-Nin-Game", "NIN_NIN_GAME", true);
        StoreResp response = createResponse(1L, "Nin-Nin-Game", "NIN_NIN_GAME", true);

        when(mapper.toStore(request)).thenReturn(mapped);
        when(repository.save(mapped)).thenReturn(saved);
        when(mapper.toStoreResp(saved)).thenReturn(response);

        StoreResp result = service.createStore(request);

        assertThat(result).isEqualTo(response);
        verify(mapper).toStore(request);
        verify(repository).save(mapped);
        verify(mapper).toStoreResp(saved);
    }

    @Test
    void retrieveStore_shouldReturnMappedResponse_whenStoreExists() {
        Store store = store(7L, "Myth Factory", "MYTH_FACTORY", true);
        StoreResp response = createResponse(7L, "Myth Factory", "MYTH_FACTORY", true);

        when(repository.findById(7L)).thenReturn(Optional.of(store));
        when(mapper.toStoreResp(store)).thenReturn(response);

        StoreResp result = service.retrieveStore(7L);

        assertThat(result).isEqualTo(response);
        verify(repository).findById(7L);
        verify(mapper).toStoreResp(store);
    }

    @Test
    void retrieveStore_shouldThrowWhenStoreIsMissing() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.retrieveStore(9L)).isInstanceOf(StoreNotFoundException.class)
                .hasMessageContaining("Store with id 9 was not found");

        verify(repository).findById(9L);
        verify(mapper, never()).toStoreResp(any());
    }

    @Test
    void retrieveStores_shouldReturnMappedActiveStoresInOrder() {
        Store first = store(1L, "A Store", "A_STORE", true);
        Store second = store(2L, "B Store", "B_STORE", true);
        StoreResp firstResp = createResponse(1L, "A Store", "A_STORE", true);
        StoreResp secondResp = createResponse(2L, "B Store", "B_STORE", true);

        when(repository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of(first, second));
        when(mapper.toStoreResp(first)).thenReturn(firstResp);
        when(mapper.toStoreResp(second)).thenReturn(secondResp);

        List<StoreResp> result = service.retrieveStores();

        assertThat(result).containsExactly(firstResp, secondResp);
        verify(repository).findAllByActiveTrueOrderByNameAsc();
        verify(mapper).toStoreResp(first);
        verify(mapper).toStoreResp(second);
    }

    @Test
    void retrieveStores_shouldReturnEmptyListWhenNoStoresExist() {
        when(repository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of());

        List<StoreResp> result = service.retrieveStores();

        assertThat(result).isEmpty();
        verify(repository).findAllByActiveTrueOrderByNameAsc();
    }

    @Test
    void updateStore_shouldUpdateExistingStoreAndReturnMappedResponse() {
        StoreReq request = createRequest("Luna Park", StoreName.LUNA_PARK, "JP", false);
        Store existing = store(3L, "Old Name", "OLD_CODE", true);
        Store incoming = store(null, "Luna Park", "LUNA_PARK", false);
        StoreResp response = createResponse(3L, "Luna Park", "LUNA_PARK", false);

        when(repository.findById(3L)).thenReturn(Optional.of(existing));
        when(mapper.toStore(request)).thenReturn(incoming);
        when(mapper.toStoreResp(existing)).thenReturn(response);

        StoreResp result = service.updateStore(3L, request);

        assertThat(result).isEqualTo(response);
        verify(repository).findById(3L);
        verify(mapper).toStore(request);
        verify(mapper).updateStore(existing, incoming);
        verify(mapper).toStoreResp(existing);
    }

    @Test
    void updateStore_shouldThrowWhenStoreIsMissing() {
        StoreReq request = createRequest("Luna Park", StoreName.LUNA_PARK, "JP", false);
        when(repository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStore(3L, request)).isInstanceOf(StoreNotFoundException.class)
                .hasMessageContaining("Store with id 3 was not found");

        verify(repository).findById(3L);
        verify(mapper, never()).updateStore(any(), any());
    }

    @Test
    void deactivateStore_shouldSetActiveFalseOnExistingStore() {
        Store existing = store(9L, "Myth Factory", "MYTH_FACTORY", true);
        when(repository.findById(9L)).thenReturn(Optional.of(existing));

        service.deactivateStore(9L);

        assertThat(existing.isActive()).isFalse();
        verify(repository).findById(9L);
    }

    @Test
    void deactivateStore_shouldThrowWhenStoreIsMissing() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deactivateStore(9L)).isInstanceOf(StoreNotFoundException.class)
                .hasMessageContaining("Store with id 9 was not found");

        verify(repository).findById(9L);
    }

    private StoreReq createRequest(String name, StoreName storeName, String country, boolean active) {
        return new StoreReq(name, storeName, URI.create("https://example.com/" + storeName.name().toLowerCase()),
                URI.create("https://example.com/" + storeName.name().toLowerCase() + ".png"),
                Currency.getInstance("MXN"), country, active);
    }

    private StoreResp createResponse(long id, String name, String storeName, boolean active) {
        return new StoreResp(id, name, storeName, "https://example.com/" + storeName.toLowerCase(),
                "https://example.com/" + storeName.toLowerCase() + ".png", Currency.getInstance("MXN").toString(), "JP",
                active);
    }

    private Store store(Long id, String name, String code, boolean active) {
        Store store = new Store();
        store.setId(id);
        store.setName(name);
        store.setCode(code);
        store.setWebsite("https://example.com/" + code.toLowerCase());
        store.setLogoUrl("https://example.com/" + code.toLowerCase() + ".png");
        store.setCurrency("MXN");
        store.setCountry("JP");
        store.setActive(active);
        return store;
    }
}
