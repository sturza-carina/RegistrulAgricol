package com.multitenant.payload;

import lombok.Data;

@Data
public class TenantCreateRequest {
    private String tenantId;
    private String name;
}
