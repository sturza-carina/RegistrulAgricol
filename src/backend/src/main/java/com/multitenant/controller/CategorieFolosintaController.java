package com.multitenant.controller;

import com.multitenant.model.registru.CategorieFolosinta;
import com.multitenant.service.CategorieFolosintaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SuppressWarnings("null")
public class CategorieFolosintaController {

    private final CategorieFolosintaService categorieFolosintaService;

    public CategorieFolosintaController(CategorieFolosintaService categorieFolosintaService) {
        this.categorieFolosintaService = categorieFolosintaService;
    }

    @GetMapping("/api/terenuri/{terenId}/categorii-folosinta")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> getCategoriiForTeren(@PathVariable Long terenId) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
        List<CategorieFolosinta> categorii = categorieFolosintaService.getCategoriiForTeren(terenId);
        return ResponseEntity.ok(categorii);
    }

    @PostMapping("/api/terenuri/{terenId}/categorii-folosinta")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> createCategorie(@PathVariable Long terenId, @RequestBody CategorieFolosinta categorie) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot create categorie outside of a specific tenant context.");
        }
        return ResponseEntity.ok(categorieFolosintaService.saveCategorie(terenId, categorie));
    }

    @PutMapping("/api/categorii-folosinta/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> updateCategorie(@PathVariable Long id, @RequestBody CategorieFolosinta categorie) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot update categorie outside of a specific tenant context.");
        }
        return ResponseEntity.ok(categorieFolosintaService.updateCategorie(id, categorie));
    }

    @DeleteMapping("/api/categorii-folosinta/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> deleteCategorie(@PathVariable Long id) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot delete categorie outside of a specific tenant context.");
        }
        categorieFolosintaService.deleteCategorie(id);
        return ResponseEntity.ok().build();
    }
}
