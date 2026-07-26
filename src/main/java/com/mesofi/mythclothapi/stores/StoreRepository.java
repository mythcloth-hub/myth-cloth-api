package com.mesofi.mythclothapi.stores;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.stores.model.Store;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findAllByActiveTrueOrderByNameAsc();

    Optional<Store> findByIdAndActiveTrue(Long storeId);

    List<Store> findAllByActiveTrue();
}
