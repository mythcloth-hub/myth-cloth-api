package com.mesofi.mythclothapi.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import com.mesofi.mythclothapi.entity.FigurineDistributor;
import com.mesofi.mythclothapi.figurines.FigurineEntity;
import com.mesofi.mythclothapi.figurines.FigurineRepository;
import com.mesofi.mythclothapi.model.CurrencyCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FigurineService {

  private static final Pattern PRICE_PATTERN = Pattern.compile("([\\d,]+)");

  private final FigurineRepository figurineRepository;

  public int importFromPublicDrive(String fileId) {
    String fileUrl =
        "https://docs.google.com/spreadsheets/d/%s/export?format=csv".formatted(fileId);

    AtomicInteger inserts = new AtomicInteger(0);
    AtomicInteger updates = new AtomicInteger(0);

    try {
      URL csvUrl = URI.create(fileUrl).toURL();
      try (Reader reader = new InputStreamReader(csvUrl.openStream(), StandardCharsets.UTF_8)) {
        Iterable<CSVRecord> records =
            CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build()
                .parse(reader);

        for (CSVRecord row : records) {
          // Distributor distributor= new Distributor();
          // distributor.se
          Double price = parsePrice(row.get("Price (JPY)"));

          FigurineDistributor figurineDistributor = new FigurineDistributor();
          figurineDistributor.setCurrency(CurrencyCode.JPY);
          // figurineDistributor.setDistributor(distributor);
          figurineDistributor.setPrice(price);

          FigurineEntity figurine = new FigurineEntity();
          figurine.setUniqueName(row.get("Myth Cloth Original Name"));
          figurine.setNormalizedName(row.get("Base Name"));
          figurine.setDistributors(List.of(figurineDistributor));

          System.out.println(price);

          figurineRepository
              .findByUniqueName(figurine.getUniqueName())
              .ifPresentOrElse(
                  existing -> {
                    // log.info("Updating existing: {}", existing.getUniqueName());

                    // existing.setNormalizedName(figurine.getNormalizedName());
                    // figurineRepository.save(existing);

                    // increment update counter
                    updates.incrementAndGet();
                  },
                  () -> {
                    log.info("Inserting new: {}", figurine.getUniqueName());
                    figurineRepository.save(figurine);

                    // increment insert counter
                    inserts.incrementAndGet();
                  });
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    log.info("Import completed. Inserts: {}, Updates: {}", inserts, updates);

    return inserts.get() + updates.get();
  }

  private Double parsePrice(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    Matcher matcher = PRICE_PATTERN.matcher(value);
    if (matcher.find()) {
      return Double.valueOf(matcher.group(1).replace(",", ""));
    }

    return null;
  }
}
