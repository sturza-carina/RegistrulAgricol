package com.multitenant.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${keycloak.jwks-uri}")
    private String keycloakJwksUri;

    private NimbusJwtDecoder keycloakDecoder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        try {
            keycloakDecoder = NimbusJwtDecoder.withJwkSetUri(keycloakJwksUri).build();
        } catch (Exception e) {
            logger.warn("Could not initialize Keycloak JWT decoder at startup — will retry lazily. Cause: " + e.getMessage());
        }
    }

    @Override
    protected void doFilterInternal(
            @org.springframework.lang.NonNull HttpServletRequest request,
            @org.springframework.lang.NonNull HttpServletResponse response,
            @org.springframework.lang.NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null) {
                String alg = getAlgorithmFromJwt(jwt);

                if ("HS256".equals(alg)) {
                    handleLocalJwt(jwt, request);
                } else if ("RS256".equals(alg)) {
                    handleKeycloakJwt(jwt, request);
                }
            }
        } catch (Exception e) {
            logger.debug("JWT filter error: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    // ── Local JWT (HS256) ──────────────────────────────────────────────────────

    private void handleLocalJwt(String jwt, HttpServletRequest request) {
        if (!jwtUtils.validateJwtToken(jwt)) return;

        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        Long userId     = jwtUtils.getUserIdFromJwtToken(jwt);
        String tenantId = jwtUtils.getTenantIdFromJwtToken(jwt);
        String role     = jwtUtils.getRoleFromJwtToken(jwt);

        GrantedAuthority authority = new SimpleGrantedAuthority(role);
        UserDetailsImpl userDetails = new UserDetailsImpl(
                userId, username, "", role, tenantId, true,
                Collections.singletonList(authority));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ── Keycloak JWT (RS256) ───────────────────────────────────────────────────

    private void handleKeycloakJwt(String jwt, HttpServletRequest request) {
        try {
            if (keycloakDecoder == null) {
                keycloakDecoder = NimbusJwtDecoder.withJwkSetUri(keycloakJwksUri).build();
            }

            Jwt decoded = keycloakDecoder.decode(jwt);

            String username = decoded.getClaimAsString("preferred_username");
            String role     = decoded.getClaimAsString("role");
            String tenantId = decoded.getClaimAsString("tenant_id");

            if (username == null) username = decoded.getSubject();
            if (role == null)     role = "ROLE_USER";
            if (tenantId == null) tenantId = "";

            GrantedAuthority authority = new SimpleGrantedAuthority(role);
            UserDetailsImpl userDetails = new UserDetailsImpl(
                    0L, username, "", role, tenantId, true,
                    Collections.singletonList(authority));

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (JwtException e) {
            logger.debug("Invalid Keycloak JWT: " + e.getMessage());
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private String getAlgorithmFromJwt(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;
            byte[] decoded = Base64.getUrlDecoder().decode(parts[0]);
            @SuppressWarnings("unchecked")
            Map<String, Object> header = objectMapper.readValue(decoded, Map.class);
            return (String) header.get("alg");
        } catch (Exception e) {
            return null;
        }
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
