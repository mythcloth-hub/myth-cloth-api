package com.mesofi.mythclothapi.distributors.model;

import lombok.Getter;

@Getter
public enum DistributorName {
  DTM("Distribuidora Mexico"),
  DAM("Distribuidora Animéxico"),
  DS_DISTRIBUTIONS("DS Distribuciones"),
  BLUE_FIN("Blue Fin");
  private final String description;

  DistributorName(String description) {
    this.description = description;
  }
}
