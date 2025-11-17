package com.mesofi.mythclothapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.entity.Figurine;

@Repository
public interface FigurineRepository extends JpaRepository<Figurine, Long> {
  Optional<Figurine> findByUniqueName(String uniqueName);
}
