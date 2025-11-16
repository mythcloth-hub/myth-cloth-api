package com.mesofi.mythclothapi.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(indexes = @Index(name = "idx_figurine_unique_name", columnList = "uniqueName"))
public class Figurine {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String uniqueName;

  private String normalizedName;

  @OneToMany(mappedBy = "figurine", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FigurineDistributor> distributors = new ArrayList<>();
}
