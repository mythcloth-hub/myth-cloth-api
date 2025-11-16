package com.mesofi.mythclothapi.repository;

import com.mesofi.mythclothapi.entity.Distributor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DistributorRepository extends JpaRepository<Distributor, Long> {}
