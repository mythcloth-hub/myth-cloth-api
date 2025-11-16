package com.mesofi.mythclothapi.distributors;

import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.distributors.model.DistributorName;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing {@link DistributorEntity} persistence operations.
 *
 * <p>Extends {@link JpaRepository} to provide basic CRUD functionality and adds custom query
 * methods for application-specific needs.
 */
public interface DistributorRepository extends JpaRepository<DistributorEntity, Long> {
  /**
   * Checks whether a distributor exists with the given name and country.
   *
   * @param name the distributor's name to check
   * @param country the country associated with the distributor
   * @return {@code true} if a distributor exists with the provided name and country, {@code false}
   *     otherwise
   */
  boolean existsByNameAndCountry(DistributorName name, CountryCode country);
}
