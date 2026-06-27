package com.multitenant.controller;

import com.multitenant.dto.CulturaParcelaDto;
import com.multitenant.service.CulturaParcelaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parcele/{parcelaId}/culturi")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
public class CulturaParcelaController {

    private final CulturaParcelaService culturaParcelaService;

    @GetMapping
    public ResponseEntity<?> getCulturi(@PathVariable Long parcelaId) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
        return ResponseEntity.ok(culturaParcelaService.getCulturiByParcelaId(parcelaId));
    }

    @PostMapping
    public ResponseEntity<?> addCultura(
            @PathVariable Long parcelaId,
            @RequestBody CulturaParcelaDto dto) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot add a Cultura outside of a specific tenant context.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(culturaParcelaService.addCulturaToParcela(parcelaId, dto));
    }

    @PutMapping("/{culturaId}")
    public ResponseEntity<?> updateCultura(
            @PathVariable Long parcelaId,
            @PathVariable Long culturaId,
            @RequestBody CulturaParcelaDto dto) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot update a Cultura outside of a specific tenant context.");
        }
        return ResponseEntity.ok(culturaParcelaService.updateCultura(parcelaId, culturaId, dto));
    }

    @DeleteMapping("/{culturaId}")
    public ResponseEntity<?> deleteCultura(
            @PathVariable Long parcelaId,
            @PathVariable Long culturaId) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot delete a Cultura outside of a specific tenant context.");
        }
        culturaParcelaService.deleteCultura(parcelaId, culturaId);
        return ResponseEntity.noContent().build();
    }
}