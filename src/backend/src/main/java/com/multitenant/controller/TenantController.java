package com.multitenant.controller;

import com.multitenant.model.Tenant;
import com.multitenant.repository.TenantRepository;
import com.multitenant.service.TenantService;
import com.multitenant.payload.TenantCreateRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;
    private final TenantRepository tenantRepository;

    public TenantController(TenantService tenantService, TenantRepository tenantRepository) {
        this.tenantService = tenantService;
        this.tenantRepository = tenantRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Tenant createTenant(@RequestBody TenantCreateRequest request) {
        return tenantService.createTenant(request.getSirutaCode(), request.getName());
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }
}
