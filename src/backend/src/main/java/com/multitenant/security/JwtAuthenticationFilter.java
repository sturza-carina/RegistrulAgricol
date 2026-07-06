package com.multitenant.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(@org.springframework.lang.NonNull HttpServletRequest request,
                                    @org.springframework.lang.NonNull HttpServletResponse response,
                                    @org.springframework.lang.NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                Long userId = jwtUtils.getUserIdFromJwtToken(jwt);
                String tenantId = jwtUtils.getTenantIdFromJwtToken(jwt);
                String role = jwtUtils.getRoleFromJwtToken(jwt);
                Long uatId = jwtUtils.getUatIdFromJwtToken(jwt);

                String type = jwtUtils.getTypeFromJwtToken(jwt);
                
                if ("CETATEAN".equals(type)) {
                    com.multitenant.model.core.Cetatean cetatean = new com.multitenant.model.core.Cetatean();
                    cetatean.setId(userId);
                    cetatean.setEmail(username);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            cetatean, null, java.util.Collections.emptyList());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    org.springframework.security.core.GrantedAuthority authority = new org.springframework.security.core.authority.SimpleGrantedAuthority(role);
                    UserDetailsImpl userDetails = new UserDetailsImpl(
                            userId,
                            username,
                            "",
                            role,
                            tenantId,
                            uatId,
                            true,
                            java.util.Collections.singletonList(authority)
                    );

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            // log exception
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        if (request.getCookies() != null) {
            String adminJwt = null;
            String cetateanJwt = null;
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    adminJwt = cookie.getValue();
                } else if ("jwt_cetatean".equals(cookie.getName())) {
                    cetateanJwt = cookie.getValue();
                }
            }

            boolean isCetateanRoute = request.getRequestURI().startsWith("/api/public/");
            if (isCetateanRoute && cetateanJwt != null) {
                return cetateanJwt;
            } else if (!isCetateanRoute && adminJwt != null) {
                return adminJwt;
            } else if (cetateanJwt != null) {
                return cetateanJwt;
            } else if (adminJwt != null) {
                return adminJwt;
            }
        }

        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
