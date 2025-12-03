package com.mesofi.mythclothapi.catalogs.repository;

import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.catalogs.model.Series;

@Repository("series")
public interface SeriesRepository extends IdDescRepository<Series, Long> {}
