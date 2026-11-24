package com.attendance.site.repository;

import com.attendance.site.entity.Site;
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

    boolean existsByOrganizationIdAndNameIgnoreCase(UUID organizationId, String name);

    boolean existsByOrganizationIdAndNameIgnoreCaseAndIdNot(UUID organizationId, String name, UUID id);
}
