package com.multitenant.controller;

import com.multitenant.model.registru.Parcela;
import com.multitenant.service.ParcelaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/parcele")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
public class ParcelaController {

    private final ParcelaService parcelaService;

    public ParcelaController(ParcelaService parcelaService) {
        this.parcelaService = parcelaService;
    }

    @GetMapping
    public ResponseEntity<?> getAllParcele() {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
        return ResponseEntity.ok(parcelaService.getAllParceleForTenant());
    }

    @GetMapping("/teren/{terenId}")
    public ResponseEntity<?> getParcele(@PathVariable Long terenId) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
        return ResponseEntity.ok(parcelaService.getParceleForTeren(terenId));
    }

    @PostMapping("/teren/{terenId}")
    public ResponseEntity<?> addParcela(@PathVariable Long terenId, @Valid @RequestBody Parcela parcela) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot add a Parcela outside of a specific tenant context.");
        }
        return ResponseEntity.ok(parcelaService.saveParcela(terenId, parcela));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateParcela(@PathVariable Long id, @Valid @RequestBody Parcela parcela) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot update a Parcela outside of a specific tenant context.");
        }
        return ResponseEntity.ok(parcelaService.updateParcela(id, parcela));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteParcela(@PathVariable Long id) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot delete a Parcela outside of a specific tenant context.");
        }
        parcelaService.deleteParcela(id);
        return ResponseEntity.ok().build();
    }
}
