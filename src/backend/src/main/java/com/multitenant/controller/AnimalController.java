package com.multitenant.controller;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.model.animal.AnimalIndividual;
import com.multitenant.model.animal.EfectivGrup;
import com.multitenant.service.AnimalIndividualService;
import com.multitenant.service.EfectivGrupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/animals")
public class AnimalController {

    private final AnimalIndividualService animalIndividualService;
    private final EfectivGrupService efectivGrupService;

    public AnimalController(AnimalIndividualService animalIndividualService,
                            EfectivGrupService efectivGrupService) {
        this.animalIndividualService = animalIndividualService;
        this.efectivGrupService = efectivGrupService;
    }

    // --- AnimalIndividual Endpoints ---

    @GetMapping("/individual")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<List<AnimalIndividual>> getAllIndividuals() {
        if ("public".equals(TenantContext.getCurrentTenant())) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(animalIndividualService.getAll());
    }

    @GetMapping("/individual/{id}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getIndividualById(@PathVariable Long id) {
        if ("public".equals(TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot fetch outside tenant context.");
        }
        return ResponseEntity.ok(animalIndividualService.getById(id));
    }

    @PostMapping("/individual")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> createIndividual(@RequestBody AnimalIndividual animal) {
        if ("public".equals(TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot create outside tenant context.");
        }
        return ResponseEntity.ok(animalIndividualService.create(animal));
    }

    @PutMapping("/individual/{id}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> updateIndividual(@PathVariable Long id, @RequestBody AnimalIndividual animal) {
        if ("public".equals(TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot update outside tenant context.");
        }
        return ResponseEntity.ok(animalIndividualService.update(id, animal));
    }

    @DeleteMapping("/individual/{id}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> deleteIndividual(@PathVariable Long id) {
        if ("public".equals(TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot delete outside tenant context.");
        }
        animalIndividualService.delete(id);
        return ResponseEntity.ok().build();
    }

    // --- EfectivGrup Endpoints ---

    @GetMapping("/grup")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<List<EfectivGrup>> getAllGroups() {
        if ("public".equals(TenantContext.getCurrentTenant())) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(efectivGrupService.getAll());
    }

    @GetMapping("/grup/{id}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getGroupById(@PathVariable Long id) {
        if ("public".equals(TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot fetch outside tenant context.");
        }
        return ResponseEntity.ok(efectivGrupService.getById(id));
    }

    @PostMapping("/grup")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> createGroup(@RequestBody EfectivGrup grup) {
        if ("public".equals(TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot create outside tenant context.");
        }
        return ResponseEntity.ok(efectivGrupService.create(grup));
    }

    @PutMapping("/grup/{id}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> updateGroup(@PathVariable Long id, @RequestBody EfectivGrup grup) {
        if ("public".equals(TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot update outside tenant context.");
        }
        return ResponseEntity.ok(efectivGrupService.update(id, grup));
    }

    @DeleteMapping("/grup/{id}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> deleteGroup(@PathVariable Long id) {
        if ("public".equals(TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot delete outside tenant context.");
        }
        efectivGrupService.delete(id);
        return ResponseEntity.ok().build();
    }

    // --- Combined Query by Proprietar (Persoana) ---

    @GetMapping("/proprietar/{proprietarId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getAnimalsByProprietar(@PathVariable Long proprietarId) {
        if ("public".equals(TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot fetch outside tenant context.");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("individuals", animalIndividualService.getByProprietarId(proprietarId));
        result.put("groups", efectivGrupService.getByProprietarId(proprietarId));
        return ResponseEntity.ok(result);
    }
}
