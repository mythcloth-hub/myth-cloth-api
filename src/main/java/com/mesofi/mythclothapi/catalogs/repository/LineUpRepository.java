package com.mesofi.mythclothapi.catalogs.repository;

import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.catalogs.model.LineUp;

@Repository("lineups")
public interface LineUpRepository extends IdDescRepository<LineUp, Long> {}
