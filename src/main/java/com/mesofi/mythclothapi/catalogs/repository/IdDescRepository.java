package com.mesofi.mythclothapi.catalogs.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@SuppressWarnings("hiding")
@NoRepositoryBean
public interface IdDescRepository<T, Long> extends JpaRepository<T, Long> {
  Optional<T> findByDescription(String description);
}
