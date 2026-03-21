package com.mesofi.mythclothapi.figurines.mapper.converters;

public class CommaListStringConverter extends ListStringConverter {
  @Override
  String getDelimiter() {
    return ",";
  }
}
