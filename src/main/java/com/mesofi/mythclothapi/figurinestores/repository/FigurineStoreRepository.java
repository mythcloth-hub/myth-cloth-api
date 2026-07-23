package com.mesofi.mythclothapi.figurinestores.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurinestores.model.FigurineStore;
import com.mesofi.mythclothapi.stores.model.Store;

@Repository
public interface FigurineStoreRepository extends JpaRepository<FigurineStore, Long> {
    Optional<FigurineStore> findByFigurineAndStore(Figurine figurine, Store store);
}
