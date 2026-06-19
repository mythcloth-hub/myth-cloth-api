package com.mesofi.mythclothapi.collectorscollections.dto;

public enum CollectionAssignmentMode {

  /** Use existing selected collections. */
  EXISTING,

  /** Always create a new collection. */
  CREATE,

  /** If no collections exist, create one. Otherwise, use selected existing collections. */
  AUTO
}
