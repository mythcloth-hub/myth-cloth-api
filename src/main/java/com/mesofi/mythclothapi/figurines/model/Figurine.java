package com.mesofi.mythclothapi.figurines.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import com.mesofi.mythclothapi.anniversaries.model.Anniversary;
import com.mesofi.mythclothapi.catalogs.model.Distribution;
import com.mesofi.mythclothapi.catalogs.model.Group;
import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.catalogs.model.Series;
import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine;
import com.mesofi.mythclothapi.common.Auditable;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEvent;
import com.mesofi.mythclothapi.figurines.repository.FigurineListener;
import com.mesofi.mythclothapi.figurinestores.model.FigurineStore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a collectible figurine managed by the application.
 *
 * <p>
 * This entity is the central representation of a figurine in the catalog. It
 * contains the figurine's identifying information, catalog classifications,
 * release status, physical characteristics, distributor information, store
 * information, events, images, and relationships to previous or subsequent
 * releases.
 * </p>
 *
 * <p>
 * Figurines may also be associated with collector collections, anniversaries,
 * distributions, and catalog classifications such as lineup, series, and group.
 * </p>
 *
 * <p>
 * The entity is audited through {@link Auditable} and uses
 * {@link FigurineListener} for entity lifecycle processing.
 * </p>
 *
 * @see Auditable
 * @see FigurineListener
 * @see ReleaseStatus
 */
@Entity
@EntityListeners(FigurineListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "figurines", indexes = @Index(name = "idx_figurine_normalized_name", columnList = "normalizedName"), comment = "Stores all collectible figurines managed by the application. "
        + "This is the central repository for all figurine records.")
public class Figurine extends Auditable {

    /**
     * Original figurine name imported from the initial CSV dataset.
     *
     * <p>
     * This value is preserved for traceability and migration purposes and cannot be
     * modified after the figurine is created.
     * </p>
     */
    @Column(unique = true, length = 200, updatable = false, comment = "Original figurine name imported from the initial CSV dataset "
            + "(\"Myth Cloth Original Name\" column). Indexed to optimize search "
            + "performance and enforce uniqueness across the catalog. Preserved "
            + "only for traceability and migration purposes.")
    private String legacyName;

    /**
     * Base figurine name without variants or additional attributes.
     *
     * <p>
     * Used internally for normalization, comparison, matching, and search
     * operations.
     * </p>
     */
    @Column(nullable = false, length = 100, comment = "Base figurine name without variants or attributes. This is used "
            + "to create the normalized name for internal processing and search optimization.")
    private String normalizedName;

    /**
     * Complete figurine name displayed throughout the catalog.
     *
     * <p>
     * Includes applicable variants or attributes such as God Cloth, OCE, or
     * Revival.
     * </p>
     */
    @Column(nullable = false, length = 200, comment = "Complete figurine name used throughout the catalog and displayed "
            + "to users. Includes all applicable variants or attributes " + "(e.g. God Cloth, OCE, Revival).")
    private String displayName;

    /**
     * Distributors associated with this figurine.
     *
     * <p>
     * Distributor associations are owned by this figurine and are automatically
     * persisted and removed with it.
     * </p>
     */
    @OneToMany(mappedBy = "figurine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FigurineDistributor> distributors = new ArrayList<>();

    /**
     * Collector collections containing this figurine.
     *
     * <p>
     * Collection associations are owned by this figurine and are automatically
     * persisted and removed with it.
     * </p>
     */
    @OneToMany(mappedBy = "figurine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollectorCollectionFigurine> collections = new ArrayList<>();

    /**
     * Stores associated with this figurine.
     *
     * <p>
     * Store associations are owned by this figurine and are automatically persisted
     * and removed with it.
     * </p>
     */
    @OneToMany(mappedBy = "figurine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FigurineStore> stores = new ArrayList<>();

    /**
     * URL of the corresponding figurine information on the Tamashii Nations
     * website.
     */
    @Column(length = 50)
    private String tamashiiUrl;

    /**
     * Distribution associated with the figurine.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private Distribution distribution;

    /**
     * Lineup to which the figurine belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private LineUp lineup;

    /**
     * Series to which the figurine belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Series series;

    /**
     * Optional group to which the figurine belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private Group group;

    /**
     * Optional anniversary associated with the figurine.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private Anniversary anniversary;

    /**
     * Current release status of the figurine.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReleaseStatus currentReleaseStatus;

    /**
     * Indicates whether the figurine has a metal body.
     */
    @Column(name = "is_metal_body")
    private Boolean metalBody;

    /**
     * Indicates whether the figurine is an Original Color Edition (OCE).
     */
    @Column(name = "is_oce")
    private Boolean oce;

    /**
     * Indicates whether the figurine is a Revival release.
     */
    @Column(name = "is_revival")
    private Boolean revival;

    /**
     * Indicates whether the figurine includes plain cloth.
     */
    @Column(name = "is_plain_cloth")
    private Boolean plainCloth;

    /**
     * Indicates whether the figurine represents a broken version.
     */
    @Column(name = "is_broken")
    private Boolean broken;

    /**
     * Indicates whether the figurine has a golden variant.
     */
    @Column(name = "is_golden")
    private Boolean golden;

    /**
     * Indicates whether the figurine has a gold variant.
     */
    @Column(name = "is_gold")
    private Boolean gold;

    /**
     * Indicates whether the figurine is based on a manga version.
     */
    @Column(name = "is_manga")
    private Boolean manga;

    /**
     * Indicates whether the figurine is part of a set.
     */
    @Column(name = "is_set")
    private Boolean set;

    /**
     * Indicates whether the figurine is articulable.
     */
    @Column(name = "is_articulable")
    private Boolean articulable;

    /**
     * Additional remarks or descriptive notes about the figurine.
     */
    @Column(length = 1500)
    private String remarks;

    /**
     * Events associated with this figurine.
     *
     * <p>
     * Events are ordered from the most recent event date to the oldest.
     * </p>
     */
    @OneToMany(mappedBy = "figurine", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("eventDate DESC")
    private List<FigurineEvent> events = new ArrayList<>();

    /**
     * Previous release represented by this figurine.
     *
     * <p>
     * This relationship is used to associate a figurine with an earlier release
     * when the current figurine represents a subsequent release or restock.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_release_id")
    private Figurine previousRelease;

    /**
     * Figurines that identify this figurine as their previous release.
     */
    @OneToMany(mappedBy = "previousRelease")
    private List<Figurine> subsequentReleases = new ArrayList<>();

    /**
     * Official images associated with the figurine.
     */
    @ElementCollection
    @CollectionTable(name = "official_images", joinColumns = @JoinColumn(name = "figurine_id"))
    private List<String> officialImages;

    /**
     * Non-official images associated with the figurine.
     */
    @ElementCollection
    @CollectionTable(name = "non_official_images", joinColumns = @JoinColumn(name = "figurine_id"))
    private List<String> nonOfficialImages;

    /**
     * Returns the normalized figurine name as its string representation.
     *
     * @return the normalized figurine name
     */
    @Override
    public String toString() {
        return normalizedName;
    }
}