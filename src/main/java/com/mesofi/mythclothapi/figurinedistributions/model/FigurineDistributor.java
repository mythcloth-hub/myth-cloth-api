package com.mesofi.mythclothapi.figurinedistributions.model;

import java.time.LocalDate;

import com.mesofi.mythclothapi.common.BaseId;
import com.mesofi.mythclothapi.distributors.model.Distributor;
import com.mesofi.mythclothapi.figurines.model.Figurine;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class FigurineDistributor extends BaseId {

  // Many figurines → many distributions

  @ManyToOne(optional = false)
  private Figurine figurine;

  @ManyToOne(optional = false)
  private Distributor distributor;

  // Many figurines → many distributions

  @Column(nullable = false)
  private CurrencyCode currency; // MXN, JPY, USD, etc

  private Double price; // Price for that distributor

  private LocalDate announcementDate;
  private LocalDate preorderDate;
  private LocalDate releaseDate;

  @Column(nullable = false)
  private boolean releaseDateConfirmed;
}
