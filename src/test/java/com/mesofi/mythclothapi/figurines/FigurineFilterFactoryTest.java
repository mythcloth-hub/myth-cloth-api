package com.mesofi.mythclothapi.figurines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

class FigurineFilterFactoryTest {

  @Test
  void shouldBuildFilterWithTrimmedName() {
    FigurineFilter filter =
        FigurineFilterFactory.build(
            null,
            "  Pegasus Seiya  ",
            1L,
            2L,
            3L,
            4L,
            true,
            false,
            true,
            false,
            true,
            false,
            true,
            false,
            true,
            false,
            "RELEASED");

    assertEquals("Pegasus Seiya", filter.name());
    assertEquals(1L, filter.lineUpId());
    assertEquals(2L, filter.seriesId());
    assertEquals(3L, filter.groupId());
    assertEquals(4L, filter.anniversaryId());
    assertEquals(true, filter.metalBody());
    assertEquals(false, filter.oce());
    assertEquals(true, filter.revival());
    assertEquals(false, filter.plainCloth());
    assertEquals(true, filter.broken());
    assertEquals(false, filter.golden());
    assertEquals(true, filter.gold());
    assertEquals(false, filter.manga());
    assertEquals(true, filter.set());
    assertEquals(false, filter.articulable());
    assertEquals("RELEASED", filter.releaseStatus());
  }

  @Test
  void shouldUseEmptyNameWhenTrimmedNameIsShorterThanThreeCharacters() {
    FigurineFilter filter =
        FigurineFilterFactory.build(
            null, " ab ", null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null);

    assertEquals("", filter.name());
  }

  @Test
  void shouldUseTrimmedNameWhenTrimmedNameHasExactlyThreeCharacters() {
    FigurineFilter filter =
        FigurineFilterFactory.build(
            null, " abc ", null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null);

    assertEquals("abc", filter.name());
  }

  @Test
  void shouldUseEmptyNameWhenNameIsNull() {
    FigurineFilter filter =
        FigurineFilterFactory.build(
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null);

    assertEquals("", filter.name());
  }

  @Test
  void shouldHavePrivateConstructor() throws Exception {
    Constructor<FigurineFilterFactory> constructor =
        FigurineFilterFactory.class.getDeclaredConstructor();

    assertFalse(constructor.canAccess(null));
    assertEquals(true, Modifier.isPrivate(constructor.getModifiers()));

    constructor.setAccessible(true);
    constructor.newInstance();
  }
}
