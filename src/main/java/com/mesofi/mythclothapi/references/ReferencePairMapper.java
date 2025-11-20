package com.mesofi.mythclothapi.references;

import org.mapstruct.Mapper;

import com.mesofi.mythclothapi.entity.DescriptiveEntity;
import com.mesofi.mythclothapi.references.entity.DistributionEntity;
import com.mesofi.mythclothapi.references.entity.GroupEntity;
import com.mesofi.mythclothapi.references.entity.LineUpEntity;
import com.mesofi.mythclothapi.references.entity.SeriesEntity;
import com.mesofi.mythclothapi.references.model.ReferencePairRequest;
import com.mesofi.mythclothapi.references.model.ReferencePairResponse;

@Mapper(componentModel = "spring")
public interface ReferencePairMapper {

  GroupEntity toGroupEntity(ReferencePairRequest request);

  SeriesEntity toSeriesEntity(ReferencePairRequest request);

  LineUpEntity toLineUpEntity(ReferencePairRequest request);

  DistributionEntity toDistributionEntityEntity(ReferencePairRequest request);

  ReferencePairResponse toCatalogResponse(DescriptiveEntity descriptiveEntity);
}
