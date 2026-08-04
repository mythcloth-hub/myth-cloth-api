package com.mesofi.mythclothapi.figurinestores.model;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.mesofi.mythclothapi.catalogs.model.LineUpType;
import com.mesofi.mythclothapi.common.Auditable;
import com.mesofi.mythclothapi.messaging.pricing.model.ListingStatus;
import com.mesofi.mythclothapi.stores.model.Store;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "figurine_stores_unmatched", indexes = {
        @Index(name = "idx_store_original_name", columnList = "store_id, original_name")}, uniqueConstraints = {
                @UniqueConstraint(name = "uk_store_original_name", columnNames = {"store_id", "original_name"})})
public class FigurineStoreUnmatched extends Auditable {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Store store;

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

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal discount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status;

    @Column(nullable = false)
    private boolean preorder;

    @Column(nullable = false)
    private Instant checkedAt;

    @Column(nullable = false)
    private boolean ignored;
}
