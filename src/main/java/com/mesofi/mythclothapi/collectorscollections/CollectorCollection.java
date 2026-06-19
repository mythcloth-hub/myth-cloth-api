package com.mesofi.mythclothapi.collectorscollections;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.common.BaseId;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "collector_collections",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_collection_collector_name",
            columnNames = {"collector_id", "description"}))
public class CollectorCollection extends BaseId {

  @Column(nullable = false, length = 200)
  private String name;

  @Column(length = 200)
  private String description;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Collector collector;

  @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CollectorCollectionFigurine> figurines = new ArrayList<>();
}
