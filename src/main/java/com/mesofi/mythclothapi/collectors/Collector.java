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

import com.mesofi.mythclothapi.collectorproviders.model.CollectorAuthProvider;
import com.mesofi.mythclothapi.collectorscollections.CollectorCollection;
import com.mesofi.mythclothapi.common.Auditable;
import com.mesofi.mythclothapi.security.roles.model.Role;

import lombok.Getter;
import lombok.Setter;

/** Collector account entity used for authenticated users of the API. */
@Entity
@Getter
@Setter
@Table(name = "collectors")
public class Collector extends Auditable {

    @Column(length = 254, nullable = false, unique = true)
    private String email;

    @Column
    private String passwordHash;

    @Column(length = 200)
    private String displayName;

    @Column(length = 200)
    private String profilePictureUrl;

    @OneToMany(mappedBy = "collector", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollectorAuthProvider> authProviders = new ArrayList<>();

    @OneToMany(mappedBy = "collector", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollectorCollection> collections = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Role role;
}
