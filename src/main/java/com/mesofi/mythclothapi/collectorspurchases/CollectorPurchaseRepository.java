package com.mesofi.mythclothapi.collectorspurchases;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.collectorspurchases.model.CollectorPurchase;

@Repository
public interface CollectorPurchaseRepository extends JpaRepository<CollectorPurchase, Long> {
  Optional<CollectorPurchase> findByIdAndCollectorId(Long id, Long collectorId);

  List<CollectorPurchase> findByCollectorIdOrderByOrderDateDescIdDesc(Long collectorId);
}
