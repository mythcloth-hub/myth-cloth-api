package com.mesofi.mythclothapi.entity;

import com.mesofi.mythclothapi.distributors.DistributorEntity;
import com.mesofi.mythclothapi.model.CurrencyCode;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class FigurineDistributor {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Many figurines → many distributions
  @ManyToOne(optional = false)
  private Figurine figurine;

  @ManyToOne(optional = false)
  private DistributorEntity distributor;

  private Double price; // Price for that territory
  private CurrencyCode currency; // MXN, JPY, USD, etc
}
