package com.mesofi.mythclothapi.figurines.model;

import java.util.ArrayList;
import java.util.List;

import com.mesofi.mythclothapi.catalogs.model.Anniversary;
import com.mesofi.mythclothapi.catalogs.model.Distribution;
import com.mesofi.mythclothapi.catalogs.model.Group;
import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.catalogs.model.Series;
import com.mesofi.mythclothapi.common.BaseId;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "figurines",
    indexes = @Index(name = "idx_figurine_unique_name", columnList = "legacyName"))
public class Figurine extends BaseId {

  @Column(unique = true, length = 200)
  private String legacyName;

  @Column(nullable = false, length = 100)
  private String normalizedName;

  // FigurineDistributor.figurine
  @OneToMany(mappedBy = "figurine", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FigurineDistributor> distributors = new ArrayList<>();

  @Column(length = 50)
  private String tamashiiUrl;

  @ManyToOne(fetch = FetchType.LAZY)
  private Distribution distribution;

  @ManyToOne(fetch = FetchType.LAZY)
  private LineUp lineup;

  @ManyToOne(fetch = FetchType.LAZY)
  private Series series;

  @ManyToOne(fetch = FetchType.LAZY)
  private Group group;

  @ManyToOne(fetch = FetchType.LAZY)
  private Anniversary anniversary;

  @Column(name = "is_metal_body")
  private Boolean metalBody;

  @Column(name = "is_oce")
  private Boolean oce;

  @Column(name = "is_revival")
  private Boolean revival;

  @Column(name = "is_plain_cloth")
  private Boolean plainCloth;

  @Column(name = "is_broken")
  private Boolean broken;

  @Column(name = "is_golden")
  private Boolean golden;

  @Column(name = "is_gold")
  private Boolean gold;

  @Column(name = "is_manga")
  private Boolean manga;

  @Column(name = "is_surplice")
  private Boolean surplice;

  @Column(name = "is_set")
  private Boolean set;

  @Column(name = "is_articulable")
  private Boolean articulable;

  @Column(length = 800)
  private String remarks;

  @ElementCollection
  @CollectionTable(name = "official_images", joinColumns = @JoinColumn(name = "figurine_id"))
  private List<String> officialImages;

  @ElementCollection
  @CollectionTable(name = "non_official_images", joinColumns = @JoinColumn(name = "figurine_id"))
  private List<String> nonOfficialImages;
}
