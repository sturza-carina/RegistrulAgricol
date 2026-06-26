package com.multitenant.config.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@org.springframework.lang.NonNull HttpServletRequest request,
                                    @org.springframework.lang.NonNull HttpServletResponse response,
                                    @org.springframework.lang.NonNull FilterChain filterChain)
            throws ServletException, IOException {

        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String tenantId = "public";

        if (auth != null && auth.getPrincipal() instanceof com.multitenant.security.UserDetailsImpl) {
            com.multitenant.security.UserDetailsImpl userDetails = (com.multitenant.security.UserDetailsImpl) auth.getPrincipal();
            if (userDetails.getTenantId() != null && !userDetails.getTenantId().isBlank()) {
                tenantId = userDetails.getTenantId();
            }
        }

        TenantContext.setCurrentTenant(tenantId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
