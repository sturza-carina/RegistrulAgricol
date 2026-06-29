package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.model.registru.CategorieFolosinta;
import com.multitenant.service.CategorieFolosintaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@SuppressWarnings("null")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@TenantRequired
public class CategorieFolosintaController {

    private final CategorieFolosintaService categorieFolosintaService;

    public CategorieFolosintaController(CategorieFolosintaService categorieFolosintaService) {
        this.categorieFolosintaService = categorieFolosintaService;
    }

    @GetMapping("/api/terenuri/{terenId}/categorii-folosinta")
    public ResponseEntity<?> getCategoriiForTeren(@PathVariable Long terenId, Pageable pageable) {
        Page<CategorieFolosinta> categorii = categorieFolosintaService.getCategoriiForTeren(terenId, pageable);
        return ResponseEntity.ok(categorii);
    }

    @PostMapping("/api/terenuri/{terenId}/categorii-folosinta")
    public ResponseEntity<?> createCategorie(@PathVariable Long terenId, @RequestBody CategorieFolosinta categorie) {
        return ResponseEntity.ok(categorieFolosintaService.saveCategorie(terenId, categorie));
    }

    @PutMapping("/api/categorii-folosinta/{id}")
    public ResponseEntity<?> updateCategorie(@PathVariable Long id, @RequestBody CategorieFolosinta categorie) {
        return ResponseEntity.ok(categorieFolosintaService.updateCategorie(id, categorie));
    }

    @DeleteMapping("/api/categorii-folosinta/{id}")
    public ResponseEntity<?> deleteCategorie(@PathVariable Long id) {
        categorieFolosintaService.deleteCategorie(id);
        return ResponseEntity.ok().build();
    }
}

