package com.attendance.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * AOP Aspect that activates Hibernate's tenantFilter before any Spring Data repository method executes.
 * Ensures all SELECT/UPDATE/DELETE queries automatically include "WHERE organization_id = :tenantId".
 */
@Aspect
@Component
public class TenantFilterAspect {

    private static final Logger log = LoggerFactory.getLogger(TenantFilterAspect.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* org.springframework.data.repository.Repository+.*(..))")
    public void enableTenantFilter() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            log.debug("Enabling tenantFilter for tenantId: {}", tenantId);
            try {
                Session session = entityManager.unwrap(Session.class);
                session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
            } catch (Exception e) {
                log.warn("Failed to activate tenantFilter on Hibernate session: {}", e.getMessage());
            }
        }
    }
}
