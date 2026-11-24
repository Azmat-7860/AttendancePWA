package com.attendance.worker;

import com.attendance.site.entity.Site;
import com.attendance.site.repository.SiteRepository;
import com.attendance.tenant.TenantContext;
import com.attendance.worker.dto.*;
import com.attendance.worker.entity.PayType;
import com.attendance.worker.entity.Worker;
import com.attendance.worker.entity.WorkerSite;
import com.attendance.worker.repository.WorkerRepository;
import com.attendance.worker.repository.WorkerSiteRepository;
import com.attendance.worker.service.WorkerService;
import com.attendance.worker.serviceimpl.WorkerServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkerServiceTest {

    @Mock
    private WorkerRepository workerRepository;

    @Mock
    private WorkerSiteRepository workerSiteRepository;

    @Mock
    private SiteRepository siteRepository;

    private WorkerService workerService;

    private final UUID orgId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        workerService = new WorkerServiceImpl(workerRepository, workerSiteRepository, siteRepository);
        TenantContext.setTenantId(orgId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Successfully creates worker with valid daily rate")
    void createWorkerSuccess() {
        CreateWorkerRequest request = new CreateWorkerRequest(
                "Ramesh Kumar",
                "+919876543210",
                "Mason",
                PayType.DAILY,
                new BigDecimal("650.00")
        );

        Worker savedWorker = new Worker("Ramesh Kumar", "+919876543210", "Mason", PayType.DAILY, new BigDecimal("650.00"));
        savedWorker.setOrganizationId(orgId);
        when(workerRepository.save(any(Worker.class))).thenReturn(savedWorker);

        WorkerResponse response = workerService.createWorker(request);

        assertThat(response.name()).isEqualTo("Ramesh Kumar");
        assertThat(response.payType()).isEqualTo(PayType.DAILY);
        assertThat(response.rate()).isEqualByComparingTo("650.00");
        assertThat(response.active()).isTrue();
        verify(workerRepository).save(any(Worker.class));
    }

    @Test
    @DisplayName("Updates worker rate, skill, and pay type")
    void updateWorkerSuccess() {
        UUID workerId = UUID.randomUUID();
        Worker existing = new Worker("Suresh", "99999", "Helper", PayType.DAILY, new BigDecimal("400.00"));
        existing.setOrganizationId(orgId);

        when(workerRepository.findByIdAndOrganizationId(workerId, orgId)).thenReturn(Optional.of(existing));
        when(workerRepository.save(any(Worker.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateWorkerRequest request = new UpdateWorkerRequest(
                "Suresh Sharma",
                "99999",
                "Senior Mason",
                PayType.DAILY,
                new BigDecimal("700.00"),
                true
        );

        WorkerResponse response = workerService.updateWorker(workerId, request);

        assertThat(response.name()).isEqualTo("Suresh Sharma");
        assertThat(response.skill()).isEqualTo("Senior Mason");
        assertThat(response.rate()).isEqualByComparingTo("700.00");
    }

    @Test
    @DisplayName("Deactivates worker (active = false)")
    void deactivateWorkerSuccess() {
        UUID workerId = UUID.randomUUID();
        Worker existing = new Worker("Inactive Worker", null, "Helper", PayType.DAILY, new BigDecimal("450.00"));
        existing.setOrganizationId(orgId);

        when(workerRepository.findByIdAndOrganizationId(workerId, orgId)).thenReturn(Optional.of(existing));
        when(workerRepository.save(any(Worker.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkerResponse response = workerService.deactivateWorker(workerId);

        assertThat(response.active()).isFalse();
    }

    @Test
    @DisplayName("Assigns worker to site with valid date range")
    void assignWorkerToSiteSuccess() {
        UUID workerId = UUID.randomUUID();
        UUID siteId = UUID.randomUUID();

        Worker worker = new Worker("Rajesh", null, "Carpenter", PayType.DAILY, new BigDecimal("600.00"));
        worker.setOrganizationId(orgId);
        Site site = new Site("Metro Station", "Block B");
        site.setOrganizationId(orgId);

        when(workerRepository.findByIdAndOrganizationId(workerId, orgId)).thenReturn(Optional.of(worker));
        when(siteRepository.findByIdAndOrganizationId(siteId, orgId)).thenReturn(Optional.of(site));

        WorkerSite savedWs = new WorkerSite(workerId, siteId, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
        savedWs.setOrganizationId(orgId);
        when(workerSiteRepository.save(any(WorkerSite.class))).thenReturn(savedWs);

        AssignSiteRequest request = new AssignSiteRequest(siteId, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
        WorkerSiteResponse response = workerService.assignWorkerToSite(workerId, request);

        assertThat(response.workerId()).isEqualTo(workerId);
        assertThat(response.siteId()).isEqualTo(siteId);
        assertThat(response.fromDate()).isEqualTo(LocalDate.of(2026, 11, 1));
        verify(workerSiteRepository).save(any(WorkerSite.class));
    }

    @Test
    @DisplayName("Rejects site assignment if toDate is before fromDate")
    void assignWorkerToSiteInvalidDatesThrows() {
        UUID workerId = UUID.randomUUID();
        UUID siteId = UUID.randomUUID();

        Worker worker = new Worker("Rajesh", null, "Carpenter", PayType.DAILY, new BigDecimal("600.00"));
        worker.setOrganizationId(orgId);
        Site site = new Site("Metro Station", "Block B");
        site.setOrganizationId(orgId);

        when(workerRepository.findByIdAndOrganizationId(workerId, orgId)).thenReturn(Optional.of(worker));
        when(siteRepository.findByIdAndOrganizationId(siteId, orgId)).thenReturn(Optional.of(site));

        AssignSiteRequest request = new AssignSiteRequest(siteId, LocalDate.of(2026, 11, 30), LocalDate.of(2026, 11, 1));

        assertThatThrownBy(() -> workerService.assignWorkerToSite(workerId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("To date cannot be before from date");
    }
}
