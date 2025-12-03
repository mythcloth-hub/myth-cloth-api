package com.mesofi.mythclothapi.figurines.mapper;

import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.CNY;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.JPY;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.MXN;

import java.time.LocalDate;
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
import com.mesofi.mythclothapi.common.Descriptive;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurines.model.Figurine;

@Mapper(componentModel = "spring")
public interface FigurineMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "legacyName", source = "originalName")
  @Mapping(target = "normalizedName", source = "baseName")
  @Mapping(
      target = "distributors",
      expression =
          "java(createDistributors(csv.getPriceJPY(), csv.getAnnouncementJPY(), csv.getPreorderJPY(), csv.getReleaseJPY(), csv.getPriceMXN(), csv.getPreorderMXN(), csv.getReleaseMXN(), csv.isHk()))")
  Figurine toFigurine(FigurineCsv csv);

  default List<FigurineDistributor> createDistributors(
      Double priceJPY,
      LocalDate announcementJPY,
      LocalDate preorderJPY,
      LocalDateConfirmed releaseJPY,
      Double priceMXN,
      LocalDate preorderMXN,
      LocalDate releaseMXN,
      boolean isHk) {

    List<FigurineDistributor> distributorList = new ArrayList<>();

    FigurineDistributor jp = new FigurineDistributor();
    Optional<LocalDateConfirmed> opt = Optional.ofNullable(releaseJPY);

    jp.setCurrency(isHk ? CNY : JPY); // In case the figurine has been released in China or Japan
    jp.setPrice(priceJPY);
    jp.setAnnouncementDate(announcementJPY);
    jp.setPreorderDate(preorderJPY);
    jp.setReleaseDate(opt.map(LocalDateConfirmed::getDate).orElse(null));
    jp.setReleaseDateConfirmed(opt.map(LocalDateConfirmed::isConfirmed).orElse(false));
    distributorList.add(jp);

    Optional.ofNullable(priceMXN)
        .ifPresent(
            price -> {
              FigurineDistributor mx = new FigurineDistributor();
              mx.setCurrency(MXN);
              mx.setPrice(price);
              mx.setPreorderDate(preorderMXN);
              mx.setReleaseDate(releaseMXN);

              distributorList.add(mx);
            });

    return distributorList;
  }

  default Distribution toDistribution(
      String distributionString, @Context List<Distribution> distributionList) {
    return findByDescription(distributionString, distributionList);
  }

  default LineUp toLineup(String lineupString, @Context List<LineUp> lineUpList) {
    return findByDescription(lineupString, lineUpList);
  }

  default Series toSeries(String seriesString, @Context List<Series> seriesList) {
    return findByDescription(seriesString, seriesList);
  }

  default Group toGroup(String groupString, @Context List<Group> groupList) {
    return findByDescription(groupString, groupList);
  }

  default Anniversary toAnniversary(Integer anniversaryNumber, List<Anniversary> anniversaryList) {
    return anniversaryList.stream()
        .filter(item -> Objects.nonNull(item.getYear()))
        .filter(item -> item.getYear().equals(anniversaryNumber))
        .findFirst()
        .orElse(null);
  }

  private <T extends Descriptive> T findByDescription(String description, List<T> list) {
    return list.stream()
        .filter(item -> item.getDescription().equalsIgnoreCase(description))
        .findFirst()
        .orElse(null);
  }
}
