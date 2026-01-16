package com.mesofi.mythclothapi.it;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines catalog selection criteria used to resolve dynamic catalog identifiers during
 * scenario-driven integration tests.
 *
 * <p>This annotation is typically used within a {@link ScenarioRequest} to specify which catalog
 * entities (distribution, line-up, series, group, anniversary) should be created and selected for a
 * test scenario.
 *
 * <p>The selected catalog entries are resolved at runtime and their identifiers are injected into
 * JSON fixtures by replacing placeholders such as {@code {{distributionId}}}, {@code {{lineUpId}}},
 * {@code {{seriesId}}}, {@code {{groupId}}}, and {@code {{anniversaryId}}}.
 *
 * <p>Attributes left empty or set to their default values are ignored during catalog resolution.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CatalogSelector {

  /**
   * Distribution channel associated with the scenario (e.g. "Stores", "Tamashii Web Shop").
   *
   * @return distribution description
   */
  String distribution() default "";

  /**
   * Product line-up associated with the scenario (e.g. "Myth Cloth EX").
   *
   * @return line-up description
   */
  String lineUp();

  /**
   * Series associated with the scenario (e.g. "Saint Seiya").
   *
   * @return series description
   */
  String series();

  /**
   * Product group associated with the scenario (e.g. "Gold Saint").
   *
   * @return group description
   */
  String group();

  /**
   * Anniversary year used to resolve anniversary catalog entries.
   *
   * <p>A value of {@code 0} indicates that no anniversary should be applied.
   *
   * @return anniversary year
   */
  int anniversary() default 0;
}
