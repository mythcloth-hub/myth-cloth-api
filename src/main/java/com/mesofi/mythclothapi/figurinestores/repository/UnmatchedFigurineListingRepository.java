package com.mesofi.mythclothapi.figurinestores.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.figurinestores.model.UnmatchedFigurineListing;

@Repository
public interface UnmatchedFigurineListingRepository extends JpaRepository<UnmatchedFigurineListing, Long> {
}
