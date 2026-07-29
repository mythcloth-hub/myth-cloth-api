package com.mesofi.mythclothapi.stores;

import java.net.URI;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mesofi.mythclothapi.stores.dto.StoreReq;
import com.mesofi.mythclothapi.stores.dto.StoreResp;
import com.mesofi.mythclothapi.stores.model.Store;

@Mapper(componentModel = "spring")
public interface StoreMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", source = "storeName")
    @Mapping(target = "figurines", ignore = true)
    Store toStore(StoreReq storeReq);

    @Mapping(target = "storeName", source = "code")
    StoreResp toStoreResp(Store store);

    default String map(URI value) {
        return URI.create(value.toString()).toString();
    }

}
