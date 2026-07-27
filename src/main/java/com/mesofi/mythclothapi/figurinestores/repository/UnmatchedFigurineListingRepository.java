package com.mesofi.mythclothapi.figurinestores.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.figurinestores.model.UnmatchedFigurineListing;
import com.mesofi.mythclothapi.stores.model.Store;

@Repository
public interface UnmatchedFigurineListingRepository extends JpaRepository<UnmatchedFigurineListing, Long> {

    Optional<UnmatchedFigurineListing> findByStoreAndOriginalName(Store store, String originalName);
}
