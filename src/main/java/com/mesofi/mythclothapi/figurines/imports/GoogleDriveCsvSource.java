package com.mesofi.mythclothapi.figurines.imports;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;

import org.springframework.stereotype.Component;

import com.mesofi.mythclothapi.figurines.FigurineImportProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GoogleDriveCsvSource implements FigurineCsvSource {

  private final FigurineImportProperties properties;

  @Override
  public Reader openReader() throws IOException {
    String url = properties.buildUrl();
    return new InputStreamReader(URI.create(url).toURL().openStream());
  }
}
