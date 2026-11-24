package com.attendance.site.serviceimpl;

import com.attendance.site.dto.CreateSiteRequest;
import com.attendance.site.dto.SiteResponse;
import com.attendance.site.dto.UpdateSiteRequest;
import com.attendance.site.entity.Site;
import com.attendance.site.repository.SiteRepository;
import com.attendance.site.service.SiteService;
import com.attendance.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SiteServiceImpl implements SiteService {

    private static final Logger log = LoggerFactory.getLogger(SiteServiceImpl.class);

    private final SiteRepository siteRepository;

    public SiteServiceImpl(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public SiteResponse createSite(CreateSiteRequest request) {
        UUID organizationId = TenantContext.requireTenantId();

        String trimmedName = request.name().trim();
        if (siteRepository.existsByOrganizationIdAndNameIgnoreCase(organizationId, trimmedName)) {
            throw new IllegalArgumentException("Site with name '" + trimmedName + "' already exists");
        }

        String trimmedAddress = (request.address() != null && !request.address().isBlank())
                ? request.address().trim()
                : null;

        Site site = new Site(trimmedName, trimmedAddress);
        site.setOrganizationId(organizationId);
        site = siteRepository.save(site);

        log.info("Created site id: {} under organization id: {}", site.getId(), organizationId);
        return SiteResponse.fromEntity(site);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public SiteResponse updateSite(UUID id, UpdateSiteRequest request) {
        UUID organizationId = TenantContext.requireTenantId();

        Site site = siteRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Site not found"));

        String trimmedName = request.name().trim();
        if (siteRepository.existsByOrganizationIdAndNameIgnoreCaseAndIdNot(organizationId, trimmedName, id)) {
            throw new IllegalArgumentException("Another site with name '" + trimmedName + "' already exists");
        }

        site.setName(trimmedName);
        if (request.address() != null) {
            site.setAddress(request.address().isBlank() ? null : request.address().trim());
        }
        if (request.active() != null) {
            site.setActive(request.active());
        }

        site = siteRepository.save(site);
        log.info("Updated site id: {} under organization id: {}", site.getId(), organizationId);
        return SiteResponse.fromEntity(site);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public SiteResponse deactivateSite(UUID id) {
        UUID organizationId = TenantContext.requireTenantId();

        Site site = siteRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Site not found"));

        site.setActive(false);
        site = siteRepository.save(site);

        log.info("Deactivated site id: {} under organization id: {}", site.getId(), organizationId);
        return SiteResponse.fromEntity(site);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER', 'SUPERVISOR')")
    public SiteResponse getSite(UUID id) {
        UUID organizationId = TenantContext.requireTenantId();

        Site site = siteRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Site not found"));

        return SiteResponse.fromEntity(site);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER', 'SUPERVISOR')")
    public List<SiteResponse> listSites(boolean activeOnly) {
        UUID organizationId = TenantContext.requireTenantId();

        List<Site> sites = activeOnly
                ? siteRepository.findByOrganizationIdAndActiveTrue(organizationId)
                : siteRepository.findByOrganizationId(organizationId);

        return sites.stream()
                .map(SiteResponse::fromEntity)
                .toList();
    }
}
