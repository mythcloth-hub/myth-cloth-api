package com.mesofi.mythclothapi.figurinestores.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.mesofi.mythclothapi.catalogs.model.LineUpType;
import com.mesofi.mythclothapi.common.Auditable;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.messaging.pricing.model.ListingStatus;
import com.mesofi.mythclothapi.stores.model.Store;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "figurine_stores", uniqueConstraints = {@UniqueConstraint(name = "uk_figurine_store_pair", columnNames = {
        "figurine_id", "store_id", "original_name"})}, indexes = {
                @Index(name = "idx_figurine_stores_store_original_name", columnList = "store_id, original_name")})
public class FigurineStore extends Auditable {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Figurine figurine;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Store store;

    @OneToMany(mappedBy = "figurineStore", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("creationDate ASC")
    private List<FigurineStorePricing> prices = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "line_up")
    private LineUpType lineUp;

    @Column(nullable = false, length = 300)
    private String originalName;

    @Column(nullable = false, length = 200)
    private String normalizedName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String productUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status;

    @Column(nullable = false)
    private boolean preorder;
}
