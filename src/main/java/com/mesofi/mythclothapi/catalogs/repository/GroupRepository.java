package com.mesofi.mythclothapi.catalogs.repository;

import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.catalogs.model.Group;

@Repository("groups")
public interface GroupRepository extends IdDescRepository<Group, Long> {}
