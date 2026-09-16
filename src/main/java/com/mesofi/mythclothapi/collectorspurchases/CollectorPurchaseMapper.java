package com.mesofi.mythclothapi.collectorspurchases;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CollectorPurchaseMapper {

    CollectorPurchase toCollectorPurchase(CollectorPurchaseReq request);

}
