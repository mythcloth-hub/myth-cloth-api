package com.mesofi.mythclothapi.catalogs.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AnniversaryResp extends CatalogResp {
  private int year;

  public AnniversaryResp(long id, String description, int year) {
    super(id, description);
    this.year = year;
  }
}
