package com.mesofi.mythclothapi.anniversaries;

import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.catalogs.repository.IdDescRepository;

@Repository("anniversaries")
public interface AnniversaryRepository extends IdDescRepository<Anniversary, Long> {}
