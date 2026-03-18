package com.mesofi.mythclothapi.figurines.mapper.converters;

public class PipeListStringConverter extends ListStringConverter {
  @Override
  String getDelimiter() {
    return "\\|";
  }
}
