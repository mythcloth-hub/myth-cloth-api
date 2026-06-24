package com.mesofi.mythclothapi.figurines.repository;

import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import lombok.Getter;

/**
 * Custom implementation of a paginated response that extends Spring Data's {@link PageImpl} to
 * include the total number of collectable items.
 *
 * <p>This class is useful when the pagination result needs to expose additional metadata related to
 * collectable figurines, while preserving the standard {@link org.springframework.data.domain.Page}
 * behavior.
 *
 * @param <T> the type of elements contained in this page
 */
@Getter
public class CollectablePageImpl<T> extends PageImpl<T> {
  /** Total number of collectable items available. */
  private final long totalCollectables;

  /**
   * Creates a new {@code CollectablePageImpl} instance.
   *
   * @param content the list of items contained in the current page
   * @param pageable pagination information, including page number and size
   * @param total total number of items available across all pages
   * @param totalCollectables total number of items considered collectable
   */
  public CollectablePageImpl(
      List<T> content, Pageable pageable, long total, long totalCollectables) {
    super(content, pageable, total);
    this.totalCollectables = totalCollectables;
  }
}
