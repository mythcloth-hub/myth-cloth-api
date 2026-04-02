package com.mesofi.mythclothapi.figurines.imports;

import java.io.IOException;
import java.io.Reader;

@FunctionalInterface
public interface FigurineCsvSource {
  Reader openReader() throws IOException;
}
