package com.mesofi.mythclothapi.collectorproviders;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectorAuthProviderRepository
    extends JpaRepository<CollectorAuthProvider, Long> {

  Optional<CollectorAuthProvider> findByProviderAndProviderUserId(
      ProviderType providerType, String subject);
}
