package com.mesofi.mythclothapi.collectors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import com.mesofi.mythclothapi.collectorproviders.CollectorAuthProvider;
import com.mesofi.mythclothapi.collectorscollections.CollectorCollection;
import com.mesofi.mythclothapi.common.BaseId;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "collectors")
public class Collector extends BaseId {

  @Column(unique = true, length = 254, nullable = false)
  private String email;

  @Column(length = 200)
  private String displayName;

  @Column(length = 200)
  private String profilePictureUrl;

  @OneToMany(mappedBy = "collector", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CollectorAuthProvider> authProviders = new ArrayList<>();

  @OneToMany(mappedBy = "collector", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CollectorCollection> collections = new ArrayList<>();

  @Column(nullable = false)
  private Instant creationDate;

  @Column(nullable = false)
  private Instant updateDate;

  @PrePersist
  public void prePersist() {
    creationDate = Instant.now();
    updateDate = Instant.now();
  }

  @PreUpdate
  public void preUpdate() {
    updateDate = Instant.now();
  }
}
