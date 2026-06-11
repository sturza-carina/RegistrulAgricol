package com.multitenant.controller;

import com.multitenant.model.registru.Gospodarie;
import com.multitenant.service.GospodarieService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gospodarii")
public class GospodarieController {

    private final GospodarieService gospodarieService;

    public GospodarieController(GospodarieService gospodarieService) {
        this.gospodarieService = gospodarieService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> getAllGospodarii() {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
        return ResponseEntity.ok(gospodarieService.getAllGospodarii());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> getGospodarieById(@PathVariable Long id) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot fetch a Gospodarie outside of a specific tenant context.");
        }
        return ResponseEntity.ok(gospodarieService.getGospodarieById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> createGospodarie(@RequestBody Gospodarie gospodarie) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot create a Gospodarie outside of a specific tenant context.");
        }
        return ResponseEntity.ok(gospodarieService.createGospodarie(gospodarie));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> updateGospodarie(@PathVariable Long id, @RequestBody Gospodarie gospodarie) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot update a Gospodarie outside of a specific tenant context.");
        }
        return ResponseEntity.ok(gospodarieService.updateGospodarie(id, gospodarie));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> deleteGospodarie(@PathVariable Long id) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot delete a Gospodarie outside of a specific tenant context.");
        }
        gospodarieService.deleteGospodarie(id);
        return ResponseEntity.ok().build();
    }
}
