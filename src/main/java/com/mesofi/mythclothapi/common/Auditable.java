package com.mesofi.mythclothapi.common;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.Setter;

/**
 * An abstract base class for entities that require creation and last-modified
 * timestamps in addition to a unique identifier.
 *
 * <p>
 * This class extends {@link BaseId} to inherit the primary key functionality
 * and uses Spring Data JPA auditing to automatically maintain the creation and
 * last-modification timestamps.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable extends BaseId {

    /**
     * The date and time when the entity was created.
     *
     * <p>
     * This value is automatically populated by Spring Data JPA auditing when the
     * entity is first persisted and cannot be modified afterward.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant creationDate;

    /**
     * The date and time when the entity was last modified.
     *
     * <p>
     * This value is automatically updated by Spring Data JPA auditing whenever the
     * entity is modified.
     */
    @LastModifiedDate
    @Column(nullable = false)
    private Instant updateDate;
}
