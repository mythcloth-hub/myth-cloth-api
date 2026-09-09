package com.mesofi.mythclothapi.collectorscollections;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionItem;
import com.mesofi.mythclothapi.common.Auditable;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a collection of figurines owned by a collector. Each collection
 * has a name, an optional image URL, a description, and a favorite status. A
 * collection is associated with a specific collector and can contain multiple
 * items (figurines).
 */
@Entity
@Getter
@Setter
@Table(name = "collector_collections")
public class CollectorCollection extends Auditable {

    /**
     * The name of the collection. This field is mandatory and has a maximum length
     * of 200 characters.
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * The URL of the image representing the collection. This field is optional and
     * has a maximum length of 500 characters.
     */
    @Column(length = 500)
    private String imageUrl;

    /**
     * The description of the collection. This field is optional and has a maximum
     * length of 200 characters.
     */
    @Column(length = 200)
    private String description;

    /**
     * Indicates whether the collection is marked as a favorite by the collector.
     */
    private boolean favorite;

    /**
     * The collector who owns this collection. This is a mandatory relationship, and
     * the collector is fetched lazily to optimize performance.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Collector collector;
    /**
     * The list of items (figurines) in this collection. This is a one-to-many
     * relationship, and the items are managed by the collection. Cascade operations
     * are applied to ensure that changes to the collection are reflected in its
     * items, and orphan removal is enabled to delete items that are no longer
     * associated with the collection.
     */
    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollectorCollectionItem> items = new ArrayList<>();
}
