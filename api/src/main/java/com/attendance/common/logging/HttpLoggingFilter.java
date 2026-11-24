package com.attendance.common.logging;

import com.attendance.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that automatically intercepts every HTTP request to log method, URI,
 * response status, duration (ms), tenant correlation, and unique Request ID.
 *
 * Implements TRD Section 17 & AGENTS.md rules:
 * - Logs entity IDs only (orgId), never personal data or tokens.
 * - Suppresses health-check and swagger noise in production logs.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
public class HttpLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(HttpLoggingFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String MDC_REQUEST_ID = "requestId";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }

        MDC.put(MDC_REQUEST_ID, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        String method = request.getMethod();
        String uri = request.getRequestURI();

        log.debug("[HTTP REQ] ---> {} {} [reqId={}]", method, uri, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            UUID tenantId = TenantContext.getTenantId();
            String tenantContextStr = (tenantId != null) ? " | orgId=" + tenantId : "";

            boolean isHealthOrDoc = uri.equals("/health")
                    || uri.startsWith("/actuator")
                    || uri.startsWith("/swagger")
                    || uri.startsWith("/v3/api-docs");

            if (status >= 500) {
                log.error("[HTTP RES] <--- {} {} -> {} (took {}ms){}", method, uri, status, duration, tenantContextStr);
            } else if (status >= 400) {
                log.warn("[HTTP RES] <--- {} {} -> {} (took {}ms){}", method, uri, status, duration, tenantContextStr);
            } else if (isHealthOrDoc) {
                // Routine health checks are kept at DEBUG to avoid log pollution
                log.debug("[HTTP RES] <--- {} {} -> {} (took {}ms){}", method, uri, status, duration, tenantContextStr);
            } else {
                log.info("[HTTP RES] <--- {} {} -> {} (took {}ms){}", method, uri, status, duration, tenantContextStr);
            }

            MDC.remove(MDC_REQUEST_ID);
        }
    }
}
