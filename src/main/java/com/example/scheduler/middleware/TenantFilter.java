package com.example.scheduler.middleware;

import com.example.scheduler.config.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class TenantFilter extends OncePerRequestFilter {
    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Value("${security.tenant-exempt-paths:}")
    private String[] tenantExemptPaths;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        if (isExempt(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String tenantId = request.getHeader(TENANT_HEADER);

            if (tenantId == null || tenantId.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("El encabezado X-Tenant-ID es obligatorio.");
                return;
            }

            TenantContext.setCurrentTenant(tenantId);
            filterChain.doFilter(request, response);

        } finally {
            TenantContext.clear();
        }
    }

    private boolean isExempt(String requestUri) {
        return Arrays.stream(tenantExemptPaths).anyMatch(pattern -> PATH_MATCHER.match(pattern, requestUri));
    }
}
