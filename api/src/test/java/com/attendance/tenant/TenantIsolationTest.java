package com.attendance.tenant;

import com.attendance.org.Organization;
import com.attendance.org.OrganizationRepository;
import com.attendance.site.Site;
import com.attendance.site.SiteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class TenantIsolationTest {

    private static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable t) {
            return false;
        }
    }

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @BeforeAll
    static void checkDocker() {
        Assumptions.assumeTrue(isDockerAvailable(), "Docker is not available in the current environment; skipping Testcontainers test.");
    }

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private SiteRepository siteRepository;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @Transactional
    @DisplayName("Organization B must not be able to read or modify Organization A's rows")
    void testCrossTenantIsolation() {
        // 1. Create two distinct tenant organizations
        Organization orgA = new Organization();
        orgA.setName("Contractor Alpha");
        orgA = organizationRepository.save(orgA);
        UUID orgAId = orgA.getId();

        Organization orgB = new Organization();
        orgB.setName("Contractor Beta");
        orgB = organizationRepository.save(orgB);
        UUID orgBId = orgB.getId();

        assertNotEquals(orgAId, orgBId, "Organizations must have unique IDs");

        // 2. As Organization A, create a site
        TenantContext.setTenantId(orgAId);
        Site siteA = new Site("Alpha Downtown Project", "123 Main St");
        Site savedSiteA = siteRepository.save(siteA);

        assertNotNull(savedSiteA.getId(), "Site ID must be generated");
        assertEquals(orgAId, savedSiteA.getOrganizationId(), "Site must be assigned to Org A");

        // Org A can find its own site
        List<Site> orgASites = siteRepository.findAll();
        assertTrue(orgASites.stream().anyMatch(s -> s.getId().equals(savedSiteA.getId())),
                "Org A must be able to view its own site");

        // 3. Switch context to Organization B
        TenantContext.setTenantId(orgBId);

        // Org B queries all sites: Hibernate tenant filter must exclude Org A's sites
        List<Site> orgBSites = siteRepository.findAll();
        assertFalse(orgBSites.stream().anyMatch(s -> s.getId().equals(savedSiteA.getId())),
                "Org B must NEVER see Org A's sites in findAll()");

        // Org B explicitly tries to find Org A's site by ID
        Optional<Site> crossTenantSite = siteRepository.findByIdAndOrganizationId(savedSiteA.getId(), orgBId);
        assertTrue(crossTenantSite.isEmpty(), "Org B query by Org B ID must return empty for Org A site");

        // 4. Org B creates its own site
        Site siteB = new Site("Beta Harbor Bridge", "456 Ocean Ave");
        Site savedSiteB = siteRepository.save(siteB);

        assertEquals(orgBId, savedSiteB.getOrganizationId(), "Site B must be assigned to Org B");

        // Verify Org B sees only Site B
        List<Site> orgBOnlySites = siteRepository.findAll();
        assertEquals(1, orgBOnlySites.size(), "Org B should see only its own site");
        assertEquals(savedSiteB.getId(), orgBOnlySites.get(0).getId());
    }

    @Test
    @DisplayName("Attempting to persist a tenant entity without TenantContext must fail")
    void shouldRejectPersistingWithoutTenantContext() {
        TenantContext.clear();

        Site site = new Site("Rogue Site", "Unknown Location");
        assertThrows(IllegalStateException.class, () -> siteRepository.save(site),
                "Saving a tenant entity without TenantContext must throw IllegalStateException");
    }

    @Test
    @DisplayName("Explicitly setting a conflicting organizationId must be rejected with SecurityException")
    void shouldRejectConflictingOrganizationId() {
        UUID authenticatedTenant = UUID.randomUUID();
        UUID forgedTenant = UUID.randomUUID();

        TenantContext.setTenantId(authenticatedTenant);

        Site site = new Site("Tampered Site", "Fake Location");
        site.setOrganizationId(forgedTenant); // Attempting to forge organization_id

        assertThrows(SecurityException.class, () -> siteRepository.save(site),
                "Attempting to forge organization_id must throw SecurityException");
    }
}
