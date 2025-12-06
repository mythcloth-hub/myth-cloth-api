package com.mesofi.mythclothapi.figurines;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mesofi.mythclothapi.catalogs.repository.AnniversaryRepository;
import com.mesofi.mythclothapi.catalogs.repository.DistributionRepository;
import com.mesofi.mythclothapi.catalogs.repository.GroupRepository;
import com.mesofi.mythclothapi.catalogs.repository.LineUpRepository;
import com.mesofi.mythclothapi.catalogs.repository.SeriesRepository;
import com.mesofi.mythclothapi.distributors.DistributorRepository;
import com.mesofi.mythclothapi.figurines.mapper.CatalogContext;
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

  private static final String DRIVE_URL =
      "https://docs.google.com/spreadsheets/d/%s/export?format=csv";

  private final FigurineMapper mapper;
  private final DistributorRepository distributorRepository;
  private final DistributionRepository distributionRepository;
  private final LineUpRepository lineUpRepository;
  private final SeriesRepository seriesRepository;
  private final GroupRepository groupRepository;
  private final AnniversaryRepository anniversaryRepository;
  private final FigurineRepository figurineRepository;

  public void importFromPublicDrive(final String fileId) {
    String fileUrl = DRIVE_URL.formatted(fileId);

    CatalogContext catalogContext = loadCatalogs();

    try (Reader reader = new InputStreamReader(URI.create(fileUrl).toURL().openStream())) {
      List<FigurineCsv> figurineCsvList =
          new CsvToBeanBuilder<FigurineCsv>(reader)
              .withType(FigurineCsv.class)
              .withIgnoreLeadingWhiteSpace(true)
              .build()
              .parse();

      List<Figurine> figurineList =
          figurineCsvList.stream()
              .map(csv -> mapper.toFigurine(csv, catalogContext))
              .map(this::linkDistributors)
              .toList();

      List<Figurine> savedFigurines = figurineRepository.saveAllAndFlush(figurineList);
      log.info("{} figurines have been saved correctly!!", savedFigurines.size());
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  private Figurine linkDistributors(Figurine figurine) {
    Optional.ofNullable(figurine.getDistributors())
        .ifPresent(list -> list.forEach(fd -> fd.setFigurine(figurine)));
    return figurine;
  }

  private CatalogContext loadCatalogs() {
    return new CatalogContext(
        distributorRepository.findAll(),
        distributionRepository.findAll(),
        lineUpRepository.findAll(),
        seriesRepository.findAll(),
        groupRepository.findAll(),
        anniversaryRepository.findAll());
  }
}
