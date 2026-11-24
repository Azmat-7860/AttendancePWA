package com.attendance.worker.entity;

import com.attendance.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

/**
 * WorkerSite entity tracking worker-to-site assignments with date ranges.
 */
@Entity
@Table(name = "worker_sites")
public class WorkerSite extends BaseTenantEntity {

    @Column(name = "worker_id", nullable = false)
    private UUID workerId;

    @Column(name = "site_id", nullable = false)
    private UUID siteId;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    public WorkerSite() {
    }

    public WorkerSite(UUID workerId, UUID siteId, LocalDate fromDate, LocalDate toDate) {
        this.workerId = workerId;
        this.siteId = siteId;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public UUID getWorkerId() {
        return workerId;
    }

    public void setWorkerId(UUID workerId) {
        this.workerId = workerId;
    }

    public UUID getSiteId() {
        return siteId;
    }

    public void setSiteId(UUID siteId) {
        this.siteId = siteId;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }
}
