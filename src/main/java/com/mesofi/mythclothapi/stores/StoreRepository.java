package com.mesofi.mythclothapi.stores;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.stores.model.Store;

/**
 * Repository for managing {@link Store} entities.
 * <p>
 * Provides CRUD operations through {@link JpaRepository} together with
 * store-specific query methods.
 */
@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    /**
     * Retrieves all active stores ordered alphabetically by name.
     *
     * @return the list of active stores ordered by name
     */
    List<Store> findAllByActiveTrueOrderByNameAsc();

    /**
     * Retrieves an active store by its identifier.
     *
     * @param storeId
     *            the store identifier
     * @return an {@link Optional} containing the active store if found;
     *         {@link Optional#empty()} otherwise
     */
    Optional<Store> findByIdAndActiveTrue(Long storeId);
}
