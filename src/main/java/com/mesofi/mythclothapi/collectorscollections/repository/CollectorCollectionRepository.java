package com.mesofi.mythclothapi.collectorscollections.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.collectorscollections.CollectorCollection;

@Repository
public interface CollectorCollectionRepository extends JpaRepository<CollectorCollection, Long> {}
