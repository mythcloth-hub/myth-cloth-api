package com.mesofi.mythclothapi.references.repository;

import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.references.entity.LineUpEntity;

@Repository("lineups")
public interface LineUpRepository extends IdDescPairRepository<LineUpEntity, Long> {}
