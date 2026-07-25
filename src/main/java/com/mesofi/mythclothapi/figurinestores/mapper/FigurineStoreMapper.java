package com.mesofi.mythclothapi.figurinestores.mapper;

import java.util.List;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreMatchedResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreMatchedSummaryResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreUnmatchedResp;
import com.mesofi.mythclothapi.figurinestores.model.FigurineStore;
import com.mesofi.mythclothapi.figurinestores.model.UnmatchedFigurineListing;
import com.mesofi.mythclothapi.stores.model.Store;

@Mapper(componentModel = "spring")
public interface FigurineStoreMapper {

    @Mapping(target = "storeId", source = "store.id")
    @Mapping(target = "storeWebsite", source = "store.url")
    FigurineStoreUnmatchedResp toFigurineStoreUnmatchedResp(UnmatchedFigurineListing unmatchedFigurineListing);

    @Mapping(target = "storeId", source = "store.id")
    @Mapping(target = "storeWebsite", source = "store.url")
    @Mapping(target = "currency", source = "store.currency")
    @Mapping(target = "matchedFigurineCount", source = "totalFigurines")
    FigurineStoreMatchedSummaryResp toFigurineStoreMatchedSummaryResp(Store store, long totalFigurines);

    @Mapping(target = "id", source = "figurineStore.id")
    @Mapping(target = "figurineId", source = "figurineStore.figurine.id")
    @Mapping(target = "figurineDisplayableName", expression = "java(displayableName)")
    @Mapping(target = "figurineOfficialImageUrl", source = "figurineStore.figurine.officialImages", qualifiedByName = "firstImage")
    @Mapping(target = "figurineTamashiiUrl", source = "figurineStore.figurine.tamashiiUrl")
    // @Mapping(target = "lineup", source = "figurineStore.figurine.lineup")
    @Mapping(target = "storeId", source = "figurineStore.store.id")
    @Mapping(target = "storeOriginalName", source = "figurineStore.originalName")
    @Mapping(target = "storeProductImageUrl", source = "figurineStore.imageUrl")
    @Mapping(target = "storeProductUrl", source = "figurineStore.productUrl")
    FigurineStoreMatchedResp toFigurineStoreMatchedResp(FigurineStore figurineStore, @Context String displayableName);

    @Named("firstImage")
    default String firstImage(List<String> images) {
        return images == null || images.isEmpty() ? null : images.getFirst();
    }
}
