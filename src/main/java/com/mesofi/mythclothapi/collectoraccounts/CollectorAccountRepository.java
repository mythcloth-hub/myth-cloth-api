package com.mesofi.mythclothapi.collectoraccounts;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing {@link CollectorAccount} persistence and lookups.
 */
@Repository
public interface CollectorAccountRepository extends JpaRepository<CollectorAccount, Long> {

    Optional<CollectorAccount> findByEmail(String email);
}
