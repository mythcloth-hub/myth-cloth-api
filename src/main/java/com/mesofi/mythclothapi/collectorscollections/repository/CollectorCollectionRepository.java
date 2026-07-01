package com.mesofi.mythclothapi.collectorscollections.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.collectorscollections.CollectorCollection;

@Repository
public interface CollectorCollectionRepository extends JpaRepository<CollectorCollection, Long> {
  List<CollectorCollection> findByCollector(Collector collector);

  Optional<CollectorCollection> findByName(String name);

  @Modifying
  @Query("DELETE FROM CollectorCollection cc WHERE cc.id = :id")
  void deleteCollectionById(Long id);
}
