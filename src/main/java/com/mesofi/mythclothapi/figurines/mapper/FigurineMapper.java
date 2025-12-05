package com.mesofi.mythclothapi.figurines.mapper;

import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.CNY;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.JPY;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.MXN;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mesofi.mythclothapi.catalogs.model.Anniversary;
import com.mesofi.mythclothapi.catalogs.model.Distribution;
import com.mesofi.mythclothapi.catalogs.model.Group;
import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.catalogs.model.Series;
import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.distributors.model.Distributor;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurines.dto.DistributorInfo;
import com.mesofi.mythclothapi.figurines.dto.FigurineReq;
import com.mesofi.mythclothapi.figurines.model.Figurine;

@Mapper(componentModel = "spring")
public interface FigurineMapper {

  /* -----------------------------
   *   CSV → Figurine
   * ----------------------------- */
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

  /* -----------------------------
   *   API Request → Figurine
   * ----------------------------- */
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

  @Mapping(target = "id", ignore = true) // populated by DB
  @Mapping(target = "figurine", ignore = true) // will be set later in service
  @Mapping(target = "distributor", source = "distributorId")
  FigurineDistributor toDistributor(DistributorInfo info, @Context CatalogContext catalogs);

  /* -----------------------------
   *   LineUp mapping methods
   * ----------------------------- */
  /** Map Long → LineUp */
  default LineUp toLineUp(Long id, @Context CatalogContext catalogs) {
    if (id == null) {
      return null;
    }
    return catalogs.lineUps().stream()
        .filter(l -> Objects.equals(l.getId(), id))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("LineUp not found for id=" + id));
  }

  default Distribution toDistribution(Long id, @Context CatalogContext catalogs) {
    if (id == null) {
      return null;
    }
    return catalogs.distributions().stream()
        .filter(l -> Objects.equals(l.getId(), id))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Distribution not found for id=" + id));
  }

  default Series toSeries(Long id, @Context CatalogContext catalogs) {
    if (id == null) {
      return null;
    }
    return catalogs.series().stream()
        .filter(l -> Objects.equals(l.getId(), id))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Series not found for id=" + id));
  }

  default Group toGroup(Long id, @Context CatalogContext catalogs) {
    if (id == null) {
      return null;
    }
    return catalogs.groups().stream()
        .filter(l -> Objects.equals(l.getId(), id))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Group not found for id=" + id));
  }

  default Anniversary toAnniversary(Long id, @Context CatalogContext catalogs) {
    if (id == null) {
      return null;
    }
    return catalogs.anniversaries().stream()
        .filter(l -> Objects.equals(l.getId(), id))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Anniversary not found for id=" + id));
  }

  default Distribution toDistribution(String description, @Context CatalogContext catalogs) {
    if (description == null || description.isBlank()) {
      return null;
    }
    return catalogs.distributions().stream()
        .filter(l -> description.equalsIgnoreCase(l.getDescription()))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Distribution not found for description='" + description + "'"));
  }

  /** Map String → LineUp */
  default LineUp toLineUp(String description, @Context CatalogContext catalogs) {
    if (description == null || description.isBlank()) {
      return null;
    }
    return catalogs.lineUps().stream()
        .filter(l -> description.equalsIgnoreCase(l.getDescription()))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "LineUp not found for description='" + description + "'"));
  }

  default Series toSeries(String description, @Context CatalogContext catalogs) {
    if (description == null || description.isBlank()) {
      return null;
    }
    return catalogs.series().stream()
        .filter(l -> description.equalsIgnoreCase(l.getDescription()))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Series not found for description='" + description + "'"));
  }

  default Group toGroup(String description, @Context CatalogContext catalogs) {
    if (description == null || description.isBlank()) {
      return null;
    }
    return catalogs.groups().stream()
        .filter(l -> description.equalsIgnoreCase(l.getDescription()))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Group not found for description='" + description + "'"));
  }

  default Anniversary toAnniversary(Integer anniversaryNumber, @Context CatalogContext catalogs) {
    return catalogs.anniversaries().stream()
        .filter(item -> Objects.nonNull(item.getYear()))
        .filter(item -> item.getYear().equals(anniversaryNumber))
        .findFirst()
        .orElse(null);
  }

  default Distributor mapDistributorId(Long id, @Context CatalogContext catalogs) {
    return catalogs.distributors().stream()
        .filter(d -> Objects.equals(d.getId(), id))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Distributor not found for id=" + id));
  }

  default List<FigurineDistributor> toDistributors(
      FigurineCsv csv, @Context List<Distributor> distributors) {

    List<FigurineDistributor> distributorList = new ArrayList<>();
    FigurineDistributor jp = new FigurineDistributor();
    Optional<LocalDateConfirmed> opt = Optional.ofNullable(csv.getReleaseJPY());

    jp.setCurrency(
        csv.isHk() ? CNY : JPY); // In case the figurine has been released in China or Japan
    jp.setPrice(csv.getPriceJPY());
    jp.setAnnouncementDate(csv.getAnnouncementJPY());
    jp.setPreorderDate(csv.getPreorderJPY());
    jp.setReleaseDate(opt.map(LocalDateConfirmed::getDate).orElse(null));
    jp.setReleaseDateConfirmed(opt.map(LocalDateConfirmed::isConfirmed).orElse(false));

    Distributor distributorFound =
        distributors.stream()
            .filter(d -> d.getCountry() == CountryCode.JP)
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Distributor not found for code='" + CountryCode.JP + ""));

    jp.setDistributor(distributorFound);
    distributorList.add(jp);

    Optional.ofNullable(csv.getPriceMXN())
        .ifPresent(
            price -> {
              FigurineDistributor mx = new FigurineDistributor();
              mx.setCurrency(MXN);
              mx.setPrice(price);
              mx.setPreorderDate(csv.getPreorderMXN());
              mx.setReleaseDate(csv.getReleaseMXN());
              mx.setDistributor(
                  distributors.stream()
                      .filter(d -> d.getCountry() == CountryCode.MX)
                      .findFirst()
                      .orElseThrow(
                          () ->
                              new IllegalArgumentException(
                                  "Distributor not found for code='" + CountryCode.MX + "")));

              distributorList.add(mx);
            });

    return distributorList;
  }
}
