package com.mesofi.mythclothapi.repository;

import com.mesofi.mythclothapi.entity.Figurine;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FigurineRepository extends JpaRepository<Figurine, Long> {
  Optional<Figurine> findByUniqueName(String uniqueName);
}
