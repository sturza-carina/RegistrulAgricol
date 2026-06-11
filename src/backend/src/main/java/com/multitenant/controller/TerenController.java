package com.multitenant.controller;

import com.multitenant.model.registru.Teren;
import com.multitenant.service.TerenService;
import com.multitenant.dto.TerenWithParcelaDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/terenuri")
public class TerenController {

    private final TerenService terenService;

    public TerenController(TerenService terenService) {
        this.terenService = terenService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> getAllTerenuri() {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
        return ResponseEntity.ok(terenService.getAllTerenuri());
    }

    @GetMapping("/gospodarie/{gospodarieId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> getTerenByGospodarieId(@PathVariable Long gospodarieId) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.noContent().build();
        }
        Teren teren = terenService.getTerenByGospodarieId(gospodarieId);
        if (teren == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(teren);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> getTerenById(@PathVariable Long id) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot fetch a Teren outside of a specific tenant context.");
        }
        return ResponseEntity.ok(terenService.getTerenById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> createTeren(@RequestBody Teren teren) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot create a Teren outside of a specific tenant context.");
        }
        return ResponseEntity.ok(terenService.createTeren(teren));
    }

    @PostMapping("/with-parcela")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> createTerenWithParcela(@RequestBody TerenWithParcelaDTO dto) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot create a Teren outside of a specific tenant context.");
        }
        return ResponseEntity.ok(terenService.createTerenWithParcela(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> updateTeren(@PathVariable Long id, @RequestBody Teren teren) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot update a Teren outside of a specific tenant context.");
        }
        return ResponseEntity.ok(terenService.updateTeren(id, teren));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> deleteTeren(@PathVariable Long id) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot delete a Teren outside of a specific tenant context.");
        }
        terenService.deleteTeren(id);
        return ResponseEntity.ok().build();
    }
}
