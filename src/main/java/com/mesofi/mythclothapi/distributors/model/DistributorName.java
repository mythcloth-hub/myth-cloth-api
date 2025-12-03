package com.mesofi.mythclothapi.distributors.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DistributorName {
  BANDAI("Tamashii Nations"),
  DAM("Distribuidora Animéxico"),
  DTM("Distribuidora Toyvision México"),
  BANDAI_CHINA("Tamashii Nations China"),
  DS_DISTRIBUTIONS("DS Distribuciones"),
  BLUE_FIN("Bluefin");

  private final String description;
}
