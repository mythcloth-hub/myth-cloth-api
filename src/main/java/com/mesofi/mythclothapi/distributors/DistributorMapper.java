package com.mesofi.mythclothapi.distributors;

import com.mesofi.mythclothapi.distributors.model.DistributorRequest;
import com.mesofi.mythclothapi.distributors.model.DistributorResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DistributorMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "figurines", ignore = true)
  DistributorEntity toDistributorEntity(DistributorRequest request);

  @Mapping(target = "name", expression = "java(distributorEntity.getName().getDescription())")
  DistributorResponse toDistributorResponse(DistributorEntity distributorEntity);
}
