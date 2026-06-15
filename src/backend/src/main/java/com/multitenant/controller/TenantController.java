package com.multitenant.controller;

import com.multitenant.model.core.Tenant;
import com.multitenant.repository.TenantRepository;
import com.multitenant.service.TenantService;
import com.multitenant.payload.TenantCreateRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
@SuppressWarnings("null")
public class TenantController {

    private final TenantService tenantService;
    private final TenantRepository tenantRepository;

    public TenantController(TenantService tenantService, TenantRepository tenantRepository) {
        this.tenantService = tenantService;
        this.tenantRepository = tenantRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public Tenant createTenant(@RequestBody TenantCreateRequest request) {
        return tenantService.createTenant(request.getSirutaCode(), request.getName());
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    @PostMapping("/{tenantId}/uats/{codSiruta}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public Tenant assignUatToTenant(@PathVariable String tenantId, @PathVariable String codSiruta) {
        return tenantService.assignUatToTenant(tenantId, codSiruta);
    }

    @DeleteMapping("/{tenantId}/uats/{codSiruta}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public void removeUatFromTenant(@PathVariable String tenantId, @PathVariable String codSiruta) {
        tenantService.removeUatFromTenant(tenantId, codSiruta);
    }

    @PutMapping("/{tenantId}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public Tenant updateTenant(@PathVariable String tenantId, @RequestBody TenantCreateRequest request) {
        return tenantService.updateTenant(tenantId, request.getName());
    }
}

