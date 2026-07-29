package com.mesofi.mythclothapi.stores.dto;

import java.net.URI;
import java.util.Currency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.mesofi.mythclothapi.messaging.pricing.model.StoreName;

public record StoreReq(
        @NotBlank(message = "name must not be blank") @Size(max = 150, message = "store name must not exceed 150 characters") String name,
        @NotNull(message = "store name must not be null") StoreName storeName, @NotNull URI website,
        @NotNull URI logoUrl, @NotNull Currency currency,
        @NotBlank(message = "country must not be blank") @Size(max = 100) String country, boolean active) {
}
