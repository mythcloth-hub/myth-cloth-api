package com.mesofi.mythclothapi.figurinestores.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.mesofi.mythclothapi.common.BaseId;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.messaging.pricing.model.LineUP;
import com.mesofi.mythclothapi.stores.model.Store;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "figurine_stores", uniqueConstraints = {
        @UniqueConstraint(name = "uk_figurine_store_pair", columnNames = {"figurine_id", "store_id"})})
public class FigurineStore extends BaseId {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Figurine figurine;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Store store;

    @OneToMany(mappedBy = "figurineStore", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FigurineStorePricing> prices = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "line_up")
    private LineUP lineUP;

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
