package com.mesofi.mythclothapi.figurines;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.figurines.model.Figurine;

import lombok.NonNull;

/** Repository for {@link Figurine} persistence and read operations. */
@Repository
public interface FigurineRepository extends JpaRepository<Figurine, Long> {

  Page<Figurine> findAll(@NonNull Pageable pageable);

  /**
   * Finds a figurine by legacy name.
   *
   * @param legacyName figurine legacy name
   * @return matching figurine, or empty if no match exists
   */
  Optional<Figurine> findByLegacyName(String legacyName);
}
