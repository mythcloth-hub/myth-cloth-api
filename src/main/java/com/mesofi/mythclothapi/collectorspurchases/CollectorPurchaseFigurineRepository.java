package com.mesofi.mythclothapi.collectorspurchases;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.collectorspurchases.model.CollectorPurchase;
import com.mesofi.mythclothapi.collectorspurchases.model.CollectorPurchaseFigurine;

@Repository
public interface CollectorPurchaseFigurineRepository
    extends JpaRepository<CollectorPurchaseFigurine, Long> {
  int deleteByPurchaseId(Long purchaseId);

  List<CollectorPurchaseFigurine> findByPurchase(CollectorPurchase purchase);

  List<CollectorPurchaseFigurine> findByPurchaseIdIn(List<Long> purchaseIds);

  List<CollectorPurchaseFigurine> findByPurchaseIdOrderByIdAsc(Long purchaseId);
}
