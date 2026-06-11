package com.mesofi.mythclothapi.figurines;

/**
 * Factory utility for constructing {@link FigurineFilter} instances from request parameters.
 *
 * <p>This class centralizes the logic for building figurine filter records, ensuring consistent
 * parameter validation and name normalization across all controllers.
 */
public final class FigurineFilterFactory {
  /**
   * Private constructor to prevent instantiation of this utility class.
   *
   * <p>All functionality is exposed through static factory methods.
   */
  private FigurineFilterFactory() {}

  /**
   * Constructs a {@link FigurineFilter} from the provided request parameters.
   *
   * <p>The name parameter is trimmed and validated to have at least 3 characters. If it does not
   * meet this requirement, an empty string is used instead.
   *
   * @param name optional name filter
   * @param lineUpId optional lineup identifier
   * @param seriesId optional series identifier
   * @param groupId optional group identifier
   * @param anniversaryId optional anniversary identifier
   * @param metalBody optional metal body flag
   * @param oce optional OCE flag
   * @param revival optional revival flag
   * @param plainCloth optional plain cloth flag
   * @param broken optional broken flag
   * @param golden optional golden flag
   * @param gold optional gold flag
   * @param manga optional manga flag
   * @param set optional set flag
   * @param articulable optional articulable flag
   * @param releaseStatus optional release status filter
   * @return a new {@link FigurineFilter} instance
   */
  public static FigurineFilter build(
      String name,
      Long lineUpId,
      Long seriesId,
      Long groupId,
      Long anniversaryId,
      Boolean metalBody,
      Boolean oce,
      Boolean revival,
      Boolean plainCloth,
      Boolean broken,
      Boolean golden,
      Boolean gold,
      Boolean manga,
      Boolean set,
      Boolean articulable,
      String releaseStatus) {

    String figurineName = name != null && name.trim().length() >= 3 ? name.trim() : "";

    return new FigurineFilter(
        figurineName,
        lineUpId,
        seriesId,
        groupId,
        anniversaryId,
        metalBody,
        oce,
        revival,
        plainCloth,
        broken,
        golden,
        gold,
        manga,
        set,
        articulable,
        releaseStatus);
  }
}
