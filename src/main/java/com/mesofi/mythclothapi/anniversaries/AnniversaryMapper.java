package com.mesofi.mythclothapi.anniversaries;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryReq;
import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryResp;
import com.mesofi.mythclothapi.anniversaries.model.Anniversary;

@Mapper(componentModel = "spring")
public interface AnniversaryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "figurines", ignore = true)
    @Mapping(target = "name", source = "description")
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    Anniversary toAnniversary(AnniversaryReq request);

    @Mapping(target = "description", source = "name")
    AnniversaryResp toAnniversaryResp(Anniversary anniversary);
}
