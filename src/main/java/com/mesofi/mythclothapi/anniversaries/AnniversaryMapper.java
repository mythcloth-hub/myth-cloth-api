package com.mesofi.mythclothapi.anniversaries;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryReq;
import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryResp;

@Mapper(componentModel = "spring")
public interface AnniversaryMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "figurines", ignore = true)
  Anniversary toAnniversary(AnniversaryReq request);

  AnniversaryResp toAnniversaryResp(Anniversary anniversary);
}
