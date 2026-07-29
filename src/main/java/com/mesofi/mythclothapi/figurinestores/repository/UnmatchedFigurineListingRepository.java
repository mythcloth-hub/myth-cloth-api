package com.mesofi.mythclothapi.figurinestores.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.figurinestores.model.FigurineStoreUnmatched;
import com.mesofi.mythclothapi.stores.model.Store;

@Repository
public interface UnmatchedFigurineListingRepository extends JpaRepository<FigurineStoreUnmatched, Long> {

    Optional<FigurineStoreUnmatched> findByStoreAndOriginalNameAndIgnoredFalse(Store store, String originalName);

    Optional<FigurineStoreUnmatched> findByStoreAndOriginalNameAndIgnoredTrue(Store store, String originalName);
}
