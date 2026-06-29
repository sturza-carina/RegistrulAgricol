package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.dto.CladireDTO;
import com.multitenant.service.CladireService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/gospodarii/{gospodarieId}/cladiri")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@SuppressWarnings("null")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@TenantRequired
public class CladireController {

    private final CladireService cladireService;

    public CladireController(CladireService cladireService) {
        this.cladireService = cladireService;
    }

    @GetMapping
    public ResponseEntity<?> getCladiri(@PathVariable Long gospodarieId, Pageable pageable) {
        return ResponseEntity.ok(cladireService.getCladiriByGospodarieId(gospodarieId, pageable));
    }

    @PostMapping
    public ResponseEntity<?> createCladire(@PathVariable Long gospodarieId, @RequestBody CladireDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cladireService.createCladire(gospodarieId, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCladire(@PathVariable Long gospodarieId, @PathVariable Long id, @RequestBody CladireDTO dto) {
        return ResponseEntity.ok(cladireService.updateCladire(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCladire(@PathVariable Long gospodarieId, @PathVariable Long id) {
        cladireService.deleteCladire(id);
        return ResponseEntity.noContent().build();
    }
}