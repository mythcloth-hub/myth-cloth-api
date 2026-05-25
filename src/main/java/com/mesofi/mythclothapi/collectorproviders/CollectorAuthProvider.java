package com.mesofi.mythclothapi.collectorproviders;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.common.BaseId;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "collector_providers",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_provider_collector",
          columnNames = {"provider", "provider_user_id"})
    })
public class CollectorAuthProvider extends BaseId {

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private ProviderType provider;

  @Column(nullable = false)
  private String providerUserId;

  @Column(length = 254)
  private String email;

  @Column private Boolean emailVerified;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "collector_id", nullable = false)
  private Collector collector;

  @Column(nullable = false)
  private Instant creationDate;

  @PrePersist
  public void prePersist() {
    creationDate = Instant.now();
  }
}
