package com.mesofi.mythclothapi.figurineevents;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mesofi.mythclothapi.figurineevents.dto.FigurineEventReq;
import com.mesofi.mythclothapi.figurineevents.dto.FigurineEventResp;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEvent;

@Mapper(componentModel = "spring")
public interface FigurineEventMapper {

  @Mapping(target = "id", ignore = true) // populated by DB
  @Mapping(target = "eventDate", source = "date")
  @Mapping(target = "eventDateConfirmed", source = "dateConfirmed")
  @Mapping(target = "figurine", ignore = true) // populate later in the service
  FigurineEvent toFigurineEvent(FigurineEventReq request);

  @Mapping(target = "date", source = "eventDate")
  @Mapping(target = "dateConfirmed", source = "eventDateConfirmed")
  FigurineEventResp toFigurineEventResp(FigurineEvent figurineEvent);

  // @Mapping(target = "id", ignore = true)
  // @Mapping(target = "figurines", ignore = true)
  // @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  // void updateDistributor(DistributorReq request, @MappingTarget Distributor entity);
}
