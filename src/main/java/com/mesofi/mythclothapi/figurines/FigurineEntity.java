package com.mesofi.mythclothapi.figurines;

import java.util.ArrayList;
import java.util.List;

import com.mesofi.mythclothapi.catalogs.distributions.DistributionEntity;
import com.mesofi.mythclothapi.catalogs.groups.GroupEntity;
import com.mesofi.mythclothapi.catalogs.lineups.LineUpEntity;
import com.mesofi.mythclothapi.catalogs.series.SeriesEntity;
import com.mesofi.mythclothapi.entity.BaseIdEntity;
import com.mesofi.mythclothapi.entity.FigurineDistributor;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "figurines",
    indexes = @Index(name = "idx_figurine_unique_name", columnList = "uniqueName"))
public class FigurineEntity extends BaseIdEntity {

  @Column(unique = true, nullable = false)
  private String uniqueName;

  private String normalizedName;

  @OneToMany(mappedBy = "figurine", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FigurineDistributor> distributors = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY)
  private DistributionEntity distribution;

  @ManyToOne(fetch = FetchType.LAZY)
  private LineUpEntity lineup;

  @ManyToOne(fetch = FetchType.LAZY)
  private SeriesEntity series;

  @ManyToOne(fetch = FetchType.LAZY)
  private GroupEntity groups;
}
