package com.mesofi.mythclothapi.collectorscollections.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.mesofi.mythclothapi.collectorscollections.CollectorCollection;
import com.mesofi.mythclothapi.common.Auditable;
import com.mesofi.mythclothapi.figurines.model.Figurine;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an item in a collector's collection, linking a specific figurine
 * to the collection with additional details such as quantity, ownership status,
 * condition, and the date it was added.
 */
@Entity
@Getter
@Setter
@Table(name = "collector_collection_items", uniqueConstraints = @UniqueConstraint(name = "uk_collection_figurine", columnNames = {
        "collection_id", "figurine_id"}))
public class CollectorCollectionItem extends Auditable {

    /**
     * The collector's collection to which this item belongs. This is a mandatory
     * relationship, and the collection is fetched lazily to optimize performance.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private CollectorCollection collection;

    /**
     * The figurine associated with this collection item. This is a mandatory
     * relationship, and the figurine is fetched lazily to optimize performance.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Figurine figurine;

    /**
     * The quantity of this figurine in the collector's collection. This field is
     * mandatory and must be a non-negative integer.
     */
    @Column(nullable = false, comment = "The quantity of this figurine in the collector collection")
    private int quantity;

    /**
     * Indicates whether the collector owns this figurine. This field is mandatory
     * and defaults to false if not specified.
     */
    @Column(nullable = false, comment = "Indicates whether the collector owns this figurine")
    private boolean owned;

    /**
     * The condition of the figurine in the collector's collection. This field is
     * mandatory and uses a string representation of the Condition enum.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, comment = "The condition of the figurine in the collector collection")
    private Condition condition;

    /**
     * The date and time when this item was added to the collector's collection.
     * This field is automatically set when the entity is persisted and is not
     * updatable thereafter.
     */
    @Setter(AccessLevel.NONE)
    @Column(nullable = false, updatable = false, comment = "The date and time when this item was added to the collector collection")
    private Instant addedAt;

    /**
     * Automatically sets the addedAt timestamp to the current instant when the
     * entity is persisted for the first time.
     */
    @PrePersist
    protected void onCreate() {
        addedAt = Instant.now();
    }
}
