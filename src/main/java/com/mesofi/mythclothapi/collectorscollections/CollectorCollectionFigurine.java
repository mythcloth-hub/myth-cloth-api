package com.mesofi.mythclothapi.collectorscollections;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.mesofi.mythclothapi.common.BaseId;
import com.mesofi.mythclothapi.figurines.model.Figurine;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "collector_collection_figurines",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_collection_figurine_pair",
            columnNames = {"collection_id", "figurine_id"}))
public class CollectorCollectionFigurine extends BaseId {

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private CollectorCollection collection;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Figurine figurine;

  private int totalFigurines = 1;
}
