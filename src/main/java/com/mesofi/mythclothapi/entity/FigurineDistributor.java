package com.mesofi.mythclothapi.entity;

import com.mesofi.mythclothapi.model.CurrencyCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class FigurineDistributor {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  // Many figurines → many distributions
  @ManyToOne(optional = false)
  private Figurine figurine;

  @ManyToOne(optional = false)
  private Distributor distributor;

  private Double price; // Price for that territory
  private CurrencyCode currency; // MXN, JPY, USD, etc
}
