package com.attendance.tenant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Thread-local holder for the authenticated user's organizationId.
 * Guarantees that tenant scoping is bound to the execution thread of each request.
 */
public final class TenantContext {

    private static final Logger log = LoggerFactory.getLogger(TenantContext.class);
    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
        // Utility class
    }

    /**
     * Sets the tenant ID for the current thread.
     *
     * @param tenantId the organization UUID
     */
    public static void setTenantId(UUID tenantId) {
        log.debug("Setting TenantContext for current thread: {}", tenantId);
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Retrieves the tenant ID for the current thread, or null if not set.
     *
     * @return the organization UUID or null
     */
    public static UUID getTenantId() {
        return CURRENT_TENANT.get();
    }

    /**
     * Retrieves the tenant ID for the current thread, throwing an exception if absent.
     *
     * @return the organization UUID
     * @throws IllegalStateException if tenant context has not been initialized
     */
    public static UUID requireTenantId() {
        UUID tenantId = CURRENT_TENANT.get();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context is required but none was set for the current thread");
        }
        return tenantId;
    }

    /**
     * Clears the tenant ID from the current thread. Must be called at the end of each request.
     */
    public static void clear() {
        log.debug("Clearing TenantContext for current thread");
        CURRENT_TENANT.remove();
    }
}
