package com.mesofi.mythclothapi.distributors;

import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.distributors.model.DistributorName;
import com.mesofi.mythclothapi.entity.FigurineDistributor;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a distributor that sells figurines in a specific country.
 *
 * <p>This entity models a commercial vendor such as DAM (Mexico), DS-Distribuciones (Spain), or
 * similar stores. A distributor is uniquely identified by a combination of its name and the country
 * in which it operates; this is enforced through the unique constraint {@code
 * uk_distributor_name_country}.
 *
 * <p>A distributor can offer many figurines, and the association is represented through the {@link
 * FigurineDistributor} join entity, which contains additional metadata such as price, availability,
 * and localized information.
 *
 * <p>Instances of this class are meant to be persisted using JPA/Hibernate.
 */
@Getter
@Setter
@Entity
@Table(
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_distributor_name_country",
            columnNames = {"name", "country"}))
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DistributorEntity {
  /**
   * Primary key identifier for the distributor.
   *
   * <p>Generated automatically by the underlying database using an identity column.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id;

  /**
   * The commercial name of the distributor.
   *
   * <p>Examples include DAM, DTM, etc. This value is mandatory and cannot be null.
   */
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private DistributorName name;

  /**
   * Country where the distributor operates.
   *
   * <p>Used to differentiate distributors with the same name but operating in different regions
   * (e.g., "Amazon JP" vs. "Amazon US"). This value is a required enum field.
   */
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private CountryCode country;

  /**
   * Optional website URL belonging to the distributor.
   *
   * <p>May be null if no external website is known or applicable.
   */
  private String website;

  /**
   * List of figurines associated with this distributor.
   *
   * <p>This is a one-to-many relationship represented through the {@link FigurineDistributor}
   * entity, which stores additional details such as price, currency, and release batches.
   *
   * <p>All child records are cascaded and removed automatically when the parent distributor is
   * deleted due to {@code cascade = CascadeType.ALL} and {@code orphanRemoval = true}.
   */
  @OneToMany(mappedBy = "distributor", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FigurineDistributor> figurines = new ArrayList<>();
}
