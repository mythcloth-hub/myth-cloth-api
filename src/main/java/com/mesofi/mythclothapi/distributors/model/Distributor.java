package com.mesofi.mythclothapi.distributors.model;

import java.util.ArrayList;
import java.util.List;

import com.mesofi.mythclothapi.common.BaseId;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "distributors",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_distributor_name_country",
            columnNames = {"name", "country"}))
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Distributor extends BaseId {

  @Column(nullable = false, length = 100)
  @Enumerated(EnumType.STRING)
  private DistributorName name;

  @Column(nullable = false, length = 10)
  @Enumerated(EnumType.STRING)
  private CountryCode country;

  @Column(length = 100)
  private String website;

  // FigurineDistributor.distributor
  @OneToMany(mappedBy = "distributor", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FigurineDistributor> figurines = new ArrayList<>();
}
