package com.mesofi.mythclothapi.collectors;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.mesofi.mythclothapi.collectorscollections.CollectorCollection;
import com.mesofi.mythclothapi.common.Auditable;
import com.mesofi.mythclothapi.security.roles.model.Role;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a collector in the application. A collector has an email, password
 * hash, display name, profile picture URL, and can have multiple collections of
 * figurines. Each collector is associated with a specific role for access
 * control.
 */
@Entity
@Getter
@Setter
@Table(name = "collectors")
public class Collector extends Auditable {

    /**
     * The email address of the collector. This field is mandatory, must be unique,
     * and has a maximum length of 254 characters.
     */
    @Column(length = 254, nullable = false, unique = true)
    private String email;

    /**
     * The hashed password of the collector. This field is not mandatory.
     */
    @Column
    private String passwordHash;

    /**
     * The display name of the collector. This field is optional and has a maximum
     * length of 200 characters.
     */
    @Column(length = 200)
    private String displayName;

    /**
     * The URL of the profile picture of the collector. This field is optional and
     * has a maximum length of 200 characters.
     */
    @Column(length = 200)
    private String profilePictureUrl;

    /**
     * The list of collections owned by the collector. This is a one-to-many
     * relationship, and the collections are managed by the collector. Cascade
     * operations are applied to ensure that changes to the collector are reflected
     * in its collections, and orphan removal is enabled to delete collections that
     * are no longer associated with the collector.
     */
    @OneToMany(mappedBy = "collector", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollectorCollection> collections = new ArrayList<>();

    /**
     * The role associated with the collector for access control. This is a
     * mandatory relationship, and the role is fetched lazily to optimize
     * performance.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Role role;
}
