package com.multitenant.controller;

import com.multitenant.dto.CladireDTO;
import com.multitenant.service.CladireService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gospodarii/{gospodarieId}/cladiri")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@SuppressWarnings("null")
public class CladireController {

    private final CladireService cladireService;

    public CladireController(CladireService cladireService) {
        this.cladireService = cladireService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> getCladiri(@PathVariable Long gospodarieId) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
        return ResponseEntity.ok(cladireService.getCladiriByGospodarieId(gospodarieId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createCladire(@PathVariable Long gospodarieId, @RequestBody CladireDTO dto) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot create a Cladire outside of a specific tenant context.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(cladireService.createCladire(gospodarieId, dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateCladire(@PathVariable Long gospodarieId, @PathVariable Long id, @RequestBody CladireDTO dto) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot update a Cladire outside of a specific tenant context.");
        }
        return ResponseEntity.ok(cladireService.updateCladire(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteCladire(@PathVariable Long gospodarieId, @PathVariable Long id) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot delete a Cladire outside of a specific tenant context.");
        }
        cladireService.deleteCladire(id);
        return ResponseEntity.noContent().build();
    }
}