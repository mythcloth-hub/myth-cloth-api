package com.mesofi.mythclothapi.figurines.repository;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import com.mesofi.mythclothapi.figurines.FigurineDisplayNameBuilder;
import com.mesofi.mythclothapi.figurines.FigurineReleaseStatusResolver;
import com.mesofi.mythclothapi.figurines.model.Figurine;

/**
 * JPA entity listener that automatically derives and updates computed figurine
 * properties before the entity is persisted or updated.
 * <p>
 * The listener ensures that the figurine's display name and current release
 * status remain synchronized with its underlying data by recalculating them
 * whenever the entity is saved.
 *
 * @see FigurineDisplayNameBuilder
 * @see FigurineReleaseStatusResolver
 */
public class FigurineListener {

    /**
     * Recalculates the computed properties for the supplied figurine before it is
     * persisted or updated.
     * <p>
     * The following properties are refreshed:
     * <ul>
     * <li>The display name, using {@link FigurineDisplayNameBuilder}.</li>
     * <li>The current release status, using
     * {@link FigurineReleaseStatusResolver}.</li>
     * </ul>
     *
     * @param figurine
     *            the figurine whose computed properties should be refreshed
     */
    @PrePersist
    @PreUpdate
    public void refreshDerivedProperties(Figurine figurine) {
        figurine.setDisplayName(FigurineDisplayNameBuilder.build(figurine));
        figurine.setCurrentReleaseStatus(FigurineReleaseStatusResolver.resolve(figurine));
    }
}
