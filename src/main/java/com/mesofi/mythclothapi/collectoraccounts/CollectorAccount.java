package com.mesofi.mythclothapi.collectoraccounts;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import com.mesofi.mythclothapi.common.Auditable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a collector account in the system.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "collector_accounts")
public class CollectorAccount extends Auditable {

    @Column(nullable = false)
    private String name;

    @Column(length = 254, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;
}
