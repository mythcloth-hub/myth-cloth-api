package com.mesofi.mythclothapi.collectors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import com.mesofi.mythclothapi.collectorproviders.model.CollectorAuthProvider;
import com.mesofi.mythclothapi.collectorscollections.CollectorCollection;
import com.mesofi.mythclothapi.common.BaseId;
import com.mesofi.mythclothapi.security.roles.model.Role;

import lombok.Getter;
import lombok.Setter;

/** Collector account entity used for authenticated users of the API. */
@Entity
@Getter
@Setter
@Table(name = "collectors")
public class Collector extends BaseId {

  @Column(length = 254, nullable = false)
  private String email;

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

  @Column(nullable = false)
  private Instant creationDate;

  @Column(nullable = false)
  private Instant updateDate;

  /** Initializes creation and update timestamps before first persistence. */
  @PrePersist
  public void prePersist() {
    creationDate = Instant.now();
    updateDate = Instant.now();
  }

  /** Refreshes the update timestamp before entity updates. */
  @PreUpdate
  public void preUpdate() {
    updateDate = Instant.now();
  }
}
