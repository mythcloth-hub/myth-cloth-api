package com.mesofi.mythclothapi.collectorspurchases.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.common.BaseId;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "collector_purchases")
public class CollectorPurchase extends BaseId {

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Collector collector;

  private LocalDate orderDate;

  private String store;

  private String orderNumber;

  private CurrencyCode currency; // MXN, JPY, USD, etc

  private BigDecimal totalAmount;

  private Integer totalFigurines;

  @Enumerated(EnumType.STRING)
  private ShippingStatus shippingStatus;

  private String trackingNumber;

  private String carrier;

  private LocalDate shippedDate;

  private LocalDate deliveredDate;
}
