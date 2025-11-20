package com.mesofi.mythclothapi.references;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mesofi.mythclothapi.entity.DescriptiveEntity;
import com.mesofi.mythclothapi.references.entity.DistributionEntity;
import com.mesofi.mythclothapi.references.entity.GroupEntity;
import com.mesofi.mythclothapi.references.entity.LineUpEntity;
import com.mesofi.mythclothapi.references.entity.SeriesEntity;
import com.mesofi.mythclothapi.references.model.ReferencePairRequest;
import com.mesofi.mythclothapi.references.model.ReferencePairResponse;

/**
 * Mapper responsible for converting between {@link ReferencePairRequest} DTOs and the various
 * reference catalog entities (Group, Series, LineUp, Distribution), as well as mapping from {@link
 * DescriptiveEntity} objects to {@link ReferencePairResponse}.
 *
 * <p>This mapper uses MapStruct to generate the implementation at compile time and is registered as
 * a Spring component.
 */
@Mapper(componentModel = "spring")
public interface ReferencePairMapper {

  /**
   * Converts a {@link ReferencePairRequest} into a {@link GroupEntity}.
   *
   * <p>The {@code id} field is ignored because a new entity is being created. The {@code figurines}
   * relationship is ignored to avoid accidental cascade or unwanted associations during creation.
   *
   * @param request the request DTO containing the group data
   * @return a new {@link GroupEntity} populated from the request
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "figurines", ignore = true)
  GroupEntity toGroupEntity(ReferencePairRequest request);

  /**
   * Converts a {@link ReferencePairRequest} into a {@link SeriesEntity}.
   *
   * <p>The {@code id} is ignored to allow the database to generate it. The {@code figurines} list
   * is ignored since this mapper only handles catalog-level attributes.
   *
   * @param request the request DTO containing the series data
   * @return a new {@link SeriesEntity}
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "figurines", ignore = true)
  SeriesEntity toSeriesEntity(ReferencePairRequest request);

  /**
   * Converts a {@link ReferencePairRequest} into a {@link LineUpEntity}.
   *
   * <p>The {@code id} and {@code figurines} fields are intentionally ignored to prevent overwriting
   * existing data or creating unwanted links.
   *
   * @param request the request DTO containing the lineup data
   * @return a new {@link LineUpEntity}
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "figurines", ignore = true)
  LineUpEntity toLineUpEntity(ReferencePairRequest request);

  /**
   * Converts a {@link ReferencePairRequest} into a {@link DistributionEntity}.
   *
   * <p>The {@code id} and {@code figurines} collections are ignored so that only catalog attributes
   * from the request are mapped.
   *
   * @param request the request DTO containing the distribution data
   * @return a new {@link DistributionEntity}
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "figurines", ignore = true)
  DistributionEntity toDistributionEntity(ReferencePairRequest request);

  /**
   * Converts a {@link DescriptiveEntity} into a {@link ReferencePairResponse}.
   *
   * <p>This is used for returning catalog entities in a standard {id, description} response format.
   *
   * @param descriptiveEntity a catalog entity implementing {@link DescriptiveEntity}
   * @return a {@link ReferencePairResponse} DTO
   */
  ReferencePairResponse toCatalogResponse(DescriptiveEntity descriptiveEntity);
}
