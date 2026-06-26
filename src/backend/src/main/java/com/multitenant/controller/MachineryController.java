package com.multitenant.controller;

import com.multitenant.dto.MachineryDTO;
import com.multitenant.service.MachineryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/machinery")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
public class MachineryController {

    private final MachineryService machineryService;

    public MachineryController(MachineryService machineryService) {
        this.machineryService = machineryService;
    }

    @GetMapping
    public ResponseEntity<?> getAllMachinery() {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
        return ResponseEntity.ok(machineryService.getAll());
    }

    @GetMapping("/gospodarie/{gospodarieId}")
    public ResponseEntity<?> getMachineryByGospodarie(@PathVariable Long gospodarieId) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot fetch machinery outside of a specific tenant context.");
        }
        return ResponseEntity.ok(machineryService.getAllByGospodarie(gospodarieId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMachineryById(@PathVariable Long id) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot fetch machinery outside of a specific tenant context.");
        }
        return ResponseEntity.ok(machineryService.getById(id));
    }

    @PostMapping
    public ResponseEntity<?> createMachinery(@RequestBody MachineryDTO dto) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot create machinery outside of a specific tenant context.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(machineryService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMachinery(@PathVariable Long id, @RequestBody MachineryDTO dto) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot update machinery outside of a specific tenant context.");
        }
        return ResponseEntity.ok(machineryService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMachinery(@PathVariable Long id) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot delete machinery outside of a specific tenant context.");
        }
        machineryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
