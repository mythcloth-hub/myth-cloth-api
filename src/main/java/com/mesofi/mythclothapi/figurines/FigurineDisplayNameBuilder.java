package com.mesofi.mythclothapi.figurines;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.mesofi.mythclothapi.anniversaries.model.Anniversary;
import com.mesofi.mythclothapi.anniversaries.model.AnniversaryType;
import com.mesofi.mythclothapi.catalogs.model.GroupType;
import com.mesofi.mythclothapi.catalogs.model.LineUpType;
import com.mesofi.mythclothapi.catalogs.model.SeriesType;
import com.mesofi.mythclothapi.common.Descriptive;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurines.model.Figurine;

/**
 * Builds the display name for a {@link Figurine} according to the official
 * Saint Seiya Myth Cloth naming conventions.
 * <p>
 * Display names vary depending on the figurine's product line, series, group,
 * release information, anniversary editions, and special edition flags (such as
 * Original Color Edition, Revival, Golden Limited Edition, and Manga Version).
 * This class centralizes all naming rules so they remain consistent throughout
 * the application.
 * <p>
 * The builder first converts descriptive catalog values into their
 * corresponding enum types, creates an immutable {@link BuildContext}, and then
 * delegates the formatting to a line-up specific builder.
 */
public final class FigurineDisplayNameBuilder {

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

    private static final List<Map.Entry<String, LineUpType>> LINE_UP_MAPPINGS = List.of(
            Map.entry("myth cloth ex", LineUpType.MYTH_CLOTH_EX), Map.entry("myth cloth", LineUpType.MYTH_CLOTH),
            Map.entry("appendix", LineUpType.APPENDIX), Map.entry("panoramation", LineUpType.DD_PANORAMATION),
            Map.entry("legend", LineUpType.SAINT_CLOTH_LEGEND), Map.entry("crown", LineUpType.SAINT_CLOTH_CROWN),
            Map.entry("zero", LineUpType.FIGUARTS_ZERO), Map.entry("figuarts", LineUpType.FIGUARTS),
            Map.entry("box", LineUpType.TAMASHII_NATIONS_BOX), Map.entry("action", LineUpType.SAINT_CLOTH_ACTION),
            Map.entry("rebirth", LineUpType.SAINT_CLOTH_REBIRTH), Map.entry("series", LineUpType.SAINT_CLOTH_SERIES),
            Map.entry("metalbuild", LineUpType.METALBUILD_EX_PROJECT));

    private static final List<Map.Entry<String, SeriesType>> SERIES_MAPPINGS = List.of(
            Map.entry("soul", SeriesType.SOUL_OF_GOLD), Map.entry("beginning", SeriesType.SS_THE_BEGINNING),
            Map.entry("saintia", SeriesType.SAINTIA_SHO), Map.entry("lost", SeriesType.LOST_CANVAS),
            Map.entry("legend", SeriesType.LEGEND_OF_SANCTUARY));

    private static final List<Map.Entry<String, GroupType>> GROUP_MAPPINGS = List.of(
            Map.entry("v1", GroupType.BRONZE_SAINT_V1), Map.entry("v2", GroupType.BRONZE_SAINT_V2),
            Map.entry("v3", GroupType.BRONZE_SAINT_V3), Map.entry("v4", GroupType.BRONZE_SAINT_V4),
            Map.entry("v5", GroupType.BRONZE_SAINT_V5), Map.entry("steel", GroupType.STEEL),
            Map.entry("gold saint", GroupType.GOLD_SAINT), Map.entry("robe", GroupType.GOD_ROBE),
            Map.entry("scale", GroupType.POSEIDON_SCALE), Map.entry("surplice", GroupType.SURPLICE_SAINT),
            Map.entry("specter", GroupType.SPECTER), Map.entry("judge", GroupType.JUDGE),
            Map.entry("god", GroupType.GOD), Map.entry("inheritor", GroupType.INHERITOR),
            Map.entry("accessories", GroupType.ACCESSORIES));

    private FigurineDisplayNameBuilder() {
    }

    /**
     * Builds the display name for the supplied figurine.
     * <p>
     * The formatting strategy is selected according to the figurine's
     * {@link LineUpType}. If the line-up cannot be determined, {@code "-"} is
     * returned.
     *
     * @param figurine
     *            the figurine whose display name will be generated
     * @return the formatted display name
     */
    public static String build(Figurine figurine) {
        BuildContext context = BuildContext.from(figurine);
        LineUpType lineUpType = context.lineUpType;

        return switch (lineUpType) {
            case MYTH_CLOTH_EX -> buildMythClothExLineUp(context);
            case MYTH_CLOTH -> buildMythClothLineUp(context);
            case APPENDIX -> buildAppendixLineUp(context);
            case DD_PANORAMATION -> buildPanoramationLineUp(context);
            case SAINT_CLOTH_LEGEND -> buildLOSLineUp(context);
            case SAINT_CLOTH_CROWN -> buildCrownLineUp(context);
            case FIGUARTS_ZERO -> buildMythClothFiguartsZeroLineUp(context);
            case FIGUARTS -> buildMythClothFiguartsLineUp(context);
            case TAMASHII_NATIONS_BOX -> buildTamashiiNationsBoxLineUp(context);
            case SAINT_CLOTH_ACTION -> buildSaintClothActionLineUp(context);
            case SAINT_CLOTH_REBIRTH -> buildSaintClothRebirthLineUp(context);
            case SAINT_CLOTH_SERIES -> buildSaintClothSeriesLineUp(context);
            case METALBUILD_EX_PROJECT -> buildMetalBuildExProjectLineUp(context);
            case null -> "-";
        };
    }

    private static String buildMythClothExLineUp(BuildContext context) {
        // Bronze Saints
        if (context.groupType == GroupType.BRONZE_SAINT_V1) {
            return "%s [First Bronze Cloth]".formatted(context.name);
        } else if (context.groupType == GroupType.BRONZE_SAINT_V2) {
            if (context.golden) {
                return "%s [New Bronze Cloth] -Golden Limited Edition-".formatted(context.name);
            }
            if (context.revival) {
                return "%s [New Bronze Cloth] <Revival Ver.>".formatted(context.name);
            }
            if (context.oce) {
                if (context.anniversary != null && context.anniversary.getYear() == 40) {
                    return "%s ~(New Bronze Cloth) 40th Anniversary Edition~".formatted(context.name);
                }
                return "%s ~Original Color Edition~".formatted(context.name);
            }
            return "%s ~New Bronze Cloth~".formatted(context.name);
        } else if (context.groupType == GroupType.BRONZE_SAINT_V3) {
            if (context.golden) {
                return "%s [Final Bronze Cloth] ~Golden Limited Edition~".formatted(context.name);
            }
            if (context.oce) {
                return "%s [Final Bronze Cloth] -Original Color Edition-".formatted(context.name);
            } else {
                return "%s [Final Bronze Cloth]".formatted(context.name);
            }
        } else if (context.groupType == GroupType.BRONZE_SAINT_V4) {
            return "%s [God Cloth]".formatted(context.name);

            // Gold Saints
        } else if (context.groupType == GroupType.GOLD_SAINT) {
            if (context.seriesType == SeriesType.LEGEND_OF_SANCTUARY) {
                return "%s ~Legend Of Sanctuary Edition~".formatted(context.name);
            }
            if (context.seriesType == SeriesType.SAINTIA_SHO) {
                return "%s Saintia Sho Color Edition".formatted(context.name);
            }
            if (context.seriesType == SeriesType.SOUL_OF_GOLD) {
                if (context.set) {
                    return "%s God Cloth Saga Saga Premium Set".formatted(context.name);
                }
                return "%s God Cloth".formatted(context.name);
            }
            if (context.revival) {
                if (context.anniversary != null && context.anniversary.getYear() == 20
                        && context.anniversary.getType() == AnniversaryType.SAINT_CLOTH_MYTH) {
                    return "%s <20th Revival Ver.>".formatted(context.name);
                }
                if (context.name.toLowerCase(Locale.ROOT).contains("leo")
                        || context.name.toLowerCase(Locale.ROOT).contains("virgo")) {
                    return "%s <Revival Version>".formatted(context.name);
                } else {
                    return "%s <Revival Ver.>".formatted(context.name);
                }
            }
            if (context.gold) {
                return "%s Gold24".formatted(context.name);
            }
            if (context.oce) {
                return "%s ~Original Color Edition~".formatted(context.name);
            }

            // Asgard
        } else if (context.groupType == GroupType.GOD_ROBE) {
            if (context.seriesType == SeriesType.SOUL_OF_GOLD) {
                return "%s God Robe".formatted(context.name);
            }
            if (context.anniversary != null && context.anniversary.getYear() == 40
                    && context.anniversary.getType() == AnniversaryType.SAINT_SEIYA) {
                return "%s 40th Anniversary Ver.".formatted(context.name);
            }

            // Poseidon
        } else if (context.groupType == GroupType.POSEIDON_SCALE) {
            if (context.name.toLowerCase(Locale.ROOT).contains("sorrento") && context.releaseDate != null
                    && context.releaseDate.getYear() == 2021) {
                return "%s <Asgard Final Battle Ver.>".formatted(context.name);
            }
            if (context.oce) {
                return "%s -Original Color Edition-".formatted(context.name);
            }
            if (context.set) {
                return "%s Imperial Throne Set".formatted(context.name);
            }

            // Surplice
        } else if (context.groupType == GroupType.SURPLICE_SAINT) {
            if (context.revival && context.anniversary != null && context.anniversary.getYear() == 20
                    && context.anniversary.getType() == AnniversaryType.SAINT_CLOTH_MYTH) {
                return "%s (Surplice) <20th Revival Ver.>".formatted(context.name);
            }
            if (context.set) {
                return "%s Set".formatted(context.name);
            }
            return "%s (Surplice)".formatted(context.name);

            // Judge
        } else if (context.groupType == GroupType.JUDGE) {
            if (context.oce) {
                return "%s ~Original Color Edition~".formatted(context.name);
            }

            // Gods
        } else if (context.groupType == GroupType.GOD) {
            if (context.set) {
                return "%s ~Divine Saga Premium Set~".formatted(context.name);
            }

            if (context.oce) {
                if (context.name.toLowerCase(Locale.ROOT).contains("hades")) {
                    return "%s ~Original Color Edition~".formatted(context.name);
                } else {
                    return "%s -Original Color Edition-".formatted(context.name);
                }
            }
            // Inheritor
        } else if (context.groupType == GroupType.INHERITOR) {
            return "%s -Inheritor of the Gold Cloth-".formatted(context.name);
            // Accessories
        } else if (context.groupType == GroupType.ACCESSORIES) {
            if (context.set) {
                return "%s Set".formatted(context.name);
            }
        } else if (context.seriesType == SeriesType.SS_THE_BEGINNING) {
            return "%s -Knights of the Zodiac-".formatted(context.name);
        }

        return context.name;
    }
    private static String buildMythClothLineUp(BuildContext context) {
        // Bronze Saints
        if (context.groupType == GroupType.BRONZE_SAINT_V1) {
            if (context.revival) {
                return "%s [First Bronze Cloth] <Revival Ver.>".formatted(context.name);
            }
            if (context.manga) {
                return "%s Comic Ver.".formatted(context.name);
            }
            if (context.anniversary != null && context.anniversary.getYear() == 20
                    && context.anniversary.getType() == AnniversaryType.SAINT_CLOTH_MYTH) {
                return "%s 20th Anniversary Ver.".formatted(context.name);
            }
            if (context.oce) {
                return "%s ~Original Color Edition~".formatted(context.name);
            }
            if (context.golden) {
                return "%s ~Limited Gold~".formatted(context.name);
            }
            if (context.seriesType != SeriesType.SAINTIA_SHO && context.seriesType != SeriesType.LOST_CANVAS) {
                return "%s ~Initial Bronze Cloth~".formatted(context.name);
            }
        }
        if (context.groupType == GroupType.BRONZE_SAINT_V2) {
            if (context.golden) {
                return "%s ~Power of Gold~".formatted(context.name);
            }
            if (context.broken) {
                return "%s ~Broken Version~".formatted(context.name);
            }
        }
        if (context.groupType == GroupType.BRONZE_SAINT_V3) {
            if (context.oce) {
                return "%s ~Original Color Edition~".formatted(context.name);
            }
            if (context.gold) {
                return "Golden Genealogy %s".formatted(context.name);
            }
            return "%s ~Final Bronze Cloth~".formatted(context.name);
        }
        if (context.groupType == GroupType.BRONZE_SAINT_V4) {
            if (context.anniversary != null && context.anniversary.getYear() == 10
                    && context.anniversary.getType() == AnniversaryType.SAINT_CLOTH_MYTH) {
                return "%s (God Cloth) ~10th Anniversary Ver.~".formatted(context.name);
            }
            if (context.oce) {
                return "%s (God Cloth) ~Original Color Edition~".formatted(context.name);
            } else {
                return "%s (God Cloth)".formatted(context.name);
            }
        }
        if (context.groupType == GroupType.BRONZE_SAINT_V5) {
            return "%s (Heaven Chapter)".formatted(context.name);
        }
        if (context.groupType == GroupType.STEEL || context.groupType == GroupType.POSEIDON_SCALE) {
            if (context.revival) {
                return "%s <Revival Ver.>".formatted(context.name);
            }
            if (context.anniversary != null && context.anniversary.getYear() == 15
                    && context.anniversary.getType() == AnniversaryType.SAINT_CLOTH_MYTH) {
                return "%s 15th Anniversary Ver.".formatted(context.name);
            }
        }

        // Asgard
        if (context.groupType == GroupType.GOD_ROBE) {
            if (context.releaseDate != null && context.releaseDate.getYear() == 2023) {
                return "%s -The Earth Representative of Odin-".formatted(context.name);
            }
            if (context.seriesType == SeriesType.SOUL_OF_GOLD) {
                return "%s God Robe".formatted(context.name);
            }
        }

        // Surplice Saints
        if (context.groupType == GroupType.SURPLICE_SAINT) {
            if (context.set) {
                if (context.oce) {
                    return "%s and Pope Set (Surplice) ~Asia Version~".formatted(context.name);
                } else {
                    return "%s and Pope Set (Surplice)".formatted(context.name);
                }
            }
            return "%s (Surplice)".formatted(context.name);
        }

        // Specters
        if (context.groupType == GroupType.SPECTER) {
            if (context.set) {
                return "%s Complete Set".formatted(context.name);
            }
        }

        // Gods
        if (context.groupType == GroupType.GOD) {
            if (context.anniversary != null && context.anniversary.getYear() == 15
                    && context.anniversary.getType() == AnniversaryType.SAINT_CLOTH_MYTH) {
                return "%s 15th Anniversary Ver.".formatted(context.name);
            }
            if (context.set) {
                return "%s Memorial Set".formatted(context.name);
            }
            if (context.oce) {
                return "%s ~Original Color Edition~".formatted(context.name);
            }
        }
        if (context.revival) {
            return "%s <Revival Ver.>".formatted(context.name);
        }

        return context.name;
    }

    private static String buildAppendixLineUp(BuildContext context) {
        if (context.groupType == GroupType.BRONZE_SAINT_V3) {
            return "%s (Final Bronze Cloth)".formatted(context.name);
        }
        if (context.oce) {
            return "%s ~Original Color Edition~".formatted(context.name);
        }
        if (context.plainCloth) {
            return "%s -Plain Clothes-".formatted(context.name);
        }
        return context.name;
    }

    private static String buildPanoramationLineUp(BuildContext context) {
        String simpleName = context.name.toLowerCase();
        return DD_NAMES.keySet().stream().filter(simpleName::contains).findFirst()
                .map(key -> DD_NAMES.get(key).replace("{name}", context.name)).orElse(context.name);
    }

    private static String buildLOSLineUp(BuildContext context) {
        return "Saint Cloth Legend %s".formatted(context.name);
    }

    private static String buildCrownLineUp(BuildContext context) {
        return context.name;
    }

    private static String buildMythClothFiguartsZeroLineUp(BuildContext context) {
        return "Figuarts Zero Touche Métallique %s".formatted(context.name);
    }

    private static String buildMythClothFiguartsLineUp(BuildContext context) {
        return context.name;
    }

    private static String buildTamashiiNationsBoxLineUp(BuildContext context) {
        return context.name;
    }

    private static String buildSaintClothActionLineUp(BuildContext context) {
        return context.name;
    }

    private static String buildSaintClothRebirthLineUp(BuildContext context) {
        return context.name;
    }

    private static String buildSaintClothSeriesLineUp(BuildContext context) {
        return "Saint Cloth Series %s".formatted(context.name);
    }

    private static String buildMetalBuildExProjectLineUp(BuildContext context) {
        return "MetalBuild EX Project %s".formatted(context.name);
    }

    /**
     * Resolves a {@link LineUpType} from a line-up description.
     * <p>
     * Matching is performed using a case-insensitive substring comparison against
     * the configured mappings.
     *
     * @param lineUpString
     *            the line-up description
     * @return the matching line-up type, or {@code null} if no mapping exists
     */
    private static LineUpType toLineUpType(String lineUpString) {
        String normalized = lineUpString.toLowerCase(Locale.ROOT);

        return LINE_UP_MAPPINGS.stream().filter(entry -> normalized.contains(entry.getKey())).map(Map.Entry::getValue)
                .findFirst().orElse(null);
    }

    /**
     * Resolves a {@link SeriesType} from a series description.
     *
     * @param seriesString
     *            the series description
     * @return the matching series type, or {@code null} if no mapping exists
     */
    private static SeriesType toSeriesType(String seriesString) {
        String normalized = seriesString.toLowerCase(Locale.ROOT);

        return SERIES_MAPPINGS.stream().filter(entry -> normalized.contains(entry.getKey())).map(Map.Entry::getValue)
                .findFirst().orElse(null);
    }

    /**
     * Resolves a {@link GroupType} from a group description.
     *
     * @param groupString
     *            the group description
     * @return the matching group type, or {@code null} if no mapping exists
     */
    private static GroupType toGroupType(String groupString) {
        String normalized = groupString.toLowerCase(Locale.ROOT);

        return GROUP_MAPPINGS.stream().filter(entry -> normalized.contains(entry.getKey())).map(Map.Entry::getValue)
                .findFirst().orElse(null);
    }

    /**
     * Immutable representation of the figurine attributes required to generate a
     * display name.
     * <p>
     * This record extracts and normalizes the relevant information from a
     * {@link Figurine}, allowing the formatting logic to work with strongly typed
     * values instead of repeatedly traversing the domain model.
     */
    private record BuildContext(String name, LineUpType lineUpType, SeriesType seriesType, GroupType groupType,
            LocalDate releaseDate, boolean metal, boolean oce, boolean revival, boolean plainCloth, boolean broken,
            boolean golden, boolean gold, boolean manga, boolean set, Anniversary anniversary) {

        /**
         * Creates a {@link BuildContext} from the supplied figurine.
         * <p>
         * Descriptive catalog values are converted into their corresponding enum types,
         * nullable Boolean properties are normalized to primitive booleans, and the
         * first distributor release date is used when available.
         *
         * @param figurine
         *            the figurine to convert
         * @return an immutable context containing all data required for display name
         *         generation
         */
        private static BuildContext from(Figurine figurine) {
            String name = figurine.getNormalizedName();
            LineUpType lineUpType = Optional.ofNullable(figurine.getLineup()).map(Descriptive::getDescription)
                    .map(FigurineDisplayNameBuilder::toLineUpType).orElse(null);
            SeriesType seriesType = Optional.ofNullable(figurine.getSeries()).map(Descriptive::getDescription)
                    .map(FigurineDisplayNameBuilder::toSeriesType).orElse(null);
            GroupType groupType = Optional.ofNullable(figurine.getGroup()).map(Descriptive::getDescription)
                    .map(FigurineDisplayNameBuilder::toGroupType).orElse(null);

            LocalDate releaseDate = Optional.ofNullable(figurine.getDistributors()).filter(list -> !list.isEmpty())
                    .map(List::getFirst).map(FigurineDistributor::getReleaseDate).orElse(null);

            boolean metal = Optional.ofNullable(figurine.getMetalBody()).orElse(false);
            boolean oce = Optional.ofNullable(figurine.getOce()).orElse(false);
            boolean revival = Optional.ofNullable(figurine.getRevival()).orElse(false);
            boolean plainCloth = Optional.ofNullable(figurine.getPlainCloth()).orElse(false);
            boolean broken = Optional.ofNullable(figurine.getBroken()).orElse(false);
            boolean golden = Optional.ofNullable(figurine.getGolden()).orElse(false);
            boolean gold = Optional.ofNullable(figurine.getGold()).orElse(false);
            boolean manga = Optional.ofNullable(figurine.getManga()).orElse(false);
            boolean set = Optional.ofNullable(figurine.getSet()).orElse(false);

            return new BuildContext(name, lineUpType, seriesType, groupType, releaseDate, metal, oce, revival,
                    plainCloth, broken, golden, gold, manga, set, figurine.getAnniversary());
        }
    }
}
