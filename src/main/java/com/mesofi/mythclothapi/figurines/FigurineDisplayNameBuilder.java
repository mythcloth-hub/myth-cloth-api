package com.mesofi.mythclothapi.figurines;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import com.mesofi.mythclothapi.anniversaries.model.Anniversary;
import com.mesofi.mythclothapi.common.Descriptive;
import com.mesofi.mythclothapi.figurines.model.Figurine;

public final class FigurineDisplayNameBuilder {

    private static final String BRONZE_V1 = "Bronze Saint V1";
    private static final String BRONZE_V2 = "Bronze Saint V2";
    private static final String BRONZE_V3 = "Bronze Saint V3";
    private static final String BRONZE_V4 = "Bronze Saint V4";
    private static final String BRONZE_V5 = "Bronze Saint V5";
    private static final String SURPLICE = "Surplice Saint";
    private static final String GOD = "God";
    private static final String GOD_ROBE = "God Robe";

    private static final String OCE_SUFFIX = " ~Original Color Edition~";
    private static final String REVIVAL_SUFFIX = " <Revival Ver.>";
    private static final String _20_REVIVAL_SUFFIX = " <20th Revival Ver.>";

    private static final Map<String, String> DD_NAMES = Map.ofEntries(
            Map.entry("gemini", "{name} -the Pope's Chamber-"), Map.entry("pegasus", "{name} -Pegasus Meteor Punches-"),
            Map.entry("virgo", "{name} -The Temple of the Maiden-"), Map.entry("phoenix", "{name} -Flying Phoenix-"),
            Map.entry("leo", "Lightning in the Palace of the Lion -{name}-"),
            Map.entry("cancer", "Desperate Battle in the Palace of the Giant Crab -{name}-"),
            Map.entry("dragon", "Rozan Rising Dragon Blow -{name}-"),
            Map.entry("sagittarius", "Commitment of Aiolos’ Spirit in the Palace of the Centaur -{name}-"),
            Map.entry("athena", "Golden Zodiac extension set Fire clock of the Sanctuary -{name}-"),
            Map.entry("capricorn", "Glittering Excalibur in the Palace of the Rock Goat -{name}-"),
            Map.entry("andromeda", "Nebula Chain -{name}-"),
            Map.entry("pisces", "Blooming Roses in the Palace of the Twin Fish -{name}-"),
            Map.entry("libra", "Guidance of the Palace of the Scale -{name}-"));

    private FigurineDisplayNameBuilder() {
    }

    public static String build(Figurine figurine) {
        BuildContext context = BuildContext.from(figurine);

        String displayName = buildFiguartsZeroDisplayName(context);
        if (displayName != null) {
            return displayName;
        }

        displayName = buildDdPanoramationDisplayName(context);
        if (displayName != null) {
            return displayName;
        }

        displayName = buildMythClothExDisplayName(context);
        if (displayName != null) {
            return displayName;
        }

        displayName = buildMythClothDisplayName(context);
        if (displayName != null) {
            return displayName;
        }

        displayName = buildAppendixDisplayName(context);
        if (displayName != null) {
            return displayName;
        }

        return context.name;
    }

    private static String buildFiguartsZeroDisplayName(BuildContext context) {
        if (context.lineUpString.equalsIgnoreCase("Figuarts Zero Metallic Touch")) {
            return "Figuarts Zero Touche Métallique " + context.name;
        }
        return null;
    }

    private static String buildDdPanoramationDisplayName(BuildContext context) {
        if (!context.lineUpString.equalsIgnoreCase("DD Panoramation")) {
            return null;
        }

        String simpleName = context.name.toLowerCase();
        return DD_NAMES.keySet().stream().filter(simpleName::contains).findFirst()
                .map(key -> DD_NAMES.get(key).replace("{name}", context.name)).orElse(context.name);
    }

    private static String buildMythClothExDisplayName(BuildContext context) {
        if (!context.lineUpString.equalsIgnoreCase("Myth Cloth EX")) {
            return null;
        }

        String name = context.name;

        if (context.seriesString.equalsIgnoreCase("Saint Seiya Legend Of Sanctuary")) {
            return name + " ~Legend of Sanctuary Edition~";
        }
        if (context.seriesString.equalsIgnoreCase("Saintia Sho")) {
            return name + " Saintia Sho Color Edition";
        }
        if (context.seriesString.equalsIgnoreCase("Soul of Gold")) {
            if (context.groupString.equalsIgnoreCase(GOD_ROBE)) {
                return name + " God Robe";
            } else {
                if (context.groupString.equalsIgnoreCase("Accessories")) {
                    return name + " Set";
                } else {
                    name += " (God Cloth)";
                    if (context.set) {
                        name += " Saga Saga Premium Set";
                    }
                    return name;
                }
            }
        }
        if (context.gold) {
            return name + " Gold 24";
        }
        if (context.seriesString.equalsIgnoreCase("Saint Seiya The Beginning")) {
            return name + " -Knights of the Zodiac-";
        }
        if (context.groupString.equalsIgnoreCase(GOD) && context.anniversary && context.set) {
            return name + " -Divine Saga Premium Set-";
        }
        if (context.groupString.equalsIgnoreCase("Gold Inheritor")) {
            return name + " ~Inheritor of the Gold Cloth~";
        }
        if (context.groupString.equalsIgnoreCase(GOD_ROBE)) {
            if (context.anniversary40) {
                return name + " 40th Anniversary Ver.";
            }
        }
        if (context.groupString.equalsIgnoreCase("Poseidon Scale")) {
            if (context.oce) {
                return name + OCE_SUFFIX;
            }
            if (name.toLowerCase().contains("sorrento") && !context.metal && context.year == 2021) {
                return name + " <Asgard Final Battle Ver.>";
            }
            if (context.set) {
                return name + " Imperial Throne Set";
            }
        }
        if (context.groupString.equalsIgnoreCase("Judge")) {
            if (context.oce) {
                return name + " -Original Color Edition-";
            }
        }
        if (context.groupString.equalsIgnoreCase(BRONZE_V1)) {
            return name + " (Initial Bronze Cloth)";
        }
        if (context.groupString.equalsIgnoreCase(BRONZE_V2)) {
            if (context.golden) {
                return name + " (New Bronze Cloth) ~Golden Limited Edition~";
            } else if (context.revival) {
                return name + " [New Bronze Cloth]" + REVIVAL_SUFFIX;
            } else if (context.oce) {
                if (context.anniversary) {
                    return name + " ~(New Bronze Cloth) 40th Anniversary Edition~";
                } else {
                    return name + OCE_SUFFIX;
                }
            } else {
                return name + " (New Bronze Cloth)";
            }
        }
        if (context.groupString.equalsIgnoreCase(BRONZE_V3)) {
            name += " [Final Bronze Cloth]";
            if (context.oce) {
                name += OCE_SUFFIX;
            } else if (context.golden) {
                name += " ~Golden Limited Edition~";
            }
            return name;
        }
        if (context.groupString.equalsIgnoreCase(BRONZE_V4)) {
            return name + " [God Cloth]";
        }
        if (context.groupString.equalsIgnoreCase(GOD)) {
            if (context.oce) {
                return name + OCE_SUFFIX;
            }
        }
        if (context.groupString.equalsIgnoreCase("Gold Saint")) {
            if (context.oce) {
                return name + OCE_SUFFIX;
            }
            if (context.revival && context.anniversary) {
                return name + _20_REVIVAL_SUFFIX;
            }
            if (context.revival) {
                return name + OCE_SUFFIX;
            }
        }
        if (context.groupString.equalsIgnoreCase(SURPLICE) && !context.set) {
            name += " (Surplice)";
            if (context.revival) {
                name += _20_REVIVAL_SUFFIX;
            }
            return name;
        }
        if (context.groupString.equalsIgnoreCase(SURPLICE) && context.set) {
            return name + " Set";
        }

        return null;
    }

    private static String buildMythClothDisplayName(BuildContext context) {
        if (!context.lineUpString.equalsIgnoreCase("Myth Cloth")) {
            return null;
        }

        String name = context.name;

        if (name.toLowerCase().contains("hilda") && context.distribution.toLowerCase().contains("stores")) {
            return name + " -The Earth Representative of Odin-";
        }
        if (context.groupString.equalsIgnoreCase(BRONZE_V1)) {
            if (context.manga) {
                return name + " Comic Ver.";
            }
            if (context.anniversary && !context.oce) {
                return name + " 20th Anniversary Ver.";
            }
            if (context.revival) {
                return name + " Early Bronze Cloth" + REVIVAL_SUFFIX;
            }
            if (context.golden) {
                return name + " ~Limited Gold~";
            }
            if (context.oce) {
                return name + OCE_SUFFIX;
            }
            if (!context.seriesString.equalsIgnoreCase("The Lost Canvas")) {
                return name + " (Initial Bronze Cloth)";
            }
        }
        if (context.groupString.equalsIgnoreCase(BRONZE_V2)) {
            if (context.golden) {
                return name + " Power of Gold";
            }
            if (context.broken) {
                return name + " ~Broken Version~";
            }
        }
        if (context.groupString.equalsIgnoreCase(BRONZE_V3)) {
            if (context.gold) {
                return name + " Golden Genealogy";
            }
            if (!context.oce) {
                return name + " (Final Bronze Cloth)";
            }
        }
        if (context.groupString.equalsIgnoreCase(BRONZE_V4)) {
            if (context.anniversary10) {
                return name + " (God Cloth) -10th Anniversary Edition-";
            }
            name += " God Cloth";
            if (context.oce) {
                return name + OCE_SUFFIX;
            }
        }
        if (context.groupString.equalsIgnoreCase(SURPLICE) && !context.oce) {
            return name + " (Surplice)";
        }
        if (context.groupString.equalsIgnoreCase("Specter")) {
            if (context.set) {
                return name + " Complete Set";
            }
        }
        if (context.revival) {
            return name + REVIVAL_SUFFIX;
        }
        if (context.groupString.equalsIgnoreCase(BRONZE_V5)) {
            return name + " (Heaven Chapter)";
        }
        if (context.anniversary15) {
            return name + " 15th Anniversary Ver.";
        }
        if (context.oce) {
            return name + OCE_SUFFIX;
        }

        return null;
    }

    private static String buildAppendixDisplayName(BuildContext context) {
        if (!context.lineUpString.equalsIgnoreCase("Appendix")) {
            return null;
        }

        if (context.oce) {
            return context.name + OCE_SUFFIX;
        }
        if (context.plainCloth) {
            return context.name + " (Plain Cloth)";
        }

        return null;
    }

    private static boolean isAnniversaryEdition(Anniversary anniversary, int year) {
        return Optional.ofNullable(anniversary).map(a -> a.getYear() == year).orElse(false);
    }

    private static final class BuildContext {
        private final String name;
        private final String lineUpString;
        private final String seriesString;
        private final String groupString;
        private final String distribution;
        private final int year;
        private final boolean oce;
        private final boolean revival;
        private final boolean golden;
        private final boolean gold;
        private final boolean set;
        private final boolean manga;
        private final boolean metal;
        private final boolean broken;
        private final boolean plainCloth;
        private final boolean anniversary;
        private final boolean anniversary15;
        private final boolean anniversary10;
        private final boolean anniversary40;

        private BuildContext(String name, String lineUpString, String seriesString, String groupString,
                String distribution, int year, boolean oce, boolean revival, boolean golden, boolean gold, boolean set,
                boolean manga, boolean metal, boolean broken, boolean plainCloth, boolean anniversary,
                boolean anniversary15, boolean anniversary10, boolean anniversary40) {
            this.name = name;
            this.lineUpString = lineUpString;
            this.seriesString = seriesString;
            this.groupString = groupString;
            this.distribution = distribution;
            this.year = year;
            this.oce = oce;
            this.revival = revival;
            this.golden = golden;
            this.gold = gold;
            this.set = set;
            this.manga = manga;
            this.metal = metal;
            this.broken = broken;
            this.plainCloth = plainCloth;
            this.anniversary = anniversary;
            this.anniversary15 = anniversary15;
            this.anniversary10 = anniversary10;
            this.anniversary40 = anniversary40;
        }

        private static BuildContext from(Figurine figurine) {
            String name = figurine.getNormalizedName();
            String lineUpString = Optional.ofNullable(figurine.getLineup()).map(Descriptive::getDescription).orElse("");
            String seriesString = Optional.ofNullable(figurine.getSeries()).map(Descriptive::getDescription).orElse("");
            String groupString = Optional.ofNullable(figurine.getGroup()).map(Descriptive::getDescription).orElse("");
            String distribution = Optional.ofNullable(figurine.getDistribution()).map(Descriptive::getDescription)
                    .orElse("");
            int year = Optional.ofNullable(figurine.getDistributors())
                    .map(list -> list.isEmpty() ? null : list.getFirst().getReleaseDate()).map(LocalDate::getYear)
                    .orElse(0);

            boolean oce = Optional.ofNullable(figurine.getOce()).orElse(false);
            boolean revival = Optional.ofNullable(figurine.getRevival()).orElse(false);
            boolean golden = Optional.ofNullable(figurine.getGolden()).orElse(false);
            boolean gold = Optional.ofNullable(figurine.getGold()).orElse(false);
            boolean set = Optional.ofNullable(figurine.getSet()).orElse(false);
            boolean manga = Optional.ofNullable(figurine.getManga()).orElse(false);
            boolean metal = Optional.ofNullable(figurine.getMetalBody()).orElse(false);
            boolean broken = Optional.ofNullable(figurine.getBroken()).orElse(false);
            boolean plainCloth = Optional.ofNullable(figurine.getPlainCloth()).orElse(false);
            boolean anniversary = Optional.ofNullable(figurine.getAnniversary()).isPresent();
            boolean anniversary15 = isAnniversaryEdition(figurine.getAnniversary(), 15);
            boolean anniversary10 = isAnniversaryEdition(figurine.getAnniversary(), 10);
            boolean anniversary40 = isAnniversaryEdition(figurine.getAnniversary(), 40);

            return new BuildContext(name, lineUpString, seriesString, groupString, distribution, year, oce, revival,
                    golden, gold, set, manga, metal, broken, plainCloth, anniversary, anniversary15, anniversary10,
                    anniversary40);
        }
    }
}
