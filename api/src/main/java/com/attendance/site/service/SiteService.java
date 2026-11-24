package com.attendance.site.service;

import com.attendance.site.dto.CreateSiteRequest;
import com.attendance.site.dto.SiteResponse;
import com.attendance.site.dto.UpdateSiteRequest;

import java.util.List;
import java.util.UUID;

public interface SiteService {

    SiteResponse createSite(CreateSiteRequest request);

    SiteResponse updateSite(UUID id, UpdateSiteRequest request);

    SiteResponse deactivateSite(UUID id);

    SiteResponse getSite(UUID id);

    List<SiteResponse> listSites(boolean activeOnly);
}
