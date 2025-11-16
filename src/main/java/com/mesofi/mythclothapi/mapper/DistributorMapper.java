package com.mesofi.mythclothapi.mapper;

import com.mesofi.mythclothapi.entity.Distributor;
import com.mesofi.mythclothapi.model.DistributorRequest;
import com.mesofi.mythclothapi.model.DistributorResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DistributorMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "figurines", ignore = true)
  Distributor toDistributorEntity(DistributorRequest request);

  @Mapping(target = "name", expression = "java(distributor.getName().getDescription())")
  DistributorResponse toDistributorResponse(Distributor distributor);
}
