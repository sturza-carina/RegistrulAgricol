package com.multitenant.controller;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.dto.SursaApaDTO;
import com.multitenant.service.SursaApaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
@RestController
@RequestMapping("/api/parcele/{parcelaId}/surse-apa")
@RequiredArgsConstructor
public class SursaApaController {

    private final SursaApaService sursaApaService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> getSurse(@PathVariable Long parcelaId) {
        if ("public".equals(TenantContext.getCurrentTenant())) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(sursaApaService.getSurseByParcelaId(parcelaId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> addSursa(
            @PathVariable Long parcelaId,
            @RequestBody SursaApaDTO dto) {
        if ("public".equals(TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot add a SursaApa outside of a specific tenant context.");
        }
        SursaApaDTO created = sursaApaService.addSursaToParcela(parcelaId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{sursaId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> updateSursa(
            @PathVariable Long parcelaId,
            @PathVariable Long sursaId,
            @RequestBody SursaApaDTO dto) {
        if ("public".equals(TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot update a SursaApa outside of a specific tenant context.");
        }
        return ResponseEntity.ok(sursaApaService.updateSursa(parcelaId, sursaId, dto));
    }

    @DeleteMapping("/{sursaId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> deleteSursa(
            @PathVariable Long parcelaId,
            @PathVariable Long sursaId) {
        if ("public".equals(TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot delete a SursaApa outside of a specific tenant context.");
        }
        sursaApaService.deleteSursa(parcelaId, sursaId);
        return ResponseEntity.ok().build();
    }
}
