package com.mesofi.mythclothapi.figurinestores.model;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.mesofi.mythclothapi.common.Auditable;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "figurine_store_pricings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_figurine_price_pair", columnNames = {"figurine_store_id",
                "current_price"})}, indexes = {
                        @Index(name = "idx_figurine_store_pricings_current_price", columnList = "figurine_store_id, current_price")})
public class FigurineStorePricing extends Auditable {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private FigurineStore figurineStore;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal currentPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal discount;

    @Column(nullable = false)
    private Instant checkedAt;
}
