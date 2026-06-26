package com.multitenant.payload;

import lombok.Data;

@Data
public class ImpersonateRequest {
    private String tenantId;
}
