package com.mesofi.mythclothapi.stores;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesofi.mythclothapi.stores.dto.StoreReq;
import com.mesofi.mythclothapi.stores.dto.StoreResp;
import com.mesofi.mythclothapi.stores.model.Store;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for managing {@link Store} entities.
 * <p>
 * Provides operations to create, retrieve, update, and disable stores that
 * supply figurine pricing information.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository repository;
    private final StoreMapper mapper;

    /**
     * Creates a new store.
     *
     * @param request
     *            the store information
     * @return the created store
     */
    @Transactional
    public StoreResp createStore(StoreReq request) {
        log.info("Creating store: {} - {}", request.name(), request.storeName());

        Store store = mapper.toStore(request);

        var saved = repository.save(store);
        return mapper.toStoreResp(saved);
    }

    /**
     * Retrieves a store by its identifier.
     *
     * @param id
     *            the store identifier
     * @return the matching store
     * @throws StoreNotFoundException
     *             if no store exists for the given identifier
     */
    @Transactional(readOnly = true)
    public StoreResp retrieveStore(Long id) {
        log.info("Retrieving store {}", id);

        return repository.findById(id).map(mapper::toStoreResp).orElseThrow(() -> new StoreNotFoundException(id));
    }

    /**
     * Retrieves all active stores ordered by name.
     *
     * @return the list of active stores
     */
    @Transactional(readOnly = true)
    public List<StoreResp> retrieveStores() {
        log.info("Retrieving all existing stores ...");

        return repository.findAllByActiveTrueOrderByNameAsc().stream().map(mapper::toStoreResp).toList();
    }

    /**
     * Updates an existing store.
     *
     * @param id
     *            the identifier of the store to update
     * @param request
     *            the updated store information
     * @return the updated store
     * @throws StoreNotFoundException
     *             if no store exists for the given identifier
     */
    @Transactional
    public StoreResp updateStore(Long id, StoreReq request) {
        log.info("Updating store: {} - {}", request.name(), request.storeName());

        var existing = repository.findById(id).orElseThrow(() -> new StoreNotFoundException(id));

        // Ask MapStruct to update fields
        Store incoming = mapper.toStore(request);
        mapper.updateStore(existing, incoming);

        return mapper.toStoreResp(existing);
    }

    /**
     * Deactivates a store.
     * <p>
     * The store is marked as inactive but is not removed from the database.
     *
     * @param id
     *            the identifier of the store to deactivate
     * @throws StoreNotFoundException
     *             if no store exists for the given identifier
     */
    @Transactional
    public void deactivateStore(Long id) {
        log.warn("Deactivating store {}", id);

        var existing = repository.findById(id).orElseThrow(() -> new StoreNotFoundException(id));

        existing.setActive(false);
    }
}
