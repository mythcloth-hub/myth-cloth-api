package com.mesofi.mythclothapi.figurinestores.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurinestores.model.FigurineStore;
import com.mesofi.mythclothapi.stores.model.Store;

@Repository
public interface FigurineStoreRepository extends JpaRepository<FigurineStore, Long> {

    List<FigurineStore> findByFigurine(Figurine figurine);

    Optional<FigurineStore> findByFigurineAndStoreAndOriginalName(Figurine figurine, Store store, String originalName);

    List<FigurineStore> findByFigurineAndStore(Figurine figurine, Store store);

    Optional<FigurineStore> findByStoreAndOriginalName(Store store, String originalName);

    long countByStore(Store store);

    List<FigurineStore> findByStoreOrderByOriginalName(Store store);
}
