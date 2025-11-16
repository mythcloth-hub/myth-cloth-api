package com.mesofi.mythclothapi.distributors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DistributorRepository extends JpaRepository<DistributorEntity, Long> {}
