package com.mesofi.mythclothapi.figurinestores.mapper;

import java.util.List;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreMatchedResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreMatchedSummaryResp;
import com.mesofi.mythclothapi.figurinestores.dto.FigurineStoreUnmatchedResp;
import com.mesofi.mythclothapi.figurinestores.model.CachedStores;
import com.mesofi.mythclothapi.figurinestores.model.FigurineStore;
import com.mesofi.mythclothapi.figurinestores.model.UnmatchedFigurineListing;
import com.mesofi.mythclothapi.stores.model.Store;

@Mapper(componentModel = "spring")
public interface FigurineStoreMapper {

    @Mapping(target = "storeId", source = "store.id")
    @Mapping(target = "storeWebsite", source = "store.website")
    @Mapping(target = "storeLogo", source = "store.logoUrl")
    FigurineStoreUnmatchedResp toFigurineStoreUnmatchedResp(UnmatchedFigurineListing unmatchedFigurineListing);

    @Mapping(target = "storeId", source = "store.id")
    @Mapping(target = "storeName", source = "store.name")
    @Mapping(target = "storeWebsite", source = "store.website")
    @Mapping(target = "storeLogo", source = "store.logoUrl")
    @Mapping(target = "matchedFigurineCount", source = "totalFigurines")
    FigurineStoreMatchedSummaryResp toFigurineStoreMatchedSummaryResp(Store store, long totalFigurines);

    @Mapping(target = "id", source = "figurineStore.id")
    @Mapping(target = "figurineId", source = "figurineStore.figurine.id")
    @Mapping(target = "figurineDisplayableName", expression = "java(displayableName)")
    @Mapping(target = "figurineOfficialImageUrl", source = "figurineStore.figurine.officialImages", qualifiedByName = "firstImage")
    @Mapping(target = "figurineTamashiiUrl", source = "figurineStore.figurine.tamashiiUrl")
    @Mapping(target = "figurineLineUp", source = "figurineStore.figurine.lineup.description")
    @Mapping(target = "storeId", source = "figurineStore.store.id")
    @Mapping(target = "storeOriginalName", source = "figurineStore.originalName")
    @Mapping(target = "storeProductImageUrl", source = "figurineStore.imageUrl")
    @Mapping(target = "storeProductUrl", source = "figurineStore.productUrl")
    FigurineStoreMatchedResp toFigurineStoreMatchedResp(FigurineStore figurineStore, @Context String displayableName);

    CachedStores toStoreCache(Store store);

    @Mapping(target = "name", ignore = true)
    @Mapping(target = "website", ignore = true)
    @Mapping(target = "logoUrl", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "figurines", ignore = true)
    Store toStore(CachedStores cachedStores);

    @Named("firstImage")
    default String firstImage(List<String> images) {
        return images == null || images.isEmpty() ? null : images.getFirst();
    }
}
