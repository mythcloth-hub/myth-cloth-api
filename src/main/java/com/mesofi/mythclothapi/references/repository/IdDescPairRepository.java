package com.mesofi.mythclothapi.references.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@SuppressWarnings("hiding")
@NoRepositoryBean
public interface IdDescPairRepository<T, Long> extends JpaRepository<T, Long> {
  Optional<T> findByDescription(String description);
}
