package com.multitenant.controller;

import com.multitenant.model.Tenant;
import com.multitenant.service.TenantService;
import com.multitenant.payload.TenantCreateRequest;
import com.multitenant.payload.TenantDetailsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public Tenant createTenant(@RequestBody TenantCreateRequest request) {
        return tenantService.createTenant(request.getName());
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public List<Tenant> getAllTenants() {
        return tenantService.getAllTenants();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public Tenant getTenantById(@PathVariable String id) {
        return tenantService.getTenantById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public Tenant updateTenant(@PathVariable String id, @RequestBody TenantDetailsResponse request) {
        return tenantService.updateTenant(id, request.getName(), request.isActive());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<Void> deleteTenant(@PathVariable String id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.noContent().build();
    }
}
