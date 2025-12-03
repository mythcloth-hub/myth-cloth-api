package com.mesofi.mythclothapi.distributors;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.mesofi.mythclothapi.distributors.dto.DistributorReq;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.distributors.model.Distributor;

/**
 * MapStruct mapper responsible for converting between distributor-related DTOs ({@link
 * DistributorReq}, {@link DistributorResp}) and the {@link Distributor} domain model.
 *
 * <p>Uses MapStruct's code generation to provide efficient, compile-time-safe mappings. Configured
 * as a Spring component.
 */
@Mapper(componentModel = "spring")
public interface DistributorMapper {

  /**
   * Converts a {@link DistributorReq} into a new {@link Distributor}.
   *
   * <p>The {@code id} and {@code figurines} fields are ignored, as they are either auto-generated
   * or managed through separate logic.
   *
   * @param request the incoming distributor creation/update request
   * @return a new mapped {@link Distributor}
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "figurines", ignore = true)
  Distributor toDistributor(DistributorReq request);

  /**
   * Converts a {@link Distributor} into a {@link DistributorResp}.
   *
   * <p>Extracts the textual description from the distributor name enum to expose a meaningful
   * string in the API response.
   *
   * @param distributorEntity the entity to map
   * @return a mapped {@link DistributorResp}
   */
  @Mapping(
      target = "description",
      expression = "java(distributorEntity.getName().getDescription())")
  DistributorResp toDistributorResp(Distributor distributorEntity);

  /**
   * Updates an existing {@link Distributor} using the non-null fields provided in a {@link
   * DistributorReq}.
   *
   * <p>Null properties in the request are ignored to avoid overwriting existing values. The {@code
   * id} and {@code figurines} fields are not modified.
   *
   * @param request the request containing updated distributor information
   * @param entity the existing entity to apply updates to
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "figurines", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateDistributor(DistributorReq request, @MappingTarget Distributor entity);
}
