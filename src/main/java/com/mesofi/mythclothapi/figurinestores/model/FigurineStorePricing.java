package com.mesofi.mythclothapi.figurinestores.model;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import com.mesofi.mythclothapi.common.BaseId;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "figurine_store_pricings")
public class FigurineStorePricing extends BaseId {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private FigurineStore figurineStore;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal currentPrice;

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
