package com.mesofi.mythclothapi.catalogs.repository;

import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.catalogs.model.Distribution;

@Repository("distributions")
public interface DistributionRepository extends IdDescRepository<Distribution, Long> {}
