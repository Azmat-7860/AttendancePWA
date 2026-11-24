package com.attendance.worker;

import com.attendance.auth.repository.RefreshTokenRepository;
import com.attendance.auth.service.JwtService;
import com.attendance.org.Organization;
import com.attendance.org.OrganizationRepository;
import com.attendance.site.entity.Site;
import com.attendance.site.repository.SiteRepository;
import com.attendance.tenant.TenantContext;
import com.attendance.user.entity.Role;
import com.attendance.user.entity.User;
import com.attendance.user.repository.UserRepository;
import com.attendance.worker.dto.AssignSiteRequest;
import com.attendance.worker.dto.CreateWorkerRequest;
import com.attendance.worker.dto.UpdateWorkerRequest;
import com.attendance.worker.entity.PayType;
import com.attendance.worker.entity.Worker;
import com.attendance.worker.repository.WorkerRepository;
import com.attendance.worker.repository.WorkerSiteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class WorkerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private WorkerSiteRepository workerSiteRepository;

    @Autowired
    private JwtService jwtService;

    private Organization orgA;
    private Organization orgB;
    private User ownerA;
    private User supervisorA;
    private User ownerB;
    private String ownerTokenA;
    private String supervisorTokenA;
    private String ownerTokenB;

    @BeforeEach
    void setUp() {
        workerSiteRepository.deleteAll();
        workerRepository.deleteAll();
        siteRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        // 1. Setup Organization A
        orgA = new Organization();
        orgA.setName("Acme Builders");
        orgA = organizationRepository.save(orgA);

        TenantContext.setTenantId(orgA.getId());
        try {
            ownerA = new User("owner@acme.example.com", "Alice Owner", Role.OWNER);
            ownerA = userRepository.save(ownerA);

            supervisorA = new User("supervisor@acme.example.com", "Bob Supervisor", Role.SUPERVISOR);
            supervisorA = userRepository.save(supervisorA);
        } finally {
            TenantContext.clear();
        }

        ownerTokenA = jwtService.generateAccessToken(ownerA.getId(), orgA.getId(), "OWNER", ownerA.getEmail());
        supervisorTokenA = jwtService.generateAccessToken(supervisorA.getId(), orgA.getId(), "SUPERVISOR", supervisorA.getEmail());

        // 2. Setup Organization B
        orgB = new Organization();
        orgB.setName("Beta Construction");
        orgB = organizationRepository.save(orgB);

        TenantContext.setTenantId(orgB.getId());
        try {
            ownerB = new User("owner@beta.example.com", "Brian Beta", Role.OWNER);
            ownerB = userRepository.save(ownerB);
        } finally {
            TenantContext.clear();
        }

        ownerTokenB = jwtService.generateAccessToken(ownerB.getId(), orgB.getId(), "OWNER", ownerB.getEmail());
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Owner can create worker with valid parameters")
    void ownerCanCreateWorker() throws Exception {
        CreateWorkerRequest request = new CreateWorkerRequest(
                "Ramesh Kumar",
                "+919876543210",
                "Mason",
                PayType.DAILY,
                new BigDecimal("600.00")
        );

        mockMvc.perform(post("/api/v1/workers")
                        .header("Authorization", "Bearer " + ownerTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Ramesh Kumar"))
                .andExpect(jsonPath("$.skill").value("Mason"))
                .andExpect(jsonPath("$.payType").value("DAILY"))
                .andExpect(jsonPath("$.rate").value(600.00))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Supervisor is forbidden from creating worker (403 Forbidden)")
    void supervisorCannotCreateWorker() throws Exception {
        CreateWorkerRequest request = new CreateWorkerRequest(
                "Unauthorized Worker",
                null,
                "Helper",
                PayType.DAILY,
                new BigDecimal("400.00")
        );

        mockMvc.perform(post("/api/v1/workers")
                        .header("Authorization", "Bearer " + supervisorTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    @DisplayName("Owner can update worker rate and details")
    void ownerCanUpdateWorker() throws Exception {
        Worker worker;
        TenantContext.setTenantId(orgA.getId());
        try {
            worker = new Worker("Sunil", null, "Carpenter", PayType.DAILY, new BigDecimal("500.00"));
            worker.setOrganizationId(orgA.getId());
            worker = workerRepository.save(worker);
        } finally {
            TenantContext.clear();
        }

        UpdateWorkerRequest updateRequest = new UpdateWorkerRequest(
                "Sunil Kumar",
                "+919999988888",
                "Master Carpenter",
                PayType.DAILY,
                new BigDecimal("750.00"),
                true
        );

        mockMvc.perform(put("/api/v1/workers/" + worker.getId())
                        .header("Authorization", "Bearer " + ownerTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sunil Kumar"))
                .andExpect(jsonPath("$.skill").value("Master Carpenter"))
                .andExpect(jsonPath("$.rate").value(750.00));
    }

    @Test
    @DisplayName("Owner can deactivate worker (DELETE /api/v1/workers/{id})")
    void ownerCanDeactivateWorker() throws Exception {
        Worker worker;
        TenantContext.setTenantId(orgA.getId());
        try {
            worker = new Worker("Retiring Worker", null, "Helper", PayType.DAILY, new BigDecimal("450.00"));
            worker.setOrganizationId(orgA.getId());
            worker = workerRepository.save(worker);
        } finally {
            TenantContext.clear();
        }

        mockMvc.perform(delete("/api/v1/workers/" + worker.getId())
                        .header("Authorization", "Bearer " + ownerTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        TenantContext.setTenantId(orgA.getId());
        try {
            Worker deactivated = workerRepository.findById(worker.getId()).orElseThrow();
            assertFalse(deactivated.isActive());
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    @DisplayName("Owner can assign worker to site and query workers for that site")
    void ownerCanAssignWorkerToSiteAndQuery() throws Exception {
        Worker worker;
        Site site;
        TenantContext.setTenantId(orgA.getId());
        try {
            worker = new Worker("Active Crew Member", null, "Mason", PayType.DAILY, new BigDecimal("600.00"));
            worker.setOrganizationId(orgA.getId());
            worker = workerRepository.save(worker);

            site = new Site("Main Bridge", "River Cross");
            site.setOrganizationId(orgA.getId());
            site = siteRepository.save(site);
        } finally {
            TenantContext.clear();
        }

        AssignSiteRequest assignRequest = new AssignSiteRequest(
                site.getId(),
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(10)
        );

        // Assign
        mockMvc.perform(post("/api/v1/workers/" + worker.getId() + "/sites")
                        .header("Authorization", "Bearer " + ownerTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.workerId").value(worker.getId().toString()))
                .andExpect(jsonPath("$.siteId").value(site.getId().toString()));

        // Query workers by site (Supervisor can also view)
        mockMvc.perform(get("/api/v1/workers/by-site/" + site.getId())
                        .header("Authorization", "Bearer " + supervisorTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(worker.getId().toString()));
    }

    @Test
    @DisplayName("Cross-tenant isolation: Tenant B cannot access Tenant A workers")
    void crossTenantWorkerIsolation() throws Exception {
        Worker workerA;
        TenantContext.setTenantId(orgA.getId());
        try {
            workerA = new Worker("Org A Worker", null, "Electrician", PayType.HOURLY, new BigDecimal("120.00"));
            workerA.setOrganizationId(orgA.getId());
            workerA = workerRepository.save(workerA);
        } finally {
            TenantContext.clear();
        }

        // Tenant B owner attempts to access worker A
        mockMvc.perform(get("/api/v1/workers/" + workerA.getId())
                        .header("Authorization", "Bearer " + ownerTokenB))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Worker not found"));
    }
}
