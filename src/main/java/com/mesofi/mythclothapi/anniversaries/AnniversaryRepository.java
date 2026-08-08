package com.mesofi.mythclothapi.anniversaries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.anniversaries.model.Anniversary;

@Repository("anniversaries")
public interface AnniversaryRepository extends JpaRepository<Anniversary, Long> {
}
