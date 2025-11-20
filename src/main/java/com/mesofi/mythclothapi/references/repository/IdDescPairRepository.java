package com.mesofi.mythclothapi.references.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@SuppressWarnings("hiding")
@NoRepositoryBean
public interface IdDescPairRepository<T, Long> extends JpaRepository<T, Long> {}
