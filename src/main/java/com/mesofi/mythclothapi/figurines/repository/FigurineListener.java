package com.mesofi.mythclothapi.figurines.repository;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import com.mesofi.mythclothapi.figurines.FigurineDisplayNameBuilder;
import com.mesofi.mythclothapi.figurines.model.Figurine;

/**
 * JPA entity listener that automatically updates a figurine's display name
 * before it is persisted or updated.
 * <p>
 * The display name is derived from the figurine's attributes using
 * {@link FigurineDisplayNameBuilder}, ensuring it remains synchronized with the
 * underlying data whenever the entity is saved.
 */
public class FigurineListener {

    /**
     * Recalculates and updates the display name for the given figurine before it is
     * persisted or updated.
     *
     * @param figurine
     *            the figurine whose display name should be refreshed
     */
    @PrePersist
    @PreUpdate
    public void updateDisplayName(Figurine figurine) {
        figurine.setDisplayName(FigurineDisplayNameBuilder.build(figurine));
    }
}
