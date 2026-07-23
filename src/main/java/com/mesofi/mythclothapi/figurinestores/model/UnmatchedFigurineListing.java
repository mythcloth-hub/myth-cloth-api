package com.mesofi.mythclothapi.figurinestores.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.mesofi.mythclothapi.common.BaseId;
import com.mesofi.mythclothapi.stores.model.Store;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "figurine_stores_unmatched", indexes = {
        @Index(name = "idx_store_normalized_name", columnList = "store_id, normalized_name")}, uniqueConstraints = {
                @UniqueConstraint(name = "uk_store_original_name", columnNames = {"store_id", "original_name"})})
public class UnmatchedFigurineListing extends BaseId {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Store store;

    @Column(nullable = false, length = 300)
    private String originalName;

    @Column(nullable = false, length = 200)
    private String normalizedName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String productUrl;

    @Column(nullable = false)
    private Instant creationDate;

    @Column(nullable = false)
    private Instant updateDate;

    /**
     * Initializes creation and update timestamps before first persistence.
     */
    @PrePersist
    public void prePersist() {
        creationDate = Instant.now();
        updateDate = Instant.now();
    }

    /**
     * Refreshes the update timestamp before entity updates.
     */
    @PreUpdate
    public void preUpdate() {
        updateDate = Instant.now();
    }
}
