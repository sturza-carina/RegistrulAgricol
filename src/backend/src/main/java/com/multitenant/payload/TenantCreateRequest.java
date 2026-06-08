package com.multitenant.payload;

import lombok.Data;

@Data
public class TenantCreateRequest {
    private String sirutaCode;
    private String name;
}
