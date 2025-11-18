package com.mesofi.mythclothapi.distribution;

import java.util.ArrayList;
import java.util.List;

import com.mesofi.mythclothapi.entity.DescriptiveEntity;
import com.mesofi.mythclothapi.entity.Figurine;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "distributions")
public class DistributionEntity extends DescriptiveEntity {

  @OneToMany(mappedBy = "distribution", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Figurine> figurines = new ArrayList<>();
}
