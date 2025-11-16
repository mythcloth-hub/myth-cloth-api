package com.mesofi.mythclothapi.distributors;

import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.distributors.model.DistributorName;
import com.mesofi.mythclothapi.entity.FigurineDistributor;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_distributor_name_country",
            columnNames = {"name", "country"}))
public class DistributorEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private DistributorName name;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private CountryCode country;

  private String website;

  @OneToMany(mappedBy = "distributor", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FigurineDistributor> figurines = new ArrayList<>();
}
