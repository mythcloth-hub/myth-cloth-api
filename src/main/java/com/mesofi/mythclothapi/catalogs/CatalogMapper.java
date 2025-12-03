package com.mesofi.mythclothapi.catalogs;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mesofi.mythclothapi.catalogs.dto.CatalogReq;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.catalogs.model.Anniversary;
import com.mesofi.mythclothapi.catalogs.model.Distribution;
import com.mesofi.mythclothapi.catalogs.model.Group;
import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.catalogs.model.Series;
import com.mesofi.mythclothapi.common.Descriptive;

/**
 * Mapper responsible for converting between {@link CatalogReq} DTOs and the various reference
 * catalog entities (Group, Series, LineUp, Distribution), as well as mapping from {@link
 * Descriptive} objects to {@link CatalogResp}.
 *
 * <p>This mapper uses MapStruct to generate the implementation at compile time and is registered as
 * a Spring component.
 */
@Mapper(componentModel = "spring")
public interface CatalogMapper {

  /**
   * Converts a {@link CatalogReq} into a {@link Group}.
   *
   * <p>The {@code id} field is ignored because a new entity is being created. The {@code figurines}
   * relationship is ignored to avoid accidental cascade or unwanted associations during creation.
   *
   * @param request the request DTO containing the group data
   * @return a new {@link Group} populated from the request
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "figurines", ignore = true)
  Group toGroup(CatalogReq request);

  /**
   * Converts a {@link CatalogReq} into a {@link Series}.
   *
   * <p>The {@code id} is ignored to allow the database to generate it. The {@code figurines} list
   * is ignored since this mapper only handles catalog-level attributes.
   *
   * @param request the request DTO containing the series data
   * @return a new {@link Series}
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "figurines", ignore = true)
  Series toSeries(CatalogReq request);

  /**
   * Converts a {@link CatalogReq} into a {@link LineUp}.
   *
   * <p>The {@code id} and {@code figurines} fields are intentionally ignored to prevent overwriting
   * existing data or creating unwanted links.
   *
   * @param request the request DTO containing the lineup data
   * @return a new {@link LineUp}
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "figurines", ignore = true)
  LineUp toLineUp(CatalogReq request);

  /**
   * Converts a {@link CatalogReq} into a {@link Distribution}.
   *
   * <p>The {@code id} and {@code figurines} collections are ignored so that only catalog attributes
   * from the request are mapped.
   *
   * @param request the request DTO containing the distribution data
   * @return a new {@link Distribution}
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "figurines", ignore = true)
  Distribution toDistribution(CatalogReq request);

  /**
   * Converts a {@link CatalogReq} into a {@link Anniversary}.
   *
   * <p>The {@code id} and {@code figurines} collections are ignored so that only catalog attributes
   * from the request are mapped.
   *
   * @param request the request DTO containing the anniversary data
   * @return a new {@link Anniversary}
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "figurines", ignore = true)
  Anniversary toAnniversary(CatalogReq request);

  /**
   * Converts a {@link Descriptive} into a {@link CatalogResp}.
   *
   * <p>This is used for returning catalog entities in a standard {id, description} response format.
   *
   * @param descriptiveEntity a catalog entity implementing {@link Descriptive}
   * @return a {@link CatalogResp} DTO
   */
  CatalogResp toCatalogResp(Descriptive descriptiveEntity);
}
