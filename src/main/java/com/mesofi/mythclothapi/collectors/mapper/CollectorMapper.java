package com.mesofi.mythclothapi.collectors.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mesofi.mythclothapi.collectorscollections.CollectorCollection;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionResp;
import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine;
import com.mesofi.mythclothapi.common.BaseId;

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
}
