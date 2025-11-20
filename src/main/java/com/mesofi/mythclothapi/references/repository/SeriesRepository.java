package com.mesofi.mythclothapi.references.repository;

import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.references.entity.SeriesEntity;

@Repository("series")
public interface SeriesRepository extends IdDescPairRepository<SeriesEntity, Long> {}
