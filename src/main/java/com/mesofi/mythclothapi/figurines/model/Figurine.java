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

@Entity
@EntityListeners(FigurineListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "figurines", indexes = @Index(name = "idx_figurine_unique_name", columnList = "legacyName"), comment = "Stores all collectible figurines managed by the application. This is the central repository for all figurine records.")
public class Figurine extends Auditable {

    @Column(unique = true, length = 200, updatable = false, comment = "Original figurine name imported from the initial CSV dataset (\"Myth Cloth Original Name\" column). Indexed to optimize search performance and enforce uniqueness across the catalog. Preserved only for traceability and migration purposes.")
    private String legacyName;

    @Column(nullable = false, length = 100, comment = "Base figurine name without variants or attributes. This is used to create the normalized name for internal processing and search optimization.")
    private String normalizedName;

    @Column(nullable = false, length = 200, comment = "Complete figurine name used throughout the catalog and displayed to users. Includes all applicable variants or attributes (e.g. God Cloth, OCE, Revival).")
    private String displayName;

    // FigurineDistributor.figurine
    @OneToMany(mappedBy = "figurine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FigurineDistributor> distributors = new ArrayList<>();

    @OneToMany(mappedBy = "figurine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollectorCollectionFigurine> collections = new ArrayList<>();

    @OneToMany(mappedBy = "figurine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FigurineStore> stores = new ArrayList<>();

    @Column(length = 50)
    private String tamashiiUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    private Distribution distribution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private LineUp lineup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Series series;

    @ManyToOne(fetch = FetchType.LAZY)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    private Anniversary anniversary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReleaseStatus currentReleaseStatus;

    @Column(name = "is_metal_body")
    private Boolean metalBody;

    @Column(name = "is_oce")
    private Boolean oce;

    @Column(name = "is_revival")
    private Boolean revival;

    @Column(name = "is_plain_cloth")
    private Boolean plainCloth;

    @Column(name = "is_broken")
    private Boolean broken;

    @Column(name = "is_golden")
    private Boolean golden;

    @Column(name = "is_gold")
    private Boolean gold;

    @Column(name = "is_manga")
    private Boolean manga;

    @Column(name = "is_set")
    private Boolean set;

    @Column(name = "is_articulable")
    private Boolean articulable;

    @Column(length = 1500)
    private String remarks;

    @OneToMany(mappedBy = "figurine", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("eventDate DESC")
    private List<FigurineEvent> events = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "official_images", joinColumns = @JoinColumn(name = "figurine_id"))
    private List<String> officialImages;

    @ElementCollection
    @CollectionTable(name = "non_official_images", joinColumns = @JoinColumn(name = "figurine_id"))
    private List<String> nonOfficialImages;
}
