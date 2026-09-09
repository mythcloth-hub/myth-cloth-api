package com.mesofi.mythclothapi.collectorscollections.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionItem;

@Repository
public interface CollectorCollectionItemRepository extends JpaRepository<CollectorCollectionItem, Long> {

}
