package com.multitenant.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

public class JwtResponse {
    private String token;
    private Long id;
    private String username;
    private String role;
    private String tenantId;

    public JwtResponse(String token, Long id, String username, String role, String tenantId) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.role = role;
        this.tenantId = tenantId;
    }

    public String getToken() { return token; }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getTenantId() { return tenantId; }
}
