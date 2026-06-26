package com.multitenant.controller;

import com.multitenant.model.registru.Teren;
import com.multitenant.service.TerenService;
import com.multitenant.dto.TerenWithParcelaDTO;
import com.multitenant.dto.TerenCreateDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/terenuri")
@SuppressWarnings("null")
public class TerenController {

    private final TerenService terenService;

    public TerenController(TerenService terenService) {
        this.terenService = terenService;
    }

    @GetMapping
    public ResponseEntity<?> getAllTerenuri() {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
        return ResponseEntity.ok(terenService.getAllTerenuri());
    }

    @GetMapping("/gospodarie/{gospodarieId}")
    public ResponseEntity<?> getTerenByGospodarieId(@PathVariable Long gospodarieId) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.noContent().build();
        }
        List<Teren> terenuri = terenService.getTerenByGospodarieId(gospodarieId);
        return ResponseEntity.ok(terenuri);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTerenById(@PathVariable Long id) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot fetch a Teren outside of a specific tenant context.");
        }
        return ResponseEntity.ok(terenService.getTerenById(id));
    }

    @PostMapping
    public ResponseEntity<?> createTeren(@RequestBody TerenCreateDTO dto) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot create a Teren outside of a specific tenant context.");
        }
        try {
            return ResponseEntity.ok(terenService.createTerenFromDTO(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Eroare la salvare teren: " + e.getMessage());
        }
    }

    @PostMapping("/with-parcela")
    public ResponseEntity<?> createTerenWithParcela(@RequestBody TerenWithParcelaDTO dto) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot create a Teren outside of a specific tenant context.");
        }
        return ResponseEntity.ok(terenService.createTerenWithParcela(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeren(@PathVariable Long id, @RequestBody Teren teren) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot update a Teren outside of a specific tenant context.");
        }
        return ResponseEntity.ok(terenService.updateTeren(id, teren));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeren(@PathVariable Long id) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot delete a Teren outside of a specific tenant context.");
        }
        terenService.deleteTeren(id);
        return ResponseEntity.ok().build();
    }
}
