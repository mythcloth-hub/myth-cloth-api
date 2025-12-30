package com.mesofi.mythclothapi.it;

public enum JsonFixtureType {
  REQUEST("request"),
  RESPONSE("response");

  private final String folder;

  JsonFixtureType(String folder) {
    this.folder = folder;
  }

  public String folder() {
    return folder;
  }
}
