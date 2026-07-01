package com.mesofi.mythclothapi.collectors.mapper;

import java.util.List;
import java.util.function.Function;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mesofi.mythclothapi.collectorscollections.CollectorCollection;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionFigurineDetailResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionFigurineResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionResp;
import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine;
import com.mesofi.mythclothapi.common.BaseId;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;

@Mapper(componentModel = "spring")
public interface CollectorMapper {

  @Mapping(
      target = "totalFigurines",
      expression = "java(collectorCollection.getFigurines().size())")
  @Mapping(target = "figurineIds", expression = "java(getFigurineIds(collectorCollection))")
  CollectorCollectionResp toCollectorCollectionResp(CollectorCollection collectorCollection);

  default List<Long> getFigurineIds(CollectorCollection collection) {
    return collection.getFigurines().stream()
        .map(CollectorCollectionFigurine::getFigurine)
        .map(BaseId::getId)
        .toList();
  }

  @Mapping(target = "name", source = "figurine.normalizedName")
  @Mapping(target = "releaseStatus", source = "releaseStatus")
  @Mapping(target = "notes", source = "figurine.remarks")
  @Mapping(target = "imageUrl", expression = "java(getFirstImage(figurine.getOfficialImages()))")
  @Mapping(target = "isCollected", source = "isCollected")
  @Mapping(target = "ownedQuantity", source = "ownedQuantity")
  @Mapping(target = "year", source = "year")
  CollectorCollectionFigurineResp toCollectorCollectionFigurineResp(
      Figurine figurine,
      ReleaseStatus releaseStatus,
      boolean isCollected,
      int ownedQuantity,
      int year);

  default String getFirstImage(List<String> images) {
    return images == null || images.isEmpty() ? null : images.getFirst();
  }

  @Mapping(target = "displayableName", expression = "java(createDisplayableName.apply(figurine))")
  CollectorCollectionFigurineDetailResp toCollectorCollectionFigurineDetailResp(
      Figurine figurine, @Context Function<Figurine, String> createDisplayableName);
}
