package com.mesofi.mythclothapi.collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for managing {@link Collector} persistence operations. */
@Repository
public interface CollectorRepository extends JpaRepository<Collector, Long> {}
