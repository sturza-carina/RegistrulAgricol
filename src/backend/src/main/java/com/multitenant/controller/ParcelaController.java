package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.model.registru.Parcela;
import com.multitenant.service.ParcelaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/parcele")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@TenantRequired
public class ParcelaController {

    private final ParcelaService parcelaService;

    public ParcelaController(ParcelaService parcelaService) {
        this.parcelaService = parcelaService;
    }

    @GetMapping
    public ResponseEntity<?> getAllParcele(Pageable pageable) {
        return ResponseEntity.ok(parcelaService.getAllParceleForTenant(pageable));
    }

    @GetMapping("/teren/{terenId}")
    public ResponseEntity<?> getParcele(@PathVariable Long terenId, Pageable pageable) {
        return ResponseEntity.ok(parcelaService.getParceleForTeren(terenId, pageable));
    }

    @PostMapping("/teren/{terenId}")
    public ResponseEntity<?> addParcela(@PathVariable Long terenId, @Valid @RequestBody Parcela parcela) {
        return ResponseEntity.ok(parcelaService.saveParcela(terenId, parcela));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateParcela(@PathVariable Long id, @Valid @RequestBody Parcela parcela) {
        return ResponseEntity.ok(parcelaService.updateParcela(id, parcela));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteParcela(@PathVariable Long id) {
        parcelaService.deleteParcela(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/istoric")
    public ResponseEntity<?> getParcelaHistory(@PathVariable Long id) {
        return ResponseEntity.ok(parcelaService.getParcelaHistory(id));
    }
}
