package com.mesofi.mythclothapi.references.repository;

import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.references.entity.DistributionEntity;

@Repository("distributions")
public interface DistributionRepository extends IdDescPairRepository<DistributionEntity, Long> {}
