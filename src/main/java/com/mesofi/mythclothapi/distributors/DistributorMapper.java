package com.mesofi.mythclothapi.distributors;

import com.mesofi.mythclothapi.distributors.model.DistributorRequest;
import com.mesofi.mythclothapi.distributors.model.DistributorResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper responsible for converting between distributor-related DTOs ({@link
 * DistributorRequest}, {@link DistributorResponse}) and the {@link DistributorEntity} domain model.
 *
 * <p>Uses MapStruct's code generation to provide efficient, compile-time-safe mappings. Configured
 * as a Spring component.
 */
@Mapper(componentModel = "spring")
public interface DistributorMapper {

  /**
   * Converts a {@link DistributorRequest} into a new {@link DistributorEntity}.
   *
   * <p>The {@code id} and {@code figurines} fields are ignored, as they are either auto-generated
   * or managed through separate logic.
   *
   * @param request the incoming distributor creation/update request
   * @return a new mapped {@link DistributorEntity}
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "figurines", ignore = true)
  DistributorEntity toDistributorEntity(DistributorRequest request);

  /**
   * Converts a {@link DistributorEntity} into a {@link DistributorResponse}.
   *
   * <p>Extracts the textual description from the distributor name enum to expose a meaningful
   * string in the API response.
   *
   * @param distributorEntity the entity to map
   * @return a mapped {@link DistributorResponse}
   */
  @Mapping(target = "name", expression = "java(distributorEntity.getName().getDescription())")
  DistributorResponse toDistributorResponse(DistributorEntity distributorEntity);

  /**
   * Updates an existing {@link DistributorEntity} using the non-null fields provided in a {@link
   * DistributorRequest}.
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
  void updateDistributorEntity(DistributorRequest request, @MappingTarget DistributorEntity entity);
}
