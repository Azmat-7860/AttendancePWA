package com.attendance.common.entity;

import com.attendance.tenant.TenantContext;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Base mapped superclass for all multi-tenant entities.
 * Enforces organization_id presence, UTC timestamps, and Hibernate tenant filtering.
 */
@MappedSuperclass
@FilterDef(
        name = "tenantFilter",
        parameters = @ParamDef(name = "tenantId", type = UUID.class)
)
@Filter(name = "tenantFilter", condition = "organization_id = :tenantId")
public abstract class BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "organization_id", nullable = false, updatable = false)
    private UUID organizationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "server_version", insertable = false, updatable = false)
    private Long serverVersion;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }

        UUID contextTenantId = TenantContext.requireTenantId();
        if (this.organizationId == null) {
            this.organizationId = contextTenantId;
        } else if (!this.organizationId.equals(contextTenantId)) {
            throw new SecurityException("Cross-tenant entity creation rejected: explicit organizationId does not match authenticated TenantContext");
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();

        UUID contextTenantId = TenantContext.requireTenantId();
        if (!Objects.equals(this.organizationId, contextTenantId)) {
            throw new SecurityException("Cross-tenant modification rejected: entity organizationId does not match authenticated TenantContext");
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Long getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(Long serverVersion) {
        this.serverVersion = serverVersion;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
