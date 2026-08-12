package com.mesofi.mythclothapi.catalogs;

import java.time.Instant;
import java.util.List;

import com.mesofi.mythclothapi.anniversaries.model.Anniversary;
import com.mesofi.mythclothapi.anniversaries.model.AnniversaryType;
import com.mesofi.mythclothapi.catalogs.model.CatalogContext;
import com.mesofi.mythclothapi.catalogs.model.Distribution;
import com.mesofi.mythclothapi.catalogs.model.Group;
import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.catalogs.model.Series;
import com.mesofi.mythclothapi.common.Descriptive;
import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.distributors.model.Distributor;
import com.mesofi.mythclothapi.distributors.model.DistributorName;

public class CatalogMockBuilder {

    private CatalogMockBuilder() {

    }

    public static CatalogContext createMockCatalogContext() {
        List<Distributor> distributors = getAllDistributors();
        List<Descriptive> distributions = getAllDistributions();
        List<Descriptive> lineUps = getAllLineUps();
        List<Descriptive> series = getAllSeries();
        List<Descriptive> groups = getAllGroups();
        List<Anniversary> anniversaries = getAllAnniversaries();

        return new CatalogContext(distributors, distributions, lineUps, series, groups, anniversaries);
    }
    private static List<Distributor> getAllDistributors() {

        return List.of(createDistributor(1L, DistributorName.BANDAI, CountryCode.JP),
                createDistributor(2L, DistributorName.DAM, CountryCode.MX),
                createDistributor(3L, DistributorName.DTM, CountryCode.MX),
                createDistributor(4L, DistributorName.BANDAI_CHINA, CountryCode.CN),
                createDistributor(5L, DistributorName.DS_DISTRIBUTIONS, CountryCode.ES),
                createDistributor(6L, DistributorName.BLUE_FIN, CountryCode.US));
    }

    private static List<Descriptive> getAllDistributions() {
        return List.of(createDistribution(1L, "Stores"), createDistribution(2L, "Tamashii Web Shop"),
                createDistribution(3L, "Tamashii World Tour"), createDistribution(4L, "Tamashii Nations"),
                createDistribution(5L, "Tamashii Store"), createDistribution(6L, "Other Limited Edition"));
    }

    private static List<Descriptive> getAllLineUps() {
        return List.of(createLineUp(1L, "Myth Cloth EX"), createLineUp(2L, "Myth Cloth"), createLineUp(3L, "Appendix"),
                createLineUp(4L, "Saint Cloth Legend"), createLineUp(5L, "Figuarts"),
                createLineUp(6L, "Saint Cloth Crown"), createLineUp(7L, "DD Panoramation"),
                createLineUp(8L, "Figuarts Zero Metallic Touch"), createLineUp(9L, "Saint Cloth Action"),
                createLineUp(10L, "Saint Cloth Rebirth"), createLineUp(11L, "EX project Metalbuild"),
                createLineUp(12L, "Saint Cloth Series"), createLineUp(13L, "Tamashii Nations Box"));
    }

    private static List<Descriptive> getAllSeries() {
        return List.of(createSeries(1L, "Saint Seiya"), createSeries(2L, "Saintia Sho"),
                createSeries(3L, "Soul of Gold"), createSeries(4L, "Saint Seiya Legend Of Sanctuary"),
                createSeries(5L, "Saint Seiya Omega"), createSeries(6L, "The Lost Canvas"),
                createSeries(7L, "Saint Seiya The Beginning"));
    }

    private static List<Descriptive> getAllGroups() {
        return List.of(createGroup(1L, "Accessories"), createGroup(2L, "Bronze Saint V1"),
                createGroup(3L, "Bronze Saint V2"), createGroup(4L, "Bronze Saint V3"),
                createGroup(5L, "Bronze Saint V4"), createGroup(6L, "Bronze Saint V5"),
                createGroup(7L, "Secondary Bronze"), createGroup(8L, "Black Saint"), createGroup(9L, "Steel"),
                createGroup(10L, "Silver Saint"), createGroup(11L, "Gold Saint"), createGroup(12L, "God Robe"),
                createGroup(13L, "Poseidon Scale"), createGroup(14L, "Surplice Saint"), createGroup(15L, "Specter"),
                createGroup(16L, "Judge"), createGroup(17L, "God"), createGroup(18L, "Gold Inheritor"));
    }

    private static List<Anniversary> getAllAnniversaries() {
        return List.of(createAnniversary(1L, "Masami Kurumada's Passionate Artwork 40th Anniversary", 40, null),
                createAnniversary(2L, "Jump 50th Anniversary Edition", 50, null),
                createAnniversary(3L, "Tamashii Nations 10th World Tour", 10,
                        AnniversaryType.TAMASHII_NATIONS_WORLD_TOUR),
                createAnniversary(4L, "Tamashii Nations 15th World Tour", 15,
                        AnniversaryType.TAMASHII_NATIONS_WORLD_TOUR),
                createAnniversary(5L, "10th Anniversary", 10, AnniversaryType.SAINT_CLOTH_MYTH),
                createAnniversary(6L, "15th Anniversary", 15, AnniversaryType.SAINT_CLOTH_MYTH),
                createAnniversary(7L, "20th Anniversary", 20, AnniversaryType.SAINT_CLOTH_MYTH),
                createAnniversary(8L, "Saint Seiya 30th Anniversary Theme Exhibition", 30, AnniversaryType.SAINT_SEIYA),
                createAnniversary(9L, "Saint Seiya 40th anniversary", 40, AnniversaryType.SAINT_SEIYA));
    }

    private static Distributor createDistributor(Long id, DistributorName name, CountryCode country) {
        Distributor distributor = new Distributor();
        distributor.setId(id);
        distributor.setName(name);
        distributor.setCountry(country);
        distributor.setWebsite(null);
        distributor.setCreationDate(Instant.now());
        distributor.setUpdateDate(Instant.now());

        return distributor;
    }

    private static Distribution createDistribution(Long id, String description) {
        Distribution distribution = new Distribution();
        distribution.setId(id);
        distribution.setDescription(description);
        distribution.setCreationDate(Instant.now());
        distribution.setUpdateDate(Instant.now());

        return distribution;
    }

    private static LineUp createLineUp(Long id, String description) {
        LineUp lineUp = new LineUp();
        lineUp.setId(id);
        lineUp.setDescription(description);
        lineUp.setCreationDate(Instant.now());
        lineUp.setUpdateDate(Instant.now());

        return lineUp;
    }

    private static Series createSeries(Long id, String description) {
        Series series = new Series();
        series.setId(id);
        series.setDescription(description);
        series.setCreationDate(Instant.now());
        series.setUpdateDate(Instant.now());

        return series;
    }

    private static Group createGroup(Long id, String description) {
        Group group = new Group();
        group.setId(id);
        group.setDescription(description);
        group.setCreationDate(Instant.now());
        group.setUpdateDate(Instant.now());

        return group;
    }

    private static Anniversary createAnniversary(Long id, String name, int year, AnniversaryType type) {
        Anniversary anniversary = new Anniversary();
        anniversary.setId(id);
        anniversary.setName(name);
        anniversary.setYear(year);
        anniversary.setType(type);
        anniversary.setCreationDate(Instant.now());
        anniversary.setUpdateDate(Instant.now());

        return anniversary;
    }
}
