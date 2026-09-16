package com.mesofi.mythclothapi.collectorspurchases;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorPurchaseResp(Long purchaseId, String seller) {

}
