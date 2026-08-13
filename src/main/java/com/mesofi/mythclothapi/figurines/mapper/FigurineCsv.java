package com.mesofi.mythclothapi.figurines.mapper;

import java.time.LocalDate;
import java.util.List;

import com.mesofi.mythclothapi.figurines.mapper.converters.AmountConverter;
import com.mesofi.mythclothapi.figurines.mapper.converters.AnniversaryNumberTypeConverter;
import com.mesofi.mythclothapi.figurines.mapper.converters.CommaListStringConverter;
import com.mesofi.mythclothapi.figurines.mapper.converters.LocalDateConfirmedConverter;
import com.mesofi.mythclothapi.figurines.mapper.converters.LocalDateConverter;
import com.mesofi.mythclothapi.figurines.mapper.converters.PipeListStringConverter;
import com.mesofi.mythclothapi.figurines.mapper.converters.TrueFalseConverter;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a figurine record imported from the CSV catalog.
 *
 * <p>
 * Each field is mapped to a CSV column using OpenCSV annotations. Custom
 * converters are used where the source data requires normalization, such as
 * monetary amounts, dates, anniversary values, boolean flags, and delimited
 * lists.
 *
 * <p>
 * This class serves as an intermediate import model and is later converted into
 * a {@link com.mesofi.mythclothapi.figurines.model.Figurine} entity by the
 * figurine mapper.
 */
@Getter
@Setter
public class FigurineCsv {

    /** Original figurine name as provided by the source catalog. */
    @CsvBindByName(column = "Myth Cloth Original Name")
    private String originalName;

    /** Base figurine name used as the normalized catalog name. */
    @CsvBindByName(column = "Base Name", required = true)
    private String baseName;

    /** Japanese retail price. */
    @CsvCustomBindByName(column = "Price (JPY)", converter = AmountConverter.class)
    private Double priceJPY;

    /** Japanese announcement date. */
    @CsvCustomBindByName(column = "Announcement (JPY)", converter = LocalDateConverter.class)
    private LocalDate announcementJPY;

    /** Japanese preorder opening date. */
    @CsvCustomBindByName(column = "Preorder (JPY)", converter = LocalDateConverter.class)
    private LocalDate preorderJPY;

    /**
     * Japanese release date and whether the release date was explicitly confirmed
     * in the source data.
     */
    @CsvCustomBindByName(column = "Release (JPY)", converter = LocalDateConfirmedConverter.class)
    private LocalDateConfirmed releaseJPY;

    /** Mexican retail price. */
    @CsvCustomBindByName(column = "Price (MXN)", converter = AmountConverter.class)
    private Double priceMXN;

    /** Mexican preorder opening date. */
    @CsvCustomBindByName(column = "Preorder (MXN)", converter = LocalDateConverter.class)
    private LocalDate preorderMXN;

    /**
     * Mexican release date and whether the release date was explicitly confirmed in
     * the source data.
     */
    @CsvCustomBindByName(column = "Release (MXN)", converter = LocalDateConfirmedConverter.class)
    private LocalDateConfirmed releaseMXN;

    /** URL of the corresponding figurine on the Tamashii Nations website. */
    @CsvBindByName(column = "Link")
    private String tamashiiUrl;

    /** Distribution category associated with the figurine. */
    @CsvBindByName(column = "Distribution")
    private String distributionString;

    /** Lineup to which the figurine belongs. */
    @CsvBindByName(column = "LineUp")
    private String lineupString;

    /** Series to which the figurine belongs. */
    @CsvBindByName(column = "Series")
    private String seriesString;

    /** Group or character grouping associated with the figurine. */
    @CsvBindByName(column = "Group")
    private String groupString;

    /**
     * Anniversary year and optional anniversary type associated with the figurine.
     */
    @CsvCustomBindByName(column = "Anniversary", converter = AnniversaryNumberTypeConverter.class)
    private AnniversaryNumberType anniversaryNumberType;

    /** Indicates whether the figurine has a metal body. */
    @CsvCustomBindByName(column = "Metal", converter = TrueFalseConverter.class)
    private boolean metalBody;

    /** Indicates whether the figurine is an Original Color Edition (OCE). */
    @CsvCustomBindByName(column = "OCE", converter = TrueFalseConverter.class)
    private boolean oce;

    /** Indicates whether the figurine is a revival release. */
    @CsvCustomBindByName(column = "Revival", converter = TrueFalseConverter.class)
    private boolean revival;

    /** Indicates whether the release includes plain cloth components. */
    @CsvCustomBindByName(column = "PlainCloth", converter = TrueFalseConverter.class)
    private boolean plainCloth;

    /**
     * Indicates whether the figurine represents a broken or battle-damaged version.
     */
    @CsvCustomBindByName(column = "Broken", converter = TrueFalseConverter.class)
    private boolean broken;

    /** Indicates whether the figurine features a golden armor variant. */
    @CsvCustomBindByName(column = "Golden", converter = TrueFalseConverter.class)
    private boolean golden;

    /** Indicates whether the figurine is a 24k gold edition. */
    @CsvCustomBindByName(column = "Gold", converter = TrueFalseConverter.class)
    private boolean gold;

    /** Indicates whether the figurine is associated with the Hong Kong market. */
    @CsvCustomBindByName(column = "HK", converter = TrueFalseConverter.class)
    private boolean hk;

    /** Indicates whether the figurine is based on the manga version. */
    @CsvCustomBindByName(column = "Manga", converter = TrueFalseConverter.class)
    private boolean manga;

    /** Indicates whether the CSV entry represents a multi-figurine set. */
    @CsvCustomBindByName(column = "Set", converter = TrueFalseConverter.class)
    private boolean set;

    /**
     * Indicates whether the figurine is a static, non-articulable release.
     *
     * <p>
     * The source CSV column is named {@code Static}; the associated converter
     * transforms this value into the {@code articulable} property.
     */
    @CsvCustomBindByName(column = "Static", converter = TrueFalseConverter.class)
    private boolean articulable;

    /** Additional remarks or descriptive information about the figurine. */
    @CsvBindByName(column = "Remarks")
    private String remarks;

    /**
     * Event definitions associated with the figurine.
     *
     * <p>
     * Individual events are separated by the pipe character in the source CSV.
     */
    @CsvCustomBindByName(column = "Events", converter = PipeListStringConverter.class)
    private List<String> events;

    /** URLs of official images associated with the figurine. */
    @CsvCustomBindByName(column = "Official Images", converter = CommaListStringConverter.class)
    private List<String> officialImages;

    /** URLs of non-official images associated with the figurine. */
    @CsvCustomBindByName(column = "Other Images", converter = CommaListStringConverter.class)
    private List<String> nonOfficialImages;
}