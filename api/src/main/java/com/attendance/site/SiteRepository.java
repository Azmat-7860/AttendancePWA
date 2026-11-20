package com.attendance.site;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SiteRepository extends JpaRepository<Site, UUID> {

    Optional<Site> findByIdAndOrganizationId(UUID id, UUID organizationId);

    List<Site> findByOrganizationId(UUID organizationId);

    List<Site> findByOrganizationIdAndActiveTrue(UUID organizationId);
}
