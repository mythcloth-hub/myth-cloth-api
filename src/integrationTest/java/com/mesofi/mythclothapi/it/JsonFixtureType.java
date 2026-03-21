package com.mesofi.mythclothapi.it;

/**
 * Defines the type of JSON fixture used in scenario-driven integration tests.
 *
 * <p>Each fixture type maps to a dedicated folder on disk where JSON files are stored. The folder
 * name is used by the test infrastructure to resolve the correct fixture path at runtime.
 */
public enum JsonFixtureType {

  /** JSON fixture representing an HTTP request payload. */
  REQUEST("request"),

  /** JSON fixture representing an expected HTTP response payload. */
  RESPONSE("response");

  private final String folder;

  JsonFixtureType(String folder) {
    this.folder = folder;
  }

  /**
   * Returns the folder name associated with this fixture type.
   *
   * @return fixture folder name
   */
  public String folder() {
    return folder;
  }
}
