package com.multitenant.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .subject((userPrincipal.getUsername()))
                .claim("userId", userPrincipal.getId())
                .claim("tenantId", userPrincipal.getTenantId())
                .claim("uatId", userPrincipal.getUatId())
                .claim("role", userPrincipal.getRole())
                .claim("type", "USER")
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateCetateanJwtToken(com.multitenant.model.core.Cetatean cetatean) {
        return Jwts.builder()
                .subject((cetatean.getEmail()))
                .claim("userId", cetatean.getId())
                .claim("type", "CETATEAN")
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), Jwts.SIG.HS256)
                .compact();
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public Claims getClaimsFromJwtToken(String token) {
        return Jwts.parser().verifyWith(key()).build()
               .parseSignedClaims(token).getPayload();
    }

    public String getUserNameFromJwtToken(String token) {
        return getClaimsFromJwtToken(token).getSubject();
    }

    public Long getUserIdFromJwtToken(String token) {
        return getClaimsFromJwtToken(token).get("userId", Long.class);
    }

    public String getTenantIdFromJwtToken(String token) {
        return getClaimsFromJwtToken(token).get("tenantId", String.class);
    }

    public Long getUatIdFromJwtToken(String token) {
        Object uatId = getClaimsFromJwtToken(token).get("uatId");
        if (uatId instanceof Integer) {
            return ((Integer) uatId).longValue();
        }
        return getClaimsFromJwtToken(token).get("uatId", Long.class);
    }

    public String getRoleFromJwtToken(String token) {
        return getClaimsFromJwtToken(token).get("role", String.class);
    }

    public String getTypeFromJwtToken(String token) {
        return getClaimsFromJwtToken(token).get("type", String.class);
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(key()).build().parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // log error
        }
        return false;
    }
}

