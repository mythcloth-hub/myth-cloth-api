package com.mesofi.mythclothapi.figurines;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mesofi.mythclothapi.anniversaries.AnniversaryRepository;
import com.mesofi.mythclothapi.anniversaries.model.Anniversary;
import com.mesofi.mythclothapi.catalogs.model.Distribution;
import com.mesofi.mythclothapi.catalogs.model.Group;
import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.catalogs.model.Series;
import com.mesofi.mythclothapi.catalogs.repository.DistributionRepository;
import com.mesofi.mythclothapi.catalogs.repository.GroupRepository;
import com.mesofi.mythclothapi.catalogs.repository.LineUpRepository;
import com.mesofi.mythclothapi.catalogs.repository.SeriesRepository;
import com.mesofi.mythclothapi.collectors.CollectorRepository;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionRepository;
import com.mesofi.mythclothapi.distributors.DistributorRepository;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurines.imports.FigurineCsvSource;
import com.mesofi.mythclothapi.figurines.mapper.FigurineMapper;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;

@ExtendWith(MockitoExtension.class)
class FigurineServiceDisplayNameTest {

  @Mock private FigurineMapper mapper;
  @Mock private FigurineCsvSource csvSource;
  @Mock private DistributorRepository distributorRepository;
  @Mock private DistributionRepository distributionRepository;
  @Mock private LineUpRepository lineUpRepository;
  @Mock private SeriesRepository seriesRepository;
  @Mock private GroupRepository groupRepository;
  @Mock private AnniversaryRepository anniversaryRepository;
  @Mock private FigurineRepository figurineRepository;
  @Mock private CurrencyRegionResolver currencyRegionResolver;
  @Mock private CollectorRepository collectorRepository;
  @Mock private CollectorCollectionRepository collectorCollectionRepository;

  private FigurineService service;

  @BeforeEach
  void setUp() {
    service =
        new FigurineService(
            mapper,
            csvSource,
            distributorRepository,
            distributionRepository,
            lineUpRepository,
            seriesRepository,
            groupRepository,
            anniversaryRepository,
            figurineRepository,
            currencyRegionResolver,
            collectorRepository,
            collectorCollectionRepository);
  }

  @ParameterizedTest(name = "{index} - {0}")
  @MethodSource("displayNameCases")
  void createDisplayableName_shouldBuildExpectedName(
      String caseName, Figurine figurine, String expected) {
    assertThat(service.createDisplayableName(figurine)).as(caseName).isEqualTo(expected);
  }

  @Test
  void createDisplayableName_shouldReturnBaseName_whenNoRuleMatches() {
    Figurine figurine = baseFigurine("Pegasus Seiya", "Unknown", "Unknown", "Unknown");

    assertThat(service.createDisplayableName(figurine)).isEqualTo("Pegasus Seiya");
  }

  static List<Object[]> displayNameCases() {
    return List.of(
        new Object[] {
          "Figuarts Zero Metallic Touch",
          baseFigurine("Seiya", "Figuarts Zero Metallic Touch", "Saint Seiya", "Bronze Saint V1"),
          "Figuarts Zero Touche M\u00e9tallique Seiya"
        },
        new Object[] {
          "DD Panoramation keyword match",
          baseFigurine("Pegasus Seiya", "DD Panoramation", "Saint Seiya", "Bronze Saint V1"),
          "Pegasus Seiya -Pegasus Meteor Punches-"
        },
        new Object[] {
          "DD Panoramation fallback",
          baseFigurine("Odin Seiya", "DD Panoramation", "Saint Seiya", "Bronze Saint V1"),
          "Odin Seiya"
        },
        new Object[] {
          "Myth Cloth EX - Legend Of Sanctuary",
          baseFigurine("Aiolos", "Myth Cloth EX", "Saint Seiya Legend Of Sanctuary", "Gold Saint"),
          "Aiolos ~Legend of Sanctuary Edition~"
        },
        new Object[] {
          "Myth Cloth EX - Saintia Sho",
          baseFigurine("Milo", "Myth Cloth EX", "Saintia Sho", "Gold Saint"),
          "Milo Saintia Sho Color Edition"
        },
        new Object[] {
          "Myth Cloth EX - Soul of Gold / God Robe",
          baseFigurine("Seiya", "Myth Cloth EX", "Soul of Gold", "God Robe"),
          "Seiya God Robe"
        },
        new Object[] {
          "Myth Cloth EX - Soul of Gold accessories",
          baseFigurine("Athena Pedestal", "Myth Cloth EX", "Soul of Gold", "Accessories"),
          "Athena Pedestal Set"
        },
        new Object[] {
          "Myth Cloth EX - Soul of Gold god cloth",
          baseFigurine("Aiolia", "Myth Cloth EX", "Soul of Gold", "Gold Saint"),
          "Aiolia (God Cloth)"
        },
        new Object[] {
          "Myth Cloth EX - Soul of Gold god cloth set",
          withSet(baseFigurine("Mu", "Myth Cloth EX", "Soul of Gold", "Gold Saint"), true),
          "Mu (God Cloth) Saga Saga Premium Set"
        },
        new Object[] {
          "Myth Cloth EX - Gold 24",
          withGold(baseFigurine("Shaka", "Myth Cloth EX", "Saint Seiya", "Gold Saint"), true),
          "Shaka Gold 24"
        },
        new Object[] {
          "Myth Cloth EX - The Beginning",
          baseFigurine("Seiya", "Myth Cloth EX", "Saint Seiya The Beginning", "Bronze Saint V1"),
          "Seiya -Knights of the Zodiac-"
        },
        new Object[] {
          "Myth Cloth EX - Divine Saga Premium Set",
          withSet(
              withAnniversary(baseFigurine("Saga", "Myth Cloth EX", "Saint Seiya", "God"), 20),
              true),
          "Saga -Divine Saga Premium Set-"
        },
        new Object[] {
          "Myth Cloth EX - Gold Inheritor",
          baseFigurine("Shiryu", "Myth Cloth EX", "Saint Seiya", "Gold Inheritor"),
          "Shiryu ~Inheritor of the Gold Cloth~"
        },
        new Object[] {
          "Myth Cloth EX - God Robe 40th",
          withAnniversary(baseFigurine("Dohko", "Myth Cloth EX", "Saint Seiya", "God Robe"), 40),
          "Dohko 40th Anniversary Ver."
        },
        new Object[] {
          "Myth Cloth EX - Poseidon OCE",
          withOce(baseFigurine("Poseidon", "Myth Cloth EX", "Saint Seiya", "Poseidon Scale"), true),
          "Poseidon ~Original Color Edition~"
        },
        new Object[] {
          "Myth Cloth EX - Poseidon Sorrento special",
          withReleaseYear(
              baseFigurine("Sorrento", "Myth Cloth EX", "Saint Seiya", "Poseidon Scale"), 2021),
          "Sorrento <Asgard Final Battle Ver.>"
        },
        new Object[] {
          "Myth Cloth EX - Poseidon set",
          withSet(baseFigurine("Poseidon", "Myth Cloth EX", "Saint Seiya", "Poseidon Scale"), true),
          "Poseidon Imperial Throne Set"
        },
        new Object[] {
          "Myth Cloth EX - Judge OCE",
          withOce(baseFigurine("Radamanthys", "Myth Cloth EX", "Saint Seiya", "Judge"), true),
          "Radamanthys -Original Color Edition-"
        },
        new Object[] {
          "Myth Cloth EX - Bronze V1",
          baseFigurine("Seiya", "Myth Cloth EX", "Saint Seiya", "Bronze Saint V1"),
          "Seiya (Initial Bronze Cloth)"
        },
        new Object[] {
          "Myth Cloth EX - Bronze V2 golden",
          withGolden(
              baseFigurine("Hyoga", "Myth Cloth EX", "Saint Seiya", "Bronze Saint V2"), true),
          "Hyoga (New Bronze Cloth) ~Golden Limited Edition~"
        },
        new Object[] {
          "Myth Cloth EX - Bronze V2 revival",
          withRevival(
              baseFigurine("Shun", "Myth Cloth EX", "Saint Seiya", "Bronze Saint V2"), true),
          "Shun [New Bronze Cloth] <Revival Ver.>"
        },
        new Object[] {
          "Myth Cloth EX - Bronze V2 oce anniversary",
          withOce(
              withAnniversary(
                  baseFigurine("Ikki", "Myth Cloth EX", "Saint Seiya", "Bronze Saint V2"), 40),
              true),
          "Ikki ~(New Bronze Cloth) 40th Anniversary Edition~"
        },
        new Object[] {
          "Myth Cloth EX - Bronze V2 oce no anniversary",
          withOce(baseFigurine("Ikki", "Myth Cloth EX", "Saint Seiya", "Bronze Saint V2"), true),
          "Ikki ~Original Color Edition~"
        },
        new Object[] {
          "Myth Cloth EX - Bronze V2 default",
          baseFigurine("Ikki", "Myth Cloth EX", "Saint Seiya", "Bronze Saint V2"),
          "Ikki (New Bronze Cloth)"
        },
        new Object[] {
          "Myth Cloth EX - Bronze V3 oce",
          withOce(baseFigurine("Seiya", "Myth Cloth EX", "Saint Seiya", "Bronze Saint V3"), true),
          "Seiya [Final Bronze Cloth] ~Original Color Edition~"
        },
        new Object[] {
          "Myth Cloth EX - Bronze V3 golden",
          withGolden(
              baseFigurine("Seiya", "Myth Cloth EX", "Saint Seiya", "Bronze Saint V3"), true),
          "Seiya [Final Bronze Cloth] ~Golden Limited Edition~"
        },
        new Object[] {
          "Myth Cloth EX - Bronze V3 default",
          baseFigurine("Seiya", "Myth Cloth EX", "Saint Seiya", "Bronze Saint V3"),
          "Seiya [Final Bronze Cloth]"
        },
        new Object[] {
          "Myth Cloth EX - Bronze V4",
          baseFigurine("Shun", "Myth Cloth EX", "Saint Seiya", "Bronze Saint V4"),
          "Shun [God Cloth]"
        },
        new Object[] {
          "Myth Cloth EX - God OCE",
          withOce(baseFigurine("Athena", "Myth Cloth EX", "Saint Seiya", "God"), true),
          "Athena ~Original Color Edition~"
        },
        new Object[] {
          "Myth Cloth EX - Gold Saint OCE",
          withOce(baseFigurine("Aiolos", "Myth Cloth EX", "Saint Seiya", "Gold Saint"), true),
          "Aiolos ~Original Color Edition~"
        },
        new Object[] {
          "Myth Cloth EX - Gold Saint revival anniversary",
          withRevival(
              withAnniversary(
                  baseFigurine("Milo", "Myth Cloth EX", "Saint Seiya", "Gold Saint"), 20),
              true),
          "Milo <20th Revival Ver.>"
        },
        new Object[] {
          "Myth Cloth EX - Gold Saint revival",
          withRevival(baseFigurine("Milo", "Myth Cloth EX", "Saint Seiya", "Gold Saint"), true),
          "Milo <Revival Ver.>"
        },
        new Object[] {
          "Myth Cloth EX - Surplice not set",
          baseFigurine("Saga", "Myth Cloth EX", "Saint Seiya", "Surplice Saint"),
          "Saga (Surplice)"
        },
        new Object[] {
          "Myth Cloth EX - Surplice not set revival",
          withRevival(baseFigurine("Saga", "Myth Cloth EX", "Saint Seiya", "Surplice Saint"), true),
          "Saga (Surplice) <20th Revival Ver.>"
        },
        new Object[] {
          "Myth Cloth EX - Surplice set",
          withSet(baseFigurine("Saga", "Myth Cloth EX", "Saint Seiya", "Surplice Saint"), true),
          "Saga Set"
        },
        new Object[] {
          "Myth Cloth - Hilda Stores",
          withDistribution(baseFigurine("Hilda", "Myth Cloth", "Saint Seiya", "God"), "Stores"),
          "Hilda -The Earth Representative of Odin-"
        },
        new Object[] {
          "Myth Cloth - Bronze V1 manga",
          withManga(baseFigurine("Seiya", "Myth Cloth", "Saint Seiya", "Bronze Saint V1"), true),
          "Seiya Comic Ver."
        },
        new Object[] {
          "Myth Cloth - Bronze V1 anniversary",
          withAnniversary(
              baseFigurine("Seiya", "Myth Cloth", "Saint Seiya", "Bronze Saint V1"), 20),
          "Seiya 20th Anniversary Ver."
        },
        new Object[] {
          "Myth Cloth - Bronze V1 revival",
          withRevival(baseFigurine("Seiya", "Myth Cloth", "Saint Seiya", "Bronze Saint V1"), true),
          "Seiya Early Bronze Cloth <Revival Ver.>"
        },
        new Object[] {
          "Myth Cloth - Bronze V1 golden",
          withGolden(baseFigurine("Seiya", "Myth Cloth", "Saint Seiya", "Bronze Saint V1"), true),
          "Seiya ~Limited Gold~"
        },
        new Object[] {
          "Myth Cloth - Bronze V1 oce",
          withOce(baseFigurine("Seiya", "Myth Cloth", "Saint Seiya", "Bronze Saint V1"), true),
          "Seiya ~Original Color Edition~"
        },
        new Object[] {
          "Myth Cloth - Bronze V1 default",
          baseFigurine("Seiya", "Myth Cloth", "Saint Seiya", "Bronze Saint V1"),
          "Seiya (Initial Bronze Cloth)"
        },
        new Object[] {
          "Myth Cloth - Bronze V2 golden",
          withGolden(baseFigurine("Hyoga", "Myth Cloth", "Saint Seiya", "Bronze Saint V2"), true),
          "Hyoga Power of Gold"
        },
        new Object[] {
          "Myth Cloth - Bronze V2 broken",
          withBroken(baseFigurine("Hyoga", "Myth Cloth", "Saint Seiya", "Bronze Saint V2"), true),
          "Hyoga ~Broken Version~"
        },
        new Object[] {
          "Myth Cloth - Bronze V3 gold",
          withGold(baseFigurine("Shun", "Myth Cloth", "Saint Seiya", "Bronze Saint V3"), true),
          "Shun Golden Genealogy"
        },
        new Object[] {
          "Myth Cloth - Bronze V3 default",
          baseFigurine("Shun", "Myth Cloth", "Saint Seiya", "Bronze Saint V3"),
          "Shun (Final Bronze Cloth)"
        },
        new Object[] {
          "Myth Cloth - Bronze V4 anniversary10",
          withAnniversary(baseFigurine("Ikki", "Myth Cloth", "Saint Seiya", "Bronze Saint V4"), 10),
          "Ikki (God Cloth) -10th Anniversary Edition-"
        },
        new Object[] {
          "Myth Cloth - Bronze V4 oce",
          withOce(baseFigurine("Ikki", "Myth Cloth", "Saint Seiya", "Bronze Saint V4"), true),
          "Ikki God Cloth ~Original Color Edition~"
        },
        new Object[] {
          "Myth Cloth - Bronze V4 oce should win over revival",
          withRevival(
              withOce(baseFigurine("Ikki", "Myth Cloth", "Saint Seiya", "Bronze Saint V4"), true),
              true),
          "Ikki God Cloth ~Original Color Edition~"
        },
        new Object[] {
          "Myth Cloth - Bronze V4 non-10th anniversary should not use 10th label",
          withAnniversary(baseFigurine("Ikki", "Myth Cloth", "Saint Seiya", "Bronze Saint V4"), 20),
          "Ikki God Cloth"
        },
        new Object[] {
          "Myth Cloth - Surplice",
          baseFigurine("Minos", "Myth Cloth", "Saint Seiya", "Surplice Saint"),
          "Minos (Surplice)"
        },
        new Object[] {
          "Myth Cloth - Specter set",
          withSet(baseFigurine("Rhadamanthys", "Myth Cloth", "Saint Seiya", "Specter"), true),
          "Rhadamanthys Complete Set"
        },
        new Object[] {
          "Myth Cloth - fallback revival",
          withRevival(baseFigurine("Aiacos", "Myth Cloth", "Saint Seiya", "God"), true),
          "Aiacos <Revival Ver.>"
        },
        new Object[] {
          "Myth Cloth - Bronze V5",
          baseFigurine("Seiya", "Myth Cloth", "Saint Seiya", "Bronze Saint V5"),
          "Seiya (Heaven Chapter)"
        },
        new Object[] {
          "Myth Cloth - anniversary15",
          withAnniversary(baseFigurine("Shiryu", "Myth Cloth", "Saint Seiya", "God"), 15),
          "Shiryu 15th Anniversary Ver."
        },
        new Object[] {
          "Myth Cloth - fallback oce",
          withOce(baseFigurine("Marin", "Myth Cloth", "Saint Seiya", "God"), true),
          "Marin ~Original Color Edition~"
        },
        new Object[] {
          "Appendix OCE",
          withOce(baseFigurine("Gemini", "Appendix", "Saint Seiya", "Gold Saint"), true),
          "Gemini ~Original Color Edition~"
        },
        new Object[] {
          "Appendix plain cloth",
          withPlainCloth(baseFigurine("Gemini", "Appendix", "Saint Seiya", "Gold Saint"), true),
          "Gemini (Plain Cloth)"
        },
        new Object[] {
          "Myth Cloth Bronze V1 Lost Canvas falls through",
          baseFigurine("Tenma", "Myth Cloth", "The Lost Canvas", "Bronze Saint V1"),
          "Tenma"
        });
  }

  private static Figurine baseFigurine(String name, String lineUp, String series, String group) {
    Figurine figurine = new Figurine();
    figurine.setNormalizedName(name);
    figurine.setLineup(createLineup(lineUp));
    figurine.setSeries(createSeries(series));
    figurine.setGroup(createGroup(group));
    figurine.setDistributors(List.of());
    return figurine;
  }

  private static Figurine withDistribution(Figurine figurine, String distribution) {
    Distribution value = new Distribution();
    value.setDescription(distribution);
    figurine.setDistribution(value);
    return figurine;
  }

  private static Figurine withReleaseYear(Figurine figurine, int year) {
    FigurineDistributor distributor = new FigurineDistributor();
    distributor.setReleaseDate(LocalDate.of(year, 1, 1));
    figurine.setDistributors(List.of(distributor));
    return figurine;
  }

  private static Figurine withAnniversary(Figurine figurine, int year) {
    Anniversary anniversary = new Anniversary();
    anniversary.setYear(year);
    figurine.setAnniversary(anniversary);
    return figurine;
  }

  private static Figurine withOce(Figurine figurine, boolean value) {
    figurine.setOce(value);
    return figurine;
  }

  private static Figurine withRevival(Figurine figurine, boolean value) {
    figurine.setRevival(value);
    return figurine;
  }

  private static Figurine withGolden(Figurine figurine, boolean value) {
    figurine.setGolden(value);
    return figurine;
  }

  private static Figurine withGold(Figurine figurine, boolean value) {
    figurine.setGold(value);
    return figurine;
  }

  private static Figurine withSet(Figurine figurine, boolean value) {
    figurine.setSet(value);
    return figurine;
  }

  private static Figurine withManga(Figurine figurine, boolean value) {
    figurine.setManga(value);
    return figurine;
  }

  private static Figurine withBroken(Figurine figurine, boolean value) {
    figurine.setBroken(value);
    return figurine;
  }

  private static Figurine withPlainCloth(Figurine figurine, boolean value) {
    figurine.setPlainCloth(value);
    return figurine;
  }

  private static LineUp createLineup(String description) {
    LineUp value = new LineUp();
    value.setDescription(description);
    return value;
  }

  private static Series createSeries(String description) {
    Series value = new Series();
    value.setDescription(description);
    return value;
  }

  private static Group createGroup(String description) {
    Group value = new Group();
    value.setDescription(description);
    return value;
  }
}
