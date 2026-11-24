package com.attendance.worker.repository;

import com.attendance.worker.entity.WorkerSite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkerSiteRepository extends JpaRepository<WorkerSite, UUID> {

    List<WorkerSite> findByOrganizationIdAndWorkerId(UUID organizationId, UUID workerId);

    List<WorkerSite> findByOrganizationIdAndSiteId(UUID organizationId, UUID siteId);

    @Query("""
        SELECT ws FROM WorkerSite ws
        WHERE ws.organizationId = :orgId
          AND ws.siteId = :siteId
          AND ws.fromDate <= :targetDate
          AND (ws.toDate IS NULL OR ws.toDate >= :targetDate)
    """)
    List<WorkerSite> findActiveAssignmentsForSiteOnDate(
            @Param("orgId") UUID orgId,
            @Param("siteId") UUID siteId,
            @Param("targetDate") LocalDate targetDate
    );

    Optional<WorkerSite> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
