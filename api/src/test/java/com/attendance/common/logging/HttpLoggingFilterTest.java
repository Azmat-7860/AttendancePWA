package com.attendance.common.logging;

import com.attendance.tenant.TenantContext;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HttpLoggingFilterTest {

    private final HttpLoggingFilter filter = new HttpLoggingFilter();

    @Test
    @DisplayName("Adds X-Request-Id to response when not provided in request")
    void shouldGenerateRequestIdWhenNotPresent() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        String requestId = response.getHeader("X-Request-Id");
        assertThat(requestId).isNotNull();
        assertThat(requestId).hasSize(8);
    }

    @Test
    @DisplayName("Propagates existing X-Request-Id header to response")
    void shouldPropagateExistingRequestId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/google");
        request.addHeader("X-Request-Id", "client-trace-1234");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertThat(response.getHeader("X-Request-Id")).isEqualTo("client-trace-1234");
    }

    @Test
    @DisplayName("Logs cleanly when tenant context is present")
    void shouldIncludeTenantContextWhenPresent() throws ServletException, IOException {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        try {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/sites");
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            filter.doFilter(request, response, filterChain);

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getHeader("X-Request-Id")).isNotNull();
        } finally {
            TenantContext.clear();
        }
    }
}
