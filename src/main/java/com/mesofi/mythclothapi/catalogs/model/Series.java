package com.mesofi.mythclothapi.catalogs.model;

import java.util.ArrayList;
import java.util.List;

import com.mesofi.mythclothapi.common.Descriptive;
import com.mesofi.mythclothapi.figurines.model.Figurine;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "series")
public class Series extends Descriptive {

  @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Figurine> figurines = new ArrayList<>();
}
