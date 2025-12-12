package com.mesofi.mythclothapi.catalogs.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnniversaryResp extends CatalogResp {
  private int year;
}
