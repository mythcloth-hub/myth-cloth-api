package com.mesofi.mythclothapi.references.entity;

import java.util.ArrayList;
import java.util.List;

import com.mesofi.mythclothapi.entity.DescriptiveEntity;
import com.mesofi.mythclothapi.figurines.FigurineEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "lineups")
public class LineUpEntity extends DescriptiveEntity {

  @OneToMany(mappedBy = "lineup", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FigurineEntity> figurines = new ArrayList<>();
}
