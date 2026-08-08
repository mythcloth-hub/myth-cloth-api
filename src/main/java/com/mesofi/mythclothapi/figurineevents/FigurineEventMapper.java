package com.mesofi.mythclothapi.figurineevents;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mesofi.mythclothapi.figurineevents.dto.FigurineEventReq;
import com.mesofi.mythclothapi.figurineevents.dto.FigurineEventResp;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEvent;

@Mapper(componentModel = "spring")
public interface FigurineEventMapper {

    @Mapping(target = "id", ignore = true) // populated by DB
    @Mapping(target = "details", source = "description")
    @Mapping(target = "eventDate", source = "date")
    @Mapping(target = "eventDateConfirmed", source = "dateConfirmed")
    @Mapping(target = "figurine", ignore = true) // populate later in the service
    @Mapping(target = "creationDate", ignore = true) // populated by DB
    @Mapping(target = "updateDate", ignore = true) // populated by DB
    FigurineEvent toFigurineEvent(FigurineEventReq request);

    @Mapping(target = "date", source = "eventDate")
    @Mapping(target = "dateConfirmed", source = "eventDateConfirmed")
    @Mapping(target = "description", source = "details")
    FigurineEventResp toFigurineEventResp(FigurineEvent figurineEvent);
}
