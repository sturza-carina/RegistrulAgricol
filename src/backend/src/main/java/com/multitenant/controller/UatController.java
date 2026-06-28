package com.multitenant.controller;

import com.multitenant.model.core.PublicUat;
import com.multitenant.model.core.Uat;
import com.multitenant.service.UatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/uats")
@CrossOrigin(origins = "*")
public class UatController {

    private final UatService uatService;

    public UatController(UatService uatService) {
        this.uatService = uatService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GLOBAL public.uat endpoints (Super Admin only)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * List all UATs from the global public registry.
     * Used by Super Admin for management, and by all roles for dropdown lookups.
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<List<PublicUat>> getAllPublicUats() {
        return ResponseEntity.ok(uatService.getAllPublicUats());
    }

    /**
     * Create a new UAT in the global registry (Super Admin only).
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<PublicUat> createPublicUat(@RequestBody PublicUat uat) {
        return ResponseEntity.ok(uatService.createPublicUat(uat));
    }

    /**
     * Update a UAT in the global registry (Super Admin only).
     */
    @PutMapping("/{codSiruta}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<PublicUat> updatePublicUat(@PathVariable String codSiruta, @RequestBody PublicUat request) {
        return ResponseEntity.ok(uatService.updatePublicUat(codSiruta, request));
    }

    /**
     * Delete a UAT from the global registry (Super Admin only).
     */
    @DeleteMapping("/{codSiruta}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<Void> deletePublicUat(@PathVariable String codSiruta) {
        uatService.deletePublicUat(codSiruta);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/judete")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<List<String>> getJudete() {
        return ResponseEntity.ok(uatService.getDistinctJudete());
    }

    @GetMapping("/localitati")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<List<PublicUat>> getLocalitatiByJudet(@RequestParam String judet) {
        return ResponseEntity.ok(uatService.getLocalitatiByJudet(judet));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TENANT-LOCAL uat endpoints (Tenant Admin)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * List UATs currently assigned to (and active in) the current tenant's schema.
     */
    @GetMapping("/tenant")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<List<Uat>> getTenantUats() {
        return ResponseEntity.ok(uatService.getTenantUats());
    }

    /**
     * Assign a UAT from the global registry into the current tenant's local schema.
     * Copies UAT data from public.uat → tenant_X.uat.
     */
    @PostMapping("/tenant/{codSiruta}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Uat> assignUatToTenant(@PathVariable String codSiruta) {
        return ResponseEntity.ok(uatService.assignUatToTenant(codSiruta));
    }

    /**
     * Remove a UAT from the current tenant's local schema.
     */
    @DeleteMapping("/tenant/{codSiruta}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> removeUatFromTenant(@PathVariable String codSiruta) {
        uatService.removeUatFromTenant(codSiruta);
        return ResponseEntity.ok().build();
    }
}
