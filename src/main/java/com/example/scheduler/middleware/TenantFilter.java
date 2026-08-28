package com.example.scheduler.middleware;

import com.example.scheduler.config.tenant.TenantContext;
import com.example.scheduler.enums.ERole;
import com.example.scheduler.exception.BadRequestException;
import com.example.scheduler.exception.BusinessException;
import com.example.scheduler.repository.PatientRepository;
import com.example.scheduler.repository.PersonalRepository;
import com.example.scheduler.security.SecurityUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {
    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final PatientRepository patientRepository;
    private final PersonalRepository personalRepository;
    private final HandlerExceptionResolver handlerExceptionResolver;

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
                throw new BadRequestException("The X-Tenant-ID header is missing");
            }

            TenantContext.setCurrentTenant(tenantId);

            if (!belongsToTenant()) {
                throw new BusinessException("That user is not allowed to access this tenant");
            }

            filterChain.doFilter(request, response);

        } catch (BadRequestException | BusinessException ex) {
            handlerExceptionResolver.resolveException(request, response, null, ex);
        } finally {
            TenantContext.clear();
        }
    }

    private boolean belongsToTenant() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return true;

        String role = SecurityUtils.extractRole(auth);
        if (role == null || role.equals("INTEGRATION")) return true;

        Long userId = Long.parseLong(auth.getName());
        return role.equals(ERole.PATIENT.name())
                ? patientRepository.existsById(userId)
                : personalRepository.existsById(userId);
    }

    private boolean isExempt(String requestUri) {
        return Arrays.stream(tenantExemptPaths).anyMatch(pattern -> PATH_MATCHER.match(pattern, requestUri));
    }
}
