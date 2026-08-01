package com.mesofi.mythclothapi.stores;

import java.net.URI;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.mesofi.mythclothapi.stores.dto.StoreReq;
import com.mesofi.mythclothapi.stores.dto.StoreResp;
import com.mesofi.mythclothapi.stores.model.Store;

/**
 * Maps between store domain entities and DTOs.
 * <p>
 * Provides conversions for creating, updating, and retrieving {@link Store}
 * instances.
 */
@Mapper(componentModel = "spring")
public interface StoreMapper {

    /**
     * Maps a store creation or update request to a {@link Store} entity.
     *
     * @param storeReq
     *            the request containing the store information
     * @return the mapped store entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", source = "storeName")
    @Mapping(target = "figurines", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    Store toStore(StoreReq storeReq);

    /**
     * Maps a {@link Store} entity to its response representation.
     *
     * @param store
     *            the store entity
     * @return the mapped response
     */
    @Mapping(target = "storeName", source = "code")
    StoreResp toStoreResp(Store store);

    /**
     * Updates an existing {@link Store} entity with values from another
     * {@link Store} instance.
     *
     * @param target
     *            the entity to update
     * @param source
     *            the source containing the updated values
     */
    void updateStore(@MappingTarget Store target, Store source);

    /**
     * Converts a {@link URI} to its string representation.
     * <p>
     * This method is used by MapStruct when mapping {@link URI} properties to
     * {@link String} fields.
     *
     * @param value
     *            the URI to convert
     * @return the URI as a string
     */
    default String map(URI value) {
        return value != null ? value.toString() : null;
    }
}
