package com.mesofi.mythclothapi.figurines.mapper;

import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.CNY;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.JPY;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.MXN;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mesofi.mythclothapi.catalogs.model.Anniversary;
import com.mesofi.mythclothapi.catalogs.model.Distribution;
import com.mesofi.mythclothapi.catalogs.model.Group;
import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.catalogs.model.Series;
import com.mesofi.mythclothapi.common.BaseId;
import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.distributors.model.Distributor;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurines.dto.DistributorInfo;
import com.mesofi.mythclothapi.figurines.dto.FigurineReq;
import com.mesofi.mythclothapi.figurines.model.Figurine;

/**
 * MapStruct mapper responsible for converting between CSV/API input models and internal {@link
 * Figurine} entities. It also resolves catalog-based relationships (Series, LineUp, Distribution,
 * Group, Anniversary, etc.) using a provided {@link CatalogContext}.
 *
 * <p>This mapper supports:
 *
 * <ul>
 *   <li>CSV → Figurine transformation ({@link FigurineCsv}).
 *   <li>API → Figurine transformation ({@link FigurineReq}).
 *   <li>API DistributorInfo → FigurineDistributor mapping.
 *   <li>Lookup helpers for catalog-based relationships.
 *   <li>Special logic for country-specific distributor generation (JP / MX).
 * </ul>
 *
 * <p>Most ID/description resolution helpers use {@link Optional} to return {@code null} when the
 * input is missing, and throw a descriptive {@link IllegalArgumentException} when no catalog match
 * is found.
 */
@Mapper(componentModel = "spring")
public interface FigurineMapper {

  /* ============================
    CSV → Figurine
  ============================ */
  /**
   * Converts a {@link FigurineCsv} row into a {@link Figurine} entity. Resolves related catalog
   * entities (distribution, lineup, series, etc.) and handles initialization of distributor entries
   * from raw CSV columns.
   *
   * @param csv the CSV input row
   * @param catalogs catalogs used to resolve references
   * @return a new {@link Figurine} instance
   */
  @Mapping(target = "id", ignore = true) // populated by DB
  @Mapping(target = "legacyName", source = "originalName")
  @Mapping(target = "normalizedName", source = "baseName")
  @Mapping(
      target = "distributors",
      expression = "java(toDistributors(csv, catalogs.distributors()))")
  @Mapping(target = "distribution", source = "distributionString")
  @Mapping(target = "lineup", source = "lineupString")
  @Mapping(target = "series", source = "seriesString")
  @Mapping(target = "group", source = "groupString")
  @Mapping(target = "anniversary", source = "anniversaryNumber")
  Figurine toFigurine(FigurineCsv csv, @Context CatalogContext catalogs);

  /* ============================
  API → Figurine
  ============================ */
  /**
   * Converts an incoming API request ({@link FigurineReq}) into a fully resolved {@link Figurine}
   * entity.
   *
   * @param req API request object
   * @param catalogs catalogs used to resolve relationships by ID
   * @return a new {@link Figurine}
   */
  @Mapping(target = "id", ignore = true) // populated by DB
  @Mapping(target = "legacyName", ignore = true) // no needed
  @Mapping(target = "normalizedName", source = "name")
  @Mapping(target = "distributors", source = "distributors")
  @Mapping(target = "distribution", source = "distributionId")
  @Mapping(target = "lineup", source = "lineUpId")
  @Mapping(target = "series", source = "seriesId")
  @Mapping(target = "group", source = "groupId")
  @Mapping(target = "anniversary", source = "anniversaryId")
  Figurine toFigurine(FigurineReq req, @Context CatalogContext catalogs);

  /* ============================
  API DistributorInfo → FigurineDistributor
  ============================ */
  /**
   * Maps a high-level API {@link DistributorInfo} wrapper into a {@link FigurineDistributor}
   * entity. The figurine relation is intentionally ignored; it is set later by the service layer.
   *
   * @param info distributor info sent from API
   * @param catalogs catalogs used to resolve the distributor entity
   * @return a new {@link FigurineDistributor}
   */
  @Mapping(target = "id", ignore = true) // populated by DB
  @Mapping(target = "figurine", ignore = true) // will be set later in service
  @Mapping(target = "distributor", source = "distributorId")
  FigurineDistributor toDistributor(DistributorInfo info, @Context CatalogContext catalogs);

  /** Resolves a {@link Distribution} by its ID. Returns {@code null} when the ID is missing. */
  default Distribution toDistribution(Long id, @Context CatalogContext catalogs) {
    String msg = "Distribution not found for id=" + id;
    return Optional.ofNullable(id)
        .map($ -> find(catalogs.distributions(), l -> Objects.equals(l.getId(), id), msg))
        .orElse(null);
  }

  /**
   * Resolves a {@link Distribution} by its description. Returns {@code null} for blank/empty
   * descriptions.
   */
  default Distribution toDistribution(String desc, @Context CatalogContext catalogs) {
    String msg = "Distribution not found for desc=" + desc;
    return Optional.ofNullable(desc)
        .filter($ -> !desc.isBlank())
        .map($ -> find(catalogs.distributions(), l -> desc.equals(l.getDescription()), msg))
        .orElse(null);
  }

  /** Resolves a {@link LineUp} by its ID. */
  default LineUp toLineUp(Long id, @Context CatalogContext catalogs) {
    String msg = "LineUp not found for id=" + id;
    return Optional.ofNullable(id)
        .map($ -> find(catalogs.lineUps(), l -> Objects.equals(l.getId(), id), msg))
        .orElse(null);
  }

  /** Resolves a {@link LineUp} by its description. */
  default LineUp toLineUp(String desc, @Context CatalogContext catalogs) {
    String msg = "LineUp not found for desc=" + desc;
    return Optional.ofNullable(desc)
        .filter($ -> !desc.isBlank())
        .map($ -> find(catalogs.lineUps(), l -> desc.equals(l.getDescription()), msg))
        .orElse(null);
  }

  /** Resolves a {@link Series} by its ID. */
  default Series toSeries(Long id, @Context CatalogContext catalogs) {
    String msg = "Series not found for id=" + id;
    return Optional.ofNullable(id)
        .map($ -> find(catalogs.series(), l -> Objects.equals(l.getId(), id), msg))
        .orElse(null);
  }

  /** Resolves a {@link Series} by its description. */
  default Series toSeries(String desc, @Context CatalogContext catalogs) {
    String msg = "Series not found for desc=" + desc;
    return Optional.ofNullable(desc)
        .filter($ -> !desc.isBlank())
        .map($ -> find(catalogs.series(), l -> desc.equals(l.getDescription()), msg))
        .orElse(null);
  }

  /** Resolves a {@link Group} by its ID. */
  default Group toGroup(Long id, @Context CatalogContext catalogs) {
    String msg = "Group not found for id=" + id;
    return Optional.ofNullable(id)
        .map($ -> find(catalogs.groups(), l -> Objects.equals(l.getId(), id), msg))
        .orElse(null);
  }

  /** Resolves a {@link Group} by its description. */
  default Group toGroup(String desc, @Context CatalogContext catalogs) {
    String msg = "Group not found for desc=" + desc;
    return Optional.ofNullable(desc)
        .filter($ -> !desc.isBlank())
        .map($ -> find(catalogs.groups(), l -> desc.equals(l.getDescription()), msg))
        .orElse(null);
  }

  /** Resolves an {@link Anniversary} by its ID. */
  default Anniversary toAnniversary(Long id, @Context CatalogContext catalogs) {
    String msg = "Anniversary not found for id=" + id;
    return Optional.ofNullable(id)
        .map($ -> find(catalogs.anniversaries(), l -> Objects.equals(l.getId(), id), msg))
        .orElse(null);
  }

  /** Resolves an {@link Anniversary} by its numeric year label (e.g., 10th anniversary = 10). */
  default Anniversary toAnniversary(Integer anniversaryNumber, @Context CatalogContext catalogs) {
    return catalogs.anniversaries().stream()
        .filter(item -> Objects.nonNull(item.getYear()))
        .filter(item -> item.getYear().equals(anniversaryNumber))
        .findFirst()
        .orElse(null);
  }

  /** Resolves a {@link Distributor} by its ID. */
  default Distributor mapDistributorId(Long id, @Context CatalogContext catalogs) {
    String msg = "Distributor not found for id=" + id;
    return Optional.ofNullable(id)
        .map($ -> find(catalogs.distributors(), l -> Objects.equals(l.getId(), id), msg))
        .orElse(null);
  }

  /**
   * Creates the list of distributor entries (JP, MX, etc.) from raw CSV pricing/release fields.
   *
   * <p>This method constructs multiple {@link FigurineDistributor} instances depending on available
   * market-specific pricing data:
   *
   * <ul>
   *   <li>JP/CNY distributor (always created)
   *   <li>MX distributor (created only when MX price is present)
   * </ul>
   *
   * @param csv the raw CSV record
   * @param distributors the master list of distributors used for lookups
   * @return the list of resolved {@link FigurineDistributor} entries
   */
  default List<FigurineDistributor> toDistributors(
      FigurineCsv csv, @Context List<Distributor> distributors) {

    List<FigurineDistributor> distributorList = new ArrayList<>();
    FigurineDistributor jp = new FigurineDistributor();
    Optional<LocalDateConfirmed> opt = Optional.ofNullable(csv.getReleaseJPY());

    // In case the figurine has been released in China or Japan
    jp.setCurrency(csv.isHk() ? CNY : JPY);
    jp.setPrice(csv.getPriceJPY());
    jp.setAnnouncementDate(csv.getAnnouncementJPY());
    jp.setPreorderDate(csv.getPreorderJPY());
    jp.setReleaseDate(opt.map(LocalDateConfirmed::getDate).orElse(null));
    jp.setReleaseDateConfirmed(opt.map(LocalDateConfirmed::isConfirmed).orElse(false));
    jp.setDistributor(findDistributorByCountry(distributors, CountryCode.JP));
    distributorList.add(jp);

    Optional.ofNullable(csv.getPriceMXN())
        .ifPresent(
            price -> {
              FigurineDistributor mx = new FigurineDistributor();
              mx.setCurrency(MXN);
              mx.setPrice(price);
              mx.setPreorderDate(csv.getPreorderMXN());
              mx.setReleaseDate(csv.getReleaseMXN());
              mx.setDistributor(findDistributorByCountry(distributors, CountryCode.MX));
              distributorList.add(mx);
            });

    return distributorList;
  }

  /**
   * Finds a distributor by country code.
   *
   * @param distributors all available distributors
   * @param countryCode the country code to match
   * @return matching distributor
   * @throws IllegalArgumentException when no distributor matches
   */
  private Distributor findDistributorByCountry(
      List<Distributor> distributors, CountryCode countryCode) {
    return distributors.stream()
        .filter(d -> d.getCountry() == countryCode)
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Distributor not found for code='" + countryCode + "'"));
  }

  /**
   * Generic helper to find a catalog entity in a list.
   *
   * @param list the catalog list
   * @param predicate selection predicate
   * @param errorMessage error thrown when not found
   * @return the matched entity
   */
  private <T extends BaseId> T find(List<T> list, Predicate<T> predicate, String errorMessage) {
    return list.stream()
        .filter(predicate)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(errorMessage));
  }
}
