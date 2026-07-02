package com.multitenant.payload;

public class UserInfoResponse {
    private Long id;
    private String username;
    private String role;
    private String tenantId;
    private Long uatId;

    public UserInfoResponse(Long id, String username, String role, String tenantId, Long uatId) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.tenantId = tenantId;
        this.uatId = uatId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public Long getUatId() { return uatId; }
    public void setUatId(Long uatId) { this.uatId = uatId; }
}
