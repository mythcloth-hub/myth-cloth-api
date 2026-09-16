package com.mesofi.mythclothapi.collectorspurchases.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine;
import com.mesofi.mythclothapi.collectorspurchases.CollectorPurchase;
import com.mesofi.mythclothapi.common.Auditable;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a figurine that is part of a collector's purchase, linking the
 * purchase to a specific figurine in the collector's collection. It includes
 * details such as quantity, price paid, and the type of purchase.
 */
@Entity
@Getter
@Setter
@Table(name = "collector_purchase_figurines", uniqueConstraints = @UniqueConstraint(name = "uk_purchase_collection_figurine", columnNames = {
        "purchase_id", "collection_figurine_id"}))
public class CollectorPurchaseFigurine extends Auditable {

    /**
     * The purchase associated with this figurine. This is a mandatory relationship,
     * and the purchase is fetched lazily to optimize performance.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_id", nullable = false)
    private CollectorPurchase purchase;

    /**
     * The figurine in the collector's collection associated with this purchase.
     * This is a mandatory relationship, and the figurine is fetched lazily to
     * optimize performance.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_figurine_id", nullable = false)
    private CollectorCollectionFigurine collectionFigurine;

    /**
     * The quantity of this figurine purchased.
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * The price paid for this figurine.
     */
    @Column(precision = 12, scale = 2)
    private BigDecimal pricePaid;

    /**
     * The type of purchase for this figurine (e.g., retail, pre-order,
     * second-hand).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PurchaseType purchaseType;

}
