package com.mesofi.mythclothapi.collectors;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for managing {@link Collector} persistence operations. */
@Repository
public interface CollectorRepository extends JpaRepository<Collector, Long> {

    /**
     * Finds a collector by their email address.
     *
     * @param email
     *            the email address of the collector
     * @return an {@link Optional} containing the collector if found, or empty if
     *         not found
     */
    Optional<Collector> findByEmail(String email);
}
