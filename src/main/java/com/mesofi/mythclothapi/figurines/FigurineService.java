package com.mesofi.mythclothapi.figurines;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mesofi.mythclothapi.catalogs.model.Anniversary;
import com.mesofi.mythclothapi.catalogs.model.Distribution;
import com.mesofi.mythclothapi.catalogs.model.Group;
import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.catalogs.model.Series;
import com.mesofi.mythclothapi.catalogs.repository.AnniversaryRepository;
import com.mesofi.mythclothapi.catalogs.repository.DistributionRepository;
import com.mesofi.mythclothapi.catalogs.repository.GroupRepository;
import com.mesofi.mythclothapi.catalogs.repository.LineUpRepository;
import com.mesofi.mythclothapi.catalogs.repository.SeriesRepository;
import com.mesofi.mythclothapi.distributors.DistributorRepository;
import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.distributors.model.Distributor;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurines.mapper.FigurineCsv;
import com.mesofi.mythclothapi.figurines.mapper.FigurineMapper;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.opencsv.bean.CsvToBeanBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FigurineService {

  private final FigurineRepository figurineRepository;
  private final FigurineMapper figurineMapper;
  private final DistributorRepository distributorRepository;
  private final DistributionRepository distributionRepository;
  private final LineUpRepository lineUpRepository;
  private final SeriesRepository seriesRepository;
  private final GroupRepository groupRepository;
  private final AnniversaryRepository anniversaryRepository;

  public int importFromPublicDrive(String fileId) {
    String fileUrl =
        "https://docs.google.com/spreadsheets/d/%s/export?format=csv".formatted(fileId);

    List<Distributor> distributorList = distributorRepository.findAll();
    List<Distribution> distributionList = distributionRepository.findAll();
    List<LineUp> lineUpList = lineUpRepository.findAll();
    List<Series> seriesList = seriesRepository.findAll();
    List<Group> groupList = groupRepository.findAll();
    List<Anniversary> anniversaryList = anniversaryRepository.findAll();

    try (Reader reader = new InputStreamReader(URI.create(fileUrl).toURL().openStream())) {
      List<FigurineCsv> figurineCsvList =
          new CsvToBeanBuilder<FigurineCsv>(reader)
              .withType(FigurineCsv.class)
              .withIgnoreLeadingWhiteSpace(true)
              .build()
              .parse();

      List<Figurine> figurineList =
          figurineCsvList.stream()
              .peek(
                  f -> {
                    f.setDistribution(
                        figurineMapper.toDistribution(f.getDistributionString(), distributionList));
                    f.setLineup(figurineMapper.toLineup(f.getLineupString(), lineUpList));
                    f.setSeries(figurineMapper.toSeries(f.getSeriesString(), seriesList));
                    f.setGroup(figurineMapper.toGroup(f.getGroupString(), groupList));
                    f.setAnniversary(
                        figurineMapper.toAnniversary(f.getAnniversaryNumber(), anniversaryList));
                  })
              .map(figurineMapper::toFigurine)
              .peek(
                  f -> {
                    if (f.getDistributors() != null) {
                      List<FigurineDistributor> ff = f.getDistributors();
                      ff.forEach(
                          fd -> {
                            fd.setFigurine(f);
                            if (CurrencyCode.JPY == fd.getCurrency()) {
                              Optional<Distributor> dd =
                                  distributorList.stream()
                                      .filter(dl -> dl.getCountry() == CountryCode.JP)
                                      .findFirst();
                              dd.ifPresent(fd::setDistributor);
                              // System.out.println(fd.getDistributor());
                            }
                            if (CurrencyCode.MXN == fd.getCurrency()) {
                              Optional<Distributor> dd =
                                  distributorList.stream()
                                      .filter(dl -> dl.getCountry() == CountryCode.MX)
                                      .findFirst();
                              dd.ifPresent(fd::setDistributor);
                            }
                            if (CurrencyCode.CNY == fd.getCurrency()) {
                              Optional<Distributor> dd =
                                  distributorList.stream()
                                      .filter(dl -> dl.getCountry() == CountryCode.CN)
                                      .findFirst();
                              dd.ifPresent(fd::setDistributor);
                            }
                          });
                    }
                  })
              .toList();

      figurineRepository.saveAllAndFlush(figurineList);
      log.info("All figurines have been saved correctly!!");
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }

    return 0;
  }
}
