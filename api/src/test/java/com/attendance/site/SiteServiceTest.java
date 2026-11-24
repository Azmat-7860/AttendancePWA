package com.attendance.site;

import com.attendance.site.dto.CreateSiteRequest;
import com.attendance.site.dto.SiteResponse;
import com.attendance.site.dto.UpdateSiteRequest;
import com.attendance.site.entity.Site;
import com.attendance.site.repository.SiteRepository;
import com.attendance.site.service.SiteService;
import com.attendance.site.serviceimpl.SiteServiceImpl;
import com.attendance.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SiteServiceTest {

    @Mock
    private SiteRepository siteRepository;

    private SiteService siteService;

    private final UUID orgId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        siteService = new SiteServiceImpl(siteRepository);
        TenantContext.setTenantId(orgId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Successfully creates a site when name is unique")
    void createSiteSuccess() {
        CreateSiteRequest request = new CreateSiteRequest("Metro Station", "Block A");

        when(siteRepository.existsByOrganizationIdAndNameIgnoreCase(orgId, "Metro Station"))
                .thenReturn(false);

        Site savedSite = new Site("Metro Station", "Block A");
        savedSite.setOrganizationId(orgId);
        when(siteRepository.save(any(Site.class))).thenReturn(savedSite);

        SiteResponse response = siteService.createSite(request);

        assertThat(response.name()).isEqualTo("Metro Station");
        assertThat(response.address()).isEqualTo("Block A");
        assertThat(response.active()).isTrue();
        assertThat(response.organizationId()).isEqualTo(orgId);
        verify(siteRepository).save(any(Site.class));
    }

    @Test
    @DisplayName("Rejects creation when site name is duplicate in the same organization")
    void createSiteDuplicateThrows() {
        CreateSiteRequest request = new CreateSiteRequest("Metro Station", "Block A");

        when(siteRepository.existsByOrganizationIdAndNameIgnoreCase(orgId, "Metro Station"))
                .thenReturn(true);

        assertThatThrownBy(() -> siteService.createSite(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(siteRepository, never()).save(any(Site.class));
    }

    @Test
    @DisplayName("Updates site name and address")
    void updateSiteSuccess() {
        UUID siteId = UUID.randomUUID();
        Site existing = new Site("Old Name", "Old Address");
        existing.setOrganizationId(orgId);

        when(siteRepository.findByIdAndOrganizationId(siteId, orgId))
                .thenReturn(Optional.of(existing));
        when(siteRepository.existsByOrganizationIdAndNameIgnoreCaseAndIdNot(orgId, "New Name", siteId))
                .thenReturn(false);
        when(siteRepository.save(any(Site.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateSiteRequest request = new UpdateSiteRequest("New Name", "New Address", true);
        SiteResponse response = siteService.updateSite(siteId, request);

        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.address()).isEqualTo("New Address");
    }

    @Test
    @DisplayName("Deactivates site by setting active to false")
    void deactivateSiteSuccess() {
        UUID siteId = UUID.randomUUID();
        Site existing = new Site("Site To Close", "Main Road");
        existing.setOrganizationId(orgId);
        existing.setActive(true);

        when(siteRepository.findByIdAndOrganizationId(siteId, orgId))
                .thenReturn(Optional.of(existing));
        when(siteRepository.save(any(Site.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SiteResponse response = siteService.deactivateSite(siteId);

        assertThat(response.active()).isFalse();
        verify(siteRepository).save(existing);
    }

    @Test
    @DisplayName("Lists active sites when activeOnly is true")
    void listActiveSitesOnly() {
        Site s1 = new Site("Site 1", "Addr 1");
        s1.setOrganizationId(orgId);

        when(siteRepository.findByOrganizationIdAndActiveTrue(orgId))
                .thenReturn(List.of(s1));

        List<SiteResponse> responses = siteService.listSites(true);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).name()).isEqualTo("Site 1");
        verify(siteRepository).findByOrganizationIdAndActiveTrue(orgId);
        verify(siteRepository, never()).findByOrganizationId(orgId);
    }
}
