package com.mesofi.mythclothapi.figurinestores.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreUnmatchedResp;
import com.mesofi.mythclothapi.figurinestores.model.UnmatchedFigurineListing;

@Mapper(componentModel = "spring")
public interface FigurineStoreMapper {

    @Mapping(target = "storeId", source = "store.id")
    @Mapping(target = "storeWebsite", source = "store.url")
    FigurineStoreUnmatchedResp toFigurineStoreUnmatchedResp(UnmatchedFigurineListing unmatchedFigurineListing);

}
