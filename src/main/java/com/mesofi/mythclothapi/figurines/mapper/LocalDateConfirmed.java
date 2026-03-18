package com.mesofi.mythclothapi.figurines.mapper;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LocalDateConfirmed {
  private LocalDate date;
  private boolean confirmed;
}
