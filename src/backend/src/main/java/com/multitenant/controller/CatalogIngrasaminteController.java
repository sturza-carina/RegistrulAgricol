package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.dto.CatalogIngrasaminteDTO;
import com.multitenant.service.CatalogIngrasaminteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalog/ingrasaminte")
@RequiredArgsConstructor
@TenantRequired
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
public class CatalogIngrasaminteController {

    private final CatalogIngrasaminteService catalogIngrasaminteService;

    @GetMapping
    public ResponseEntity<Page<CatalogIngrasaminteDTO>> getCatalog(
            @RequestParam(value = "query", required = false) String query,
            Pageable pageable) {
        return ResponseEntity.ok(catalogIngrasaminteService.getIngrasaminteCatalog(query, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatalogIngrasaminteDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(catalogIngrasaminteService.getIngrasamantById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<CatalogIngrasaminteDTO> create(@RequestBody CatalogIngrasaminteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogIngrasaminteService.createIngrasamant(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<CatalogIngrasaminteDTO> update(@PathVariable Long id, @RequestBody CatalogIngrasaminteDTO dto) {
        return ResponseEntity.ok(catalogIngrasaminteService.updateIngrasamant(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        catalogIngrasaminteService.deleteIngrasamant(id);
        return ResponseEntity.noContent().build();
    }
}
