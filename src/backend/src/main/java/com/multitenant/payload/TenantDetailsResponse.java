package com.multitenant.payload;

import java.time.LocalDateTime;

public class TenantDetailsResponse {
    private String id;
    private String name;
    private String schemaName;
    private LocalDateTime createdAt;
    private boolean isActive;

    public TenantDetailsResponse() {}

    public TenantDetailsResponse(String id, String name, String schemaName, LocalDateTime createdAt, boolean isActive) {
        this.id = id;
        this.name = name;
        this.schemaName = schemaName;
        this.createdAt = createdAt;
        this.isActive = isActive;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
