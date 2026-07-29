package com.mesofi.mythclothapi.stores.dto;

public record StoreResp(long id, String name, String storeName, String website, String logoUrl, String currency,
        String country, boolean active) {

}
