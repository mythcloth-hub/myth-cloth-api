package com.mesofi.mythclothapi.figurines;

import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.ANNOUNCED;
import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.RELEASED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

import org.junit.jupiter.api.Test;

class FigurineFilterFactoryTest {

    @Test
    void shouldBuildFilterWithTrimmedNameAndPreserveAllValues() {
        FigurineFilter filter = FigurineFilterFactory.build(List.of(10L, 20L), "  Pegasus Seiya  ", 1L, 2L, 3L, 4L, 5L,
                true, false, true, false, true, false, true, false, true, false, "RELEASED", true);

        assertEquals(List.of(10L, 20L), filter.figurineIds());
        assertEquals("Pegasus Seiya", filter.name());
        assertEquals(1L, filter.lineUpId());
        assertEquals(2L, filter.seriesId());
        assertEquals(3L, filter.groupId());
        assertEquals(4L, filter.distributionId());
        assertEquals(5L, filter.anniversaryId());
        assertTrue(filter.metalBody());
        assertFalse(filter.oce());
        assertTrue(filter.revival());
        assertFalse(filter.plainCloth());
        assertTrue(filter.broken());
        assertFalse(filter.golden());
        assertTrue(filter.gold());
        assertFalse(filter.manga());
        assertTrue(filter.set());
        assertFalse(filter.articulable());
        assertEquals(List.of("RELEASED"), filter.releaseStatuses());
        assertTrue(filter.restocks());
    }

    @Test
    void shouldUseEmptyNameWhenTrimmedNameIsTooShortOrNull() {
        FigurineFilter shortFilter = FigurineFilterFactory.build(null, " ab ", null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, (String) null, null);
        FigurineFilter exactThreeFilter = FigurineFilterFactory.build(null, " abc ", null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, (String) null, null);
        FigurineFilter nullFilter = FigurineFilterFactory.build(null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, (String) null, null);

        assertEquals("", shortFilter.name());
        assertEquals("abc", exactThreeFilter.name());
        assertEquals("", nullFilter.name());
    }

    @Test
    void shouldBuildFilterFromSingleReleaseStatusAndUseNullStatusAsEmptyList() {
        FigurineFilter statusFilter = FigurineFilterFactory.build(List.of(5L), "  Iron  ", 1L, 2L, 3L, 4L, 6L, null,
                null, null, null, null, null, null, null, null, null, "RELEASED", null);
        FigurineFilter nullStatusFilter = FigurineFilterFactory.build(List.of(8L), "  Titan  ", null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, (String) null, false);

        assertEquals(List.of("RELEASED"), statusFilter.releaseStatuses());
        assertEquals("Iron", statusFilter.name());
        assertEquals(List.of(5L), statusFilter.figurineIds());
        assertEquals(List.of(8L), nullStatusFilter.figurineIds());
        assertEquals("Titan", nullStatusFilter.name());
        assertFalse(nullStatusFilter.restocks());
        assertNull(nullStatusFilter.releaseStatuses());
    }

    @Test
    void shouldBuildReleasedAndAnnouncedFiltersWithAndWithoutRestocks() {
        FigurineFilter bothStatuses = FigurineFilterFactory.buildReleasedAndAnnounced();
        FigurineFilter filteredRestocks = FigurineFilterFactory.buildReleasedAndAnnounced(false);

        assertEquals(List.of(RELEASED.name(), ANNOUNCED.name()), bothStatuses.releaseStatuses());
        assertEquals(List.of(RELEASED.name(), ANNOUNCED.name()), filteredRestocks.releaseStatuses());
        assertNull(bothStatuses.restocks());
        assertFalse(filteredRestocks.restocks());
    }

    @Test
    void shouldHavePrivateConstructor() throws Exception {
        Constructor<FigurineFilterFactory> constructor = FigurineFilterFactory.class.getDeclaredConstructor();

        assertFalse(constructor.canAccess(null));
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));

        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
