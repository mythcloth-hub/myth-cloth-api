package com.mesofi.mythclothapi.figurines;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FigurineRepository extends JpaRepository<FigurineEntity, Long> {
  Optional<FigurineRepository> findByUniqueName(String uniqueName);
}
