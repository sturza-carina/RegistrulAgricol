package com.multitenant.controller;

import com.multitenant.model.persoana.Persoana;
import com.multitenant.service.PersoanaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/persons", "/api/persoane"})
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
public class PersoanaController {

    private final PersoanaService persoanaService;

    public PersoanaController(PersoanaService persoanaService) {
        this.persoanaService = persoanaService;
    }

    @PostMapping
    public ResponseEntity<?> createPerson(@RequestBody Persoana persoana) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot create a Persoana outside of a specific tenant context. Please log in as a Tenant Administrator.");
        }
        return ResponseEntity.ok(persoanaService.createPerson(persoana));
    }

    @GetMapping
    public ResponseEntity<?> getAllPersons(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
        return ResponseEntity.ok(persoanaService.getAllPersons(search, type));
    }

    @GetMapping("/gospodarie/{gospodarieId}")
    public ResponseEntity<List<Persoana>> getPersonsByGospodarieId(@PathVariable Long gospodarieId) {
        return ResponseEntity.ok(persoanaService.getPersonsByGospodarieId(gospodarieId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPersonById(@PathVariable Long id) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot fetch a Persoana outside of a specific tenant context.");
        }
        return ResponseEntity.ok(persoanaService.getPersonById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePerson(@PathVariable Long id, @RequestBody Persoana persoana) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot update a Persoana outside of a specific tenant context.");
        }
        return ResponseEntity.ok(persoanaService.updatePerson(id, persoana));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePerson(@PathVariable Long id) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot delete a Persoana outside of a specific tenant context.");
        }
        persoanaService.deletePerson(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{persoanaId}/gospodarii/{gospodarieId}")
    public ResponseEntity<?> addPersonToGospodarie(@PathVariable Long persoanaId, @PathVariable Long gospodarieId) {
        if ("public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot modify a Persoana outside of a specific tenant context.");
        }
        persoanaService.addPersonToGospodarie(persoanaId, gospodarieId);
        return ResponseEntity.ok().build();
    }
}



