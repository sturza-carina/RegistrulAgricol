package com.multitenant.aspect;

import com.multitenant.config.tenant.TenantContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Aspect
@Component
public class TenantRequiredAspect {

    @Before("@annotation(com.multitenant.annotation.TenantRequired) || @within(com.multitenant.annotation.TenantRequired)")
    public void checkTenantContext(JoinPoint joinPoint) {
        if ("public".equals(TenantContext.getCurrentTenant())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "Cannot perform this action outside of a specific tenant context. Please select a UAT/Tenant."
            );
        }
    }
}
