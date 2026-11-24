package com.attendance.site;

import com.attendance.auth.repository.RefreshTokenRepository;
import com.attendance.auth.service.JwtService;
import com.attendance.org.Organization;
import com.attendance.org.OrganizationRepository;
import com.attendance.site.dto.CreateSiteRequest;
import com.attendance.site.dto.UpdateSiteRequest;
import com.attendance.site.entity.Site;
import com.attendance.site.repository.SiteRepository;
import com.attendance.tenant.TenantContext;
import com.attendance.user.entity.Role;
import com.attendance.user.entity.User;
import com.attendance.user.repository.UserRepository;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class SiteIntegrationTest {

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
    @DisplayName("Owner can create a new site")
    void ownerCanCreateSite() throws Exception {
        CreateSiteRequest request = new CreateSiteRequest("Downtown Metro Station", "Sector 4, Main Road");

        mockMvc.perform(post("/api/v1/sites")
                        .header("Authorization", "Bearer " + ownerTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Downtown Metro Station"))
                .andExpect(jsonPath("$.address").value("Sector 4, Main Road"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.organizationId").value(orgA.getId().toString()));

        TenantContext.setTenantId(orgA.getId());
        try {
            assertTrue(siteRepository.existsByOrganizationIdAndNameIgnoreCase(orgA.getId(), "Downtown Metro Station"));
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    @DisplayName("Duplicate site name in same organization is rejected with 400 Bad Request")
    void duplicateSiteNameRejected() throws Exception {
        TenantContext.setTenantId(orgA.getId());
        try {
            Site site = new Site("Riverside Complex", "Phase 1");
            site.setOrganizationId(orgA.getId());
            siteRepository.save(site);
        } finally {
            TenantContext.clear();
        }

        CreateSiteRequest duplicateRequest = new CreateSiteRequest("Riverside Complex", "Different address");

        mockMvc.perform(post("/api/v1/sites")
                        .header("Authorization", "Bearer " + ownerTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.detail").value("Site with name 'Riverside Complex' already exists"));
    }

    @Test
    @DisplayName("Creating a site with blank name fails validation with 400 Bad Request")
    void blankSiteNameFailsValidation() throws Exception {
        CreateSiteRequest request = new CreateSiteRequest("   ", "Some address");

        mockMvc.perform(post("/api/v1/sites")
                        .header("Authorization", "Bearer " + ownerTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    @DisplayName("Supervisor is forbidden from creating a site (403 Forbidden)")
    void supervisorCannotCreateSite() throws Exception {
        CreateSiteRequest request = new CreateSiteRequest("Highway Extension", "KM 45");

        mockMvc.perform(post("/api/v1/sites")
                        .header("Authorization", "Bearer " + supervisorTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    @DisplayName("Both Owner and Supervisor can list sites for their organization")
    void supervisorAndOwnerCanListSites() throws Exception {
        TenantContext.setTenantId(orgA.getId());
        try {
            Site s1 = new Site("Site Alpha", "Address Alpha");
            s1.setOrganizationId(orgA.getId());
            siteRepository.save(s1);

            Site s2 = new Site("Site Beta", "Address Beta");
            s2.setOrganizationId(orgA.getId());
            siteRepository.save(s2);
        } finally {
            TenantContext.clear();
        }

        // Supervisor listing
        mockMvc.perform(get("/api/v1/sites")
                        .header("Authorization", "Bearer " + supervisorTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").isNotEmpty());

        // Owner listing
        mockMvc.perform(get("/api/v1/sites")
                        .header("Authorization", "Bearer " + ownerTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Owner can update site details")
    void ownerCanUpdateSite() throws Exception {
        Site site;
        TenantContext.setTenantId(orgA.getId());
        try {
            site = new Site("Airport Terminal", "Gate 1");
            site.setOrganizationId(orgA.getId());
            site = siteRepository.save(site);
        } finally {
            TenantContext.clear();
        }

        UpdateSiteRequest updateRequest = new UpdateSiteRequest("Airport Terminal 2", "Gate 5", true);

        mockMvc.perform(put("/api/v1/sites/" + site.getId())
                        .header("Authorization", "Bearer " + ownerTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Airport Terminal 2"))
                .andExpect(jsonPath("$.address").value("Gate 5"));

        TenantContext.setTenantId(orgA.getId());
        try {
            Site updated = siteRepository.findById(site.getId()).orElseThrow();
            assertThat(updated.getName()).isEqualTo("Airport Terminal 2");
            assertThat(updated.getAddress()).isEqualTo("Gate 5");
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    @DisplayName("Supervisor is forbidden from updating a site (403 Forbidden)")
    void supervisorCannotUpdateSite() throws Exception {
        Site site;
        TenantContext.setTenantId(orgA.getId());
        try {
            site = new Site("City Mall", "Main Atrium");
            site.setOrganizationId(orgA.getId());
            site = siteRepository.save(site);
        } finally {
            TenantContext.clear();
        }

        UpdateSiteRequest updateRequest = new UpdateSiteRequest("City Mall Renovation", "Floor 2", true);

        mockMvc.perform(put("/api/v1/sites/" + site.getId())
                        .header("Authorization", "Bearer " + supervisorTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Owner can deactivate a site (DELETE /api/v1/sites/{id})")
    void ownerCanDeactivateSite() throws Exception {
        Site site;
        TenantContext.setTenantId(orgA.getId());
        try {
            site = new Site("Completed Project", "Old Town");
            site.setOrganizationId(orgA.getId());
            site = siteRepository.save(site);
        } finally {
            TenantContext.clear();
        }

        mockMvc.perform(delete("/api/v1/sites/" + site.getId())
                        .header("Authorization", "Bearer " + ownerTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        TenantContext.setTenantId(orgA.getId());
        try {
            Site deactivated = siteRepository.findById(site.getId()).orElseThrow();
            assertFalse(deactivated.isActive());
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    @DisplayName("Cross-tenant isolation: Tenant B cannot access or update Tenant A site")
    void crossTenantSiteIsolation() throws Exception {
        Site siteA;
        TenantContext.setTenantId(orgA.getId());
        try {
            siteA = new Site("Acme Secret Site", "Secret Location");
            siteA.setOrganizationId(orgA.getId());
            siteA = siteRepository.save(siteA);
        } finally {
            TenantContext.clear();
        }

        // Tenant B owner attempts to view Tenant A's site
        mockMvc.perform(get("/api/v1/sites/" + siteA.getId())
                        .header("Authorization", "Bearer " + ownerTokenB))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Site not found"));

        // Tenant B owner attempts to update Tenant A's site
        UpdateSiteRequest maliciousUpdate = new UpdateSiteRequest("Hijacked Site", "Stolen", true);
        mockMvc.perform(put("/api/v1/sites/" + siteA.getId())
                        .header("Authorization", "Bearer " + ownerTokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(maliciousUpdate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Site not found"));

        // Tenant B owner listing should NOT include Tenant A's site
        mockMvc.perform(get("/api/v1/sites")
                        .header("Authorization", "Bearer " + ownerTokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
