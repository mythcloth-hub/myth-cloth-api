package com.mesofi.mythclothapi.collectorspurchases.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.collectorspurchases.CollectorPurchase;

@Repository
public interface CollectorPurchaseRepository extends JpaRepository<CollectorPurchase, Long> {

}
