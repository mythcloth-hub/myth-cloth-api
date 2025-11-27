package com.mesofi.mythclothapi.references.repository;

import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.references.entity.GroupEntity;

@Repository("groups")
public interface GroupRepository extends IdDescPairRepository<GroupEntity, Long> {}
