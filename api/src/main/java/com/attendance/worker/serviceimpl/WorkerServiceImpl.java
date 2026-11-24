package com.attendance.worker.serviceimpl;

import com.attendance.site.repository.SiteRepository;
import com.attendance.tenant.TenantContext;
import com.attendance.worker.dto.*;
import com.attendance.worker.entity.Worker;
import com.attendance.worker.entity.WorkerSite;
import com.attendance.worker.repository.WorkerRepository;
import com.attendance.worker.repository.WorkerSiteRepository;
import com.attendance.worker.service.WorkerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class WorkerServiceImpl implements WorkerService {

    private static final Logger log = LoggerFactory.getLogger(WorkerServiceImpl.class);

    private final WorkerRepository workerRepository;
    private final WorkerSiteRepository workerSiteRepository;
    private final SiteRepository siteRepository;

    public WorkerServiceImpl(
            WorkerRepository workerRepository,
            WorkerSiteRepository workerSiteRepository,
            SiteRepository siteRepository
    ) {
        this.workerRepository = workerRepository;
        this.workerSiteRepository = workerSiteRepository;
        this.siteRepository = siteRepository;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public WorkerResponse createWorker(CreateWorkerRequest request) {
        UUID organizationId = TenantContext.requireTenantId();

        String trimmedName = request.name().trim();
        String trimmedPhone = (request.phone() != null && !request.phone().isBlank()) ? request.phone().trim() : null;
        String trimmedSkill = (request.skill() != null && !request.skill().isBlank()) ? request.skill().trim() : null;

        Worker worker = new Worker(trimmedName, trimmedPhone, trimmedSkill, request.payType(), request.rate());
        worker.setOrganizationId(organizationId);
        worker = workerRepository.save(worker);

        log.info("Created worker id: {} under organization id: {}", worker.getId(), organizationId);
        return WorkerResponse.fromEntity(worker);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public WorkerResponse updateWorker(UUID id, UpdateWorkerRequest request) {
        UUID organizationId = TenantContext.requireTenantId();

        Worker worker = workerRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Worker not found"));

        worker.setName(request.name().trim());
        worker.setPhone((request.phone() != null && !request.phone().isBlank()) ? request.phone().trim() : null);
        worker.setSkill((request.skill() != null && !request.skill().isBlank()) ? request.skill().trim() : null);
        worker.setPayType(request.payType());
        worker.setRate(request.rate());

        if (request.active() != null) {
            worker.setActive(request.active());
        }

        worker = workerRepository.save(worker);
        log.info("Updated worker id: {} under organization id: {}", worker.getId(), organizationId);
        return WorkerResponse.fromEntity(worker);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public WorkerResponse deactivateWorker(UUID id) {
        UUID organizationId = TenantContext.requireTenantId();

        Worker worker = workerRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Worker not found"));

        worker.setActive(false);
        worker = workerRepository.save(worker);

        log.info("Deactivated worker id: {} under organization id: {}", worker.getId(), organizationId);
        return WorkerResponse.fromEntity(worker);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER', 'SUPERVISOR')")
    public WorkerResponse getWorker(UUID id) {
        UUID organizationId = TenantContext.requireTenantId();

        Worker worker = workerRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Worker not found"));

        return WorkerResponse.fromEntity(worker);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER', 'SUPERVISOR')")
    public List<WorkerResponse> listWorkers(boolean activeOnly) {
        UUID organizationId = TenantContext.requireTenantId();

        List<Worker> workers = activeOnly
                ? workerRepository.findByOrganizationIdAndActiveTrue(organizationId)
                : workerRepository.findByOrganizationId(organizationId);

        return workers.stream()
                .map(WorkerResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public WorkerSiteResponse assignWorkerToSite(UUID workerId, AssignSiteRequest request) {
        UUID organizationId = TenantContext.requireTenantId();

        // Validate worker exists in tenant
        Worker worker = workerRepository.findByIdAndOrganizationId(workerId, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Worker not found"));

        // Validate site exists in tenant
        siteRepository.findByIdAndOrganizationId(request.siteId(), organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Site not found"));

        if (request.toDate() != null && request.toDate().isBefore(request.fromDate())) {
            throw new IllegalArgumentException("To date cannot be before from date");
        }

        WorkerSite ws = new WorkerSite(worker.getId(), request.siteId(), request.fromDate(), request.toDate());
        ws.setOrganizationId(organizationId);
        ws = workerSiteRepository.save(ws);

        log.info("Assigned worker id: {} to site id: {} under organization id: {}", workerId, request.siteId(), organizationId);
        return WorkerSiteResponse.fromEntity(ws);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER', 'SUPERVISOR')")
    public List<WorkerSiteResponse> listWorkerSiteAssignments(UUID workerId) {
        UUID organizationId = TenantContext.requireTenantId();

        // Verify worker exists in tenant
        workerRepository.findByIdAndOrganizationId(workerId, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Worker not found"));

        List<WorkerSite> assignments = workerSiteRepository.findByOrganizationIdAndWorkerId(organizationId, workerId);
        return assignments.stream()
                .map(WorkerSiteResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER', 'SUPERVISOR')")
    public List<WorkerResponse> listWorkersForSite(UUID siteId, LocalDate onDate) {
        UUID organizationId = TenantContext.requireTenantId();

        // Verify site exists in tenant
        siteRepository.findByIdAndOrganizationId(siteId, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Site not found"));

        LocalDate targetDate = (onDate != null) ? onDate : LocalDate.now();

        List<WorkerSite> activeAssignments = workerSiteRepository.findActiveAssignmentsForSiteOnDate(
                organizationId,
                siteId,
                targetDate
        );

        List<UUID> workerIds = activeAssignments.stream()
                .map(WorkerSite::getWorkerId)
                .distinct()
                .toList();

        return workerRepository.findAllById(workerIds).stream()
                .filter(w -> w.getOrganizationId().equals(organizationId) && w.isActive())
                .map(WorkerResponse::fromEntity)
                .toList();
    }
}
