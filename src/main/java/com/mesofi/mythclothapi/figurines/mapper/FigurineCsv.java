package com.mesofi.mythclothapi.figurines.mapper;

import java.time.LocalDate;
import java.util.List;

import com.mesofi.mythclothapi.catalogs.model.Anniversary;
import com.mesofi.mythclothapi.catalogs.model.Distribution;
import com.mesofi.mythclothapi.catalogs.model.Group;
import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.catalogs.model.Series;
import com.mesofi.mythclothapi.figurines.mapper.converters.AmountConverter;
import com.mesofi.mythclothapi.figurines.mapper.converters.ListStringConverter;
import com.mesofi.mythclothapi.figurines.mapper.converters.LocalDateConfirmedConverter;
import com.mesofi.mythclothapi.figurines.mapper.converters.LocalDateConverter;
import com.mesofi.mythclothapi.figurines.mapper.converters.TrueFalseConverter;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FigurineCsv {
  @CsvBindByName(column = "Myth Cloth Original Name")
  private String originalName;

  @CsvBindByName(column = "Base Name", required = true)
  private String baseName;

  @CsvCustomBindByName(column = "Price (JPY)", converter = AmountConverter.class)
  private Double priceJPY;

  @CsvCustomBindByName(column = "Announcement (JPY)", converter = LocalDateConverter.class)
  private LocalDate announcementJPY;

  @CsvCustomBindByName(column = "Preorder (JPY)", converter = LocalDateConverter.class)
  private LocalDate preorderJPY;

  @CsvCustomBindByName(column = "Release (JPY)", converter = LocalDateConfirmedConverter.class)
  private LocalDateConfirmed releaseJPY;

  @CsvCustomBindByName(column = "Price (MXN)", converter = AmountConverter.class)
  private Double priceMXN;

  @CsvCustomBindByName(column = "Preorder (MXN)", converter = LocalDateConverter.class)
  private LocalDate preorderMXN;

  @CsvCustomBindByName(column = "Release (MXN)", converter = LocalDateConverter.class)
  private LocalDate releaseMXN;

  @CsvBindByName(column = "Link")
  private String tamashiiUrl;

  @CsvBindByName(column = "Distribution")
  private String distributionString;

  private Distribution distribution;

  @CsvBindByName(column = "LineUp")
  private String lineupString;

  private LineUp lineup;

  @CsvBindByName(column = "Series")
  private String seriesString;

  private Series series;

  @CsvBindByName(column = "Group")
  private String groupString;

  private Group group;

  @CsvBindByName(column = "Anniversary")
  private Integer anniversaryNumber;

  private Anniversary anniversary;

  @CsvCustomBindByName(column = "Metal", converter = TrueFalseConverter.class)
  private boolean metalBody;

  @CsvCustomBindByName(column = "OCE", converter = TrueFalseConverter.class)
  private boolean oce;

  @CsvCustomBindByName(column = "Revival", converter = TrueFalseConverter.class)
  private boolean revival;

  @CsvCustomBindByName(column = "PlainCloth", converter = TrueFalseConverter.class)
  private boolean plainCloth;

  @CsvCustomBindByName(column = "Broken", converter = TrueFalseConverter.class)
  private boolean broken;

  @CsvCustomBindByName(column = "Golden", converter = TrueFalseConverter.class)
  private boolean golden;

  @CsvCustomBindByName(column = "Gold", converter = TrueFalseConverter.class)
  private boolean gold;

  @CsvCustomBindByName(column = "HK", converter = TrueFalseConverter.class)
  private boolean hk;

  @CsvCustomBindByName(column = "Manga", converter = TrueFalseConverter.class)
  private boolean manga;

  @CsvCustomBindByName(column = "Surplice", converter = TrueFalseConverter.class)
  private boolean surplice;

  @CsvCustomBindByName(column = "Set", converter = TrueFalseConverter.class)
  private boolean set;

  @CsvCustomBindByName(column = "Static", converter = TrueFalseConverter.class)
  private boolean articulable;

  @CsvBindByName(column = "Remarks")
  private String remarks;

  @CsvCustomBindByName(column = "Official Images", converter = ListStringConverter.class)
  private List<String> officialImages;

  @CsvCustomBindByName(column = "Other Images", converter = ListStringConverter.class)
  private List<String> nonOfficialImages;
}
