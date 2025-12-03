package com.mesofi.mythclothapi.figurines;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.figurines.model.Figurine;

@Repository
public interface FigurineRepository extends JpaRepository<Figurine, Long> {
  Optional<FigurineRepository> findByLegacyName(String legacyName);
}
