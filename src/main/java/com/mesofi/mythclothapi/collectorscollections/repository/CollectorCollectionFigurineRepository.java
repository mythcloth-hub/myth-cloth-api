package com.mesofi.mythclothapi.collectorscollections.repository;

import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.collectorscollections.CollectorCollection;
import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine;
import com.mesofi.mythclothapi.figurines.model.Figurine;

@Repository
public interface CollectorCollectionFigurineRepository
    extends JpaRepository<CollectorCollectionFigurine, Long> {

  Optional<CollectorCollectionFigurine> findByCollectionAndFigurine(
      CollectorCollection collection, Figurine figurine);

  @Modifying
  @Transactional
  @Query(
      """
        delete from CollectorCollectionFigurine ccf
        where ccf.collection.id = :collectionId
          and ccf.collection.collector.id = :collectorId
    """)
  int deleteByCollectionIdAndCollectorId(
      @Param("collectionId") Long collectionId, @Param("collectorId") Long collectorId);
}
