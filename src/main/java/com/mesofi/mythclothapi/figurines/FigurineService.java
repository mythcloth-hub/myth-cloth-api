package com.mesofi.mythclothapi.figurines;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.mesofi.mythclothapi.catalogs.repository.AnniversaryRepository;
import com.mesofi.mythclothapi.catalogs.repository.DistributionRepository;
import com.mesofi.mythclothapi.catalogs.repository.GroupRepository;
import com.mesofi.mythclothapi.catalogs.repository.LineUpRepository;
import com.mesofi.mythclothapi.catalogs.repository.SeriesRepository;
import com.mesofi.mythclothapi.distributors.DistributorRepository;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurines.dto.FigurineReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.figurines.mapper.CatalogContext;
import com.mesofi.mythclothapi.figurines.mapper.FigurineCsv;
import com.mesofi.mythclothapi.figurines.mapper.FigurineMapper;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.opencsv.bean.CsvToBeanBuilder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Validated
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
  private final FigurineRepository repository;

  public void importFromPublicDrive(final String fileId) {
    String fileUrl = DRIVE_URL.formatted(fileId);
    CatalogContext catalogContext = loadCatalogs();

    try (Reader reader = new InputStreamReader(URI.create(fileUrl).toURL().openStream())) {

      List<FigurineCsv> csvRows =
          new CsvToBeanBuilder<FigurineCsv>(reader)
              .withType(FigurineCsv.class)
              .withIgnoreLeadingWhiteSpace(true)
              .build()
              .parse();

      List<Figurine> figurines =
          csvRows.stream().map(csv -> upsertFigurine(csv, catalogContext)).toList();

      List<Figurine> saved = repository.saveAllAndFlush(figurines);
      log.info("{} figurines have been processed (inserted or updated)", saved.size());
    } catch (IOException ex) {
      throw new IllegalStateException("Unable to read CSV from Google Drive", ex);
    }
  }

  private Figurine upsertFigurine(FigurineCsv csv, CatalogContext context) {
    // Convert CSV → Incoming entity
    Figurine incoming = mapper.toFigurine(csv, context);

    // Find existing by unique key (legacyName)
    return repository
        .findByLegacyName(incoming.getLegacyName())
        .map(
            existing -> {
              // Update existing record
              mapper.updateFigurine(existing, incoming);
              linkReferences(existing);
              return existing;
            })
        .orElseGet(
            () -> {
              // Create new record
              linkReferences(incoming);
              return incoming;
            });
  }

  @Transactional
  public FigurineResp createFigurine(@NotNull @Valid FigurineReq request) {
    log.info("Creating figurine '{}'", request.name());

    CatalogContext catalogContext = loadCatalogs();

    Figurine figurine = mapper.toFigurine(request, catalogContext);
    linkReferences(figurine);

    var saved = repository.save(figurine);
    return mapper.toFigurineResp(saved, this::createDisplayableName);
  }

  public String createDisplayableName(Figurine figurine) {
    return "FIXME";
  }

  public Double calculatePriceWithTax(FigurineDistributor figurineDistributor) {
    return 3.0;
  }

  private void linkReferences(Figurine figurine) {
    figurine.getDistributors().forEach(d -> d.setFigurine(figurine));
    figurine.getEvents().forEach(e -> e.setFigurine(figurine));
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
