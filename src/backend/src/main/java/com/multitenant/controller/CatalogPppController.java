package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.dto.CatalogPppDTO;
import com.multitenant.service.CatalogPppService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalog/ppp")
@RequiredArgsConstructor
@TenantRequired
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
public class CatalogPppController {

    private final CatalogPppService catalogPppService;

    @GetMapping
    public ResponseEntity<Page<CatalogPppDTO>> getCatalog(
            @RequestParam(value = "query", required = false) String query,
            Pageable pageable) {
        return ResponseEntity.ok(catalogPppService.getPppCatalog(query, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatalogPppDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(catalogPppService.getPppById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<CatalogPppDTO> create(@RequestBody CatalogPppDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogPppService.createPpp(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<CatalogPppDTO> update(@PathVariable Long id, @RequestBody CatalogPppDTO dto) {
        return ResponseEntity.ok(catalogPppService.updatePpp(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        catalogPppService.deletePpp(id);
        return ResponseEntity.noContent().build();
    }
}
