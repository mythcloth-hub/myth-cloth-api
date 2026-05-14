package com.mesofi.mythclothapi.figurines.mapper;

import com.mesofi.mythclothapi.anniversaries.model.AnniversaryType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnniversaryNumberType {
  private AnniversaryType anniversaryType;
  private int anniversaryNumber;
}
