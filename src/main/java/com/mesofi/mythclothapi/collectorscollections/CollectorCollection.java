package com.mesofi.mythclothapi.collectorscollections;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.common.Descriptive;
import com.mesofi.mythclothapi.figurines.model.Figurine;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class CollectorCollection extends Descriptive {

  @ManyToOne(optional = false)
  private Collector collector;

  @ManyToOne(optional = false)
  private Figurine figurine;
}
