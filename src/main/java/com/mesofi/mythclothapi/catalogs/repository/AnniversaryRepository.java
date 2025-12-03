package com.mesofi.mythclothapi.catalogs.repository;

import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.catalogs.model.Anniversary;

@Repository("anniversaries")
public interface AnniversaryRepository extends IdDescRepository<Anniversary, Long> {}
