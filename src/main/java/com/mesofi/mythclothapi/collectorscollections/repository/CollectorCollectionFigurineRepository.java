package com.mesofi.mythclothapi.collectorscollections.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.collectorscollections.CollectorCollection;
import com.mesofi.mythclothapi.collectorscollections.CollectorCollectionFigurine;
import com.mesofi.mythclothapi.figurines.model.Figurine;

@Repository
public interface CollectorCollectionFigurineRepository
    extends JpaRepository<CollectorCollectionFigurine, Long> {

  Optional<CollectorCollectionFigurine> findByCollectionAndFigurine(
      CollectorCollection collection, Figurine figurine);
}
