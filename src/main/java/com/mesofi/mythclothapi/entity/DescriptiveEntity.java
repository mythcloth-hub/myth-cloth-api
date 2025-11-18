package com.mesofi.mythclothapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * An abstract base class for entities that require a description field in addition to a unique
 * identifier.
 *
 * <p>This class extends {@link BaseIdEntity} to inherit the primary key functionality and adds a
 * non-nullable description column with a maximum length of 100 characters.
 *
 * <p>Annotated with {@code @MappedSuperclass} so that subclasses annotated with {@code @Entity}
 * inherit this field mapping.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class DescriptiveEntity extends BaseIdEntity {

  /**
   * A brief description of the entity.
   *
   * <p>Annotated with {@code @Column(nullable = false, length = 100)} to define the constraints at
   * the database level, ensuring the field is never null and adheres to the specified maximum
   * length.
   */
  @Column(nullable = false, length = 100)
  private String description;
}
