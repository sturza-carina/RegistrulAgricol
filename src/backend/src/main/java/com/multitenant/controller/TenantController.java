package com.multitenant.controller;

import com.multitenant.model.core.Tenant;
import com.multitenant.repository.TenantRepository;
import com.multitenant.service.TenantService;
import com.multitenant.payload.TenantCreateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
public class TenantController {

    private final TenantService tenantService;
    private final TenantRepository tenantRepository;

    public TenantController(TenantService tenantService, TenantRepository tenantRepository) {
        this.tenantService = tenantService;
        this.tenantRepository = tenantRepository;
    }

    @PostMapping
    public ResponseEntity<Tenant> createTenant(@RequestBody TenantCreateRequest request) {
        return ResponseEntity.ok(tenantService.createTenant(request.getTenantId(), request.getName()));
    }

    @GetMapping
    public ResponseEntity<List<Tenant>> getAllTenants() {
        return ResponseEntity.ok(tenantRepository.findAll());
    }

    @PutMapping("/{tenantId}")
    public ResponseEntity<Tenant> updateTenant(@PathVariable String tenantId, @RequestBody TenantCreateRequest request) {
        return ResponseEntity.ok(tenantService.updateTenant(tenantId, request.getName()));
    }

    @PostMapping("/migrate-all")
    public ResponseEntity<Void> migrateAllTenants() {
        tenantService.migrateAllTenants();
        return ResponseEntity.ok().build();
    }
}
