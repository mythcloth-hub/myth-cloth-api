package com.mesofi.mythclothapi.messaging.pricing.model;

import java.net.URI;

/**
 * Identifies the external stores supported by the market crawler.
 * <p>
 * Each enum constant represents a retailer that can be crawled to retrieve
 * product information such as pricing, availability, and product details. In
 * addition to uniquely identifying the store, each constant provides the base
 * website URI used by crawler implementations.
 */
public enum StoreName {

    /**
     * Nin-Nin-Game online store.
     */
    NIN_NIN_GAME("https://www.nin-nin-game.com"),

    /**
     * Mandarake online store.
     */
    MANDARAKE("https://order.mandarake.co.jp"),

    /**
     * Luna Park online store.
     */
    LUNA_PARK("https://www.lunapark.store"),

    /**
     * My Kombini online store.
     */
    MY_KOMBINI("https://mykombini.com"),

    /**
     * Myth Supplies online store.
     */
    MYTH_SUPPLIES("https://mythsupplies.com");

    /**
     * Base website URI of the store.
     */
    private final URI website;

    /**
     * Creates a store identifier associated with its website.
     *
     * @param website
     *            the base website URI of the store
     */
    StoreName(String website) {
        this.website = URI.create(website);
    }

    /**
     * Returns the base website URI of the store.
     *
     * @return the store's website URI
     */
    public URI website() {
        return website;
    }
}
