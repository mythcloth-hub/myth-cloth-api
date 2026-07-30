package com.mesofi.mythclothapi.messaging.pricing.model;

/**
 * Identifies the external stores supported by the market crawler.
 * <p>
 * Each enum constant uniquely identifies a retailer from which the market
 * crawler can retrieve product information, such as pricing, availability, and
 * product details. These identifiers are used throughout the application to
 * select the appropriate crawler implementation and to associate pricing data
 * with its originating store.
 */
public enum StoreName {

    /**
     * Nin-Nin-Game online store.
     */
    NIN_NIN_GAME,

    /**
     * Mandarake online store.
     */
    MANDARAKE,

    /**
     * Luna Park online store.
     */
    LUNA_PARK,

    /**
     * My Kombini online store.
     */
    MY_KOMBINI,

    /**
     * Myth Supplies online store.
     */
    MYTH_SUPPLIES,

    /**
     * Logan Store online store.
     */
    LOGAN_STORE,

    /**
     * Myth Factory online store.
     */
    MYTH_FACTORY

}
