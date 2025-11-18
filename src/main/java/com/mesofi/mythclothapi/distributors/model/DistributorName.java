package com.mesofi.mythclothapi.distributors.model;

import lombok.Getter;

@Getter
public enum DistributorName {
  BANDAI("Tamashii Nations"),
  DAM("Distribuidora Animéxico"),
  DTM("Distribuidora Toyvision México"),

  DS_DISTRIBUTIONS("DS Distribuciones"),
  BLUE_FIN("Bluefin");

  private final String description;

  DistributorName(String description) {
    this.description = description;
  }
}
