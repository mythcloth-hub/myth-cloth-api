package com.mesofi.mythclothapi.figurines;

import static com.mesofi.mythclothapi.distributors.model.CountryCode.JP;
import static com.mesofi.mythclothapi.figurineevents.model.FigurineEventType.ANNOUNCEMENT;
import static com.mesofi.mythclothapi.figurineevents.model.FigurineEventType.PREORDER_OPEN;
import static com.mesofi.mythclothapi.figurineevents.model.FigurineEventType.RELEASE;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.mesofi.mythclothapi.anniversaries.AnniversaryRepository;
import com.mesofi.mythclothapi.catalogs.repository.DistributionRepository;
import com.mesofi.mythclothapi.catalogs.repository.GroupRepository;
import com.mesofi.mythclothapi.catalogs.repository.LineUpRepository;
import com.mesofi.mythclothapi.catalogs.repository.SeriesRepository;
import com.mesofi.mythclothapi.distributors.DistributorRepository;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEvent;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEventType;
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
              prepareForPersistence(incoming);
              return incoming;
            });
  }

  @Transactional
  public FigurineResp createFigurine(@NotNull @Valid FigurineReq request) {
    log.info("Creating figurine '{}'", request.name());

    CatalogContext catalogContext = loadCatalogs();

    Figurine figurine = mapper.toFigurine(request, catalogContext);
    prepareForPersistence(figurine);

    var saved = repository.save(figurine);
    return mapper.toFigurineResp(saved, this::createDisplayableName, this::calculatePriceWithTax);
  }

  public String createDisplayableName(Figurine figurine) {
    return "FIXME";
  }

  public Double calculatePriceWithTax(FigurineDistributor figurineDistributor) {
    if (figurineDistributor == null
        || figurineDistributor.getPrice() == null
        || figurineDistributor.getPrice() <= 0) {
      return null;
    }

    return switch (figurineDistributor.getCurrency()) {
      case JPY ->
          calculateJapanesePriceWithTax(
              figurineDistributor.getPrice(), figurineDistributor.getReleaseDate());
      case MXN -> figurineDistributor.getPrice() * 1.16; // example IVA
      case USD -> figurineDistributor.getPrice(); // no VAT by default
      default -> figurineDistributor.getPrice();
    };
  }

  private Double calculateJapanesePriceWithTax(Double price, LocalDate releaseDate) {
    if (releaseDate == null) {
      return price; // fallback: unknown tax date
    }

    double taxRate;

    if (releaseDate.isBefore(LocalDate.of(1997, 4, 1))) {
      taxRate = 0.03;
    } else if (releaseDate.isBefore(LocalDate.of(2014, 4, 1))) {
      taxRate = 0.05;
    } else if (releaseDate.isBefore(LocalDate.of(2019, 10, 1))) {
      taxRate = 0.08;
    } else {
      taxRate = 0.10;
    }

    return price * (1 + taxRate);
  }

  private void prepareForPersistence(Figurine figurine) {
    createDefaultEvents(figurine);
    linkReferences(figurine);

    Instant localDateTime = Instant.now();
    figurine.setCreationDate(localDateTime);
    figurine.setUpdateDate(localDateTime);
  }

  private void createDefaultEvents(Figurine figurine) {
    // creates the default events ...
    if (figurine.getDistributors() != null && !figurine.getDistributors().isEmpty()) {
      FigurineDistributor figurineDistributor = figurine.getDistributors().getFirst();

      Optional.ofNullable(figurineDistributor.getAnnouncementDate())
          .ifPresent(
              announcementDate ->
                  addDefaultEvent(
                      "First announced as a possible future release.",
                      announcementDate,
                      ANNOUNCEMENT,
                      figurine));
      Optional.ofNullable(figurineDistributor.getPreorderDate())
          .ifPresent(
              preorderDate ->
                  addDefaultEvent(
                      "Pre-orders are officially open.", preorderDate, PREORDER_OPEN, figurine));
      Optional.ofNullable(figurineDistributor.getReleaseDate())
          .ifPresent(
              releaseDate ->
                  addDefaultEvent(
                      "The global release date has been officially announced.",
                      releaseDate,
                      RELEASE,
                      figurine));
    }
  }

  private void addDefaultEvent(
      String description, LocalDate date, FigurineEventType type, Figurine figurine) {

    FigurineEvent event = new FigurineEvent();
    event.setDescription(description);
    event.setEventDate(date);
    event.setType(type);
    event.setRegion(JP);

    figurine.getEvents().add(event);
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
