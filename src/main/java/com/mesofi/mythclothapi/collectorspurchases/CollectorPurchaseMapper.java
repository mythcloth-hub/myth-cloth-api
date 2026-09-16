package com.mesofi.mythclothapi.collectorspurchases;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CollectorPurchaseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "orderDate", source = "purchaseDate")
    CollectorPurchase toCollectorPurchase(CollectorPurchaseReq request);

}
