package com.attendance.site.dto;

import com.attendance.site.entity.Site;

import java.time.Instant;
import java.util.UUID;

public record SiteResponse(
        UUID id,
        String name,
        String address,
        boolean active,
        UUID organizationId,
        Instant createdAt,
        Instant updatedAt
) {
    public static SiteResponse fromEntity(Site site) {
        return new SiteResponse(
                site.getId(),
                site.getName(),
                site.getAddress(),
                site.isActive(),
                site.getOrganizationId(),
                site.getCreatedAt(),
                site.getUpdatedAt()
        );
    }
}
