package com.attendance.worker.dto;

import com.attendance.worker.entity.WorkerSite;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record WorkerSiteResponse(
        UUID id,
        UUID workerId,
        UUID siteId,
        LocalDate fromDate,
        LocalDate toDate,
        UUID organizationId,
        Instant createdAt
) {
    public static WorkerSiteResponse fromEntity(WorkerSite ws) {
        return new WorkerSiteResponse(
                ws.getId(),
                ws.getWorkerId(),
                ws.getSiteId(),
                ws.getFromDate(),
                ws.getToDate(),
                ws.getOrganizationId(),
                ws.getCreatedAt()
        );
    }
}
