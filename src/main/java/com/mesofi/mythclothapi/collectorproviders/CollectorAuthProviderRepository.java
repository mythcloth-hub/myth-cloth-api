package com.mesofi.mythclothapi.collectorproviders;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.collectorproviders.model.CollectorAuthProvider;
import com.mesofi.mythclothapi.collectorproviders.model.ProviderType;

/** Repository for managing {@link CollectorAuthProvider} persistence and lookups. */
@Repository
public interface CollectorAuthProviderRepository
    extends JpaRepository<CollectorAuthProvider, Long> {

  /**
   * Finds a provider link by provider type and provider-specific user id.
   *
   * @param providerType authentication provider type
   * @param subject provider-specific user identifier (subject)
   * @return matching provider link when it exists
   */
  Optional<CollectorAuthProvider> findByProviderAndProviderUserId(
      ProviderType providerType, String subject);
}
