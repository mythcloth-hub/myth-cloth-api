package com.mesofi.mythclothapi.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * An abstract base class for entities that require a primary key identifier.
 *
 * <p>This class is annotated with {@code @MappedSuperclass}, which allows its persistence fields to
 * be inherited by all subclasses annotated with {@code @Entity}. It automatically provides a
 * generated ID field managed by the persistence provider.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseIdEntity {

  /**
   * The unique identifier for the entity.
   *
   * <p>Annotated with {@code @Id} to mark it as the primary key of the database table. Annotated
   * with {@code @GeneratedValue} using strategy {@code GenerationType.IDENTITY} to indicate that
   * the persistence provider must assign primary keys for the entity using a database identity
   * column.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
}
