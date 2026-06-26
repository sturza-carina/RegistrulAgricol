package com.multitenant.controller;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.model.animal.AnimalIndividual;
import com.multitenant.model.animal.EfectivGrup;
import com.multitenant.model.animal.EvenimentAnimal;
import com.multitenant.service.AnimalIndividualService;
import com.multitenant.service.CrossTenantTransferService;
import com.multitenant.service.EfectivGrupService;
import com.multitenant.service.EvenimentAnimalService;
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
    private final EvenimentAnimalService evenimentAnimalService;
    private final CrossTenantTransferService transferService;

    public AnimalController(AnimalIndividualService animalIndividualService,
                            EfectivGrupService efectivGrupService,
                            EvenimentAnimalService evenimentAnimalService,
                            CrossTenantTransferService transferService) {
        this.animalIndividualService = animalIndividualService;
        this.efectivGrupService = efectivGrupService;
        this.evenimentAnimalService = evenimentAnimalService;
        this.transferService = transferService;
    }

    // =========================================================================
    // AnimalIndividual — CRUD
    // =========================================================================

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

    // =========================================================================
    // AnimalIndividual — Timeline (Evenimente)
    // =========================================================================

    /**
     * POST /api/animals/individual/{id}/evenimente
     * Adaugă un eveniment nou în istoricul unui animal (NASTERE, TRATAMENT_VETERINAR, VANZARE etc.)
     */
    @PostMapping("/individual/{id}/evenimente")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> adaugaEveniment(
            @PathVariable Long id,
            @RequestBody EvenimentAnimal eveniment) {
        if ("public".equals(TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot create outside tenant context.");
        }
        return ResponseEntity.ok(evenimentAnimalService.adaugaEveniment(id, eveniment));
    }

    /**
     * GET /api/animals/individual/{id}/evenimente
     * Returnează timeline-ul complet al unui animal, ordonat descrescător după dată.
     */
    @GetMapping("/individual/{id}/evenimente")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getTimeline(@PathVariable Long id) {
        if ("public".equals(TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot fetch outside tenant context.");
        }
        return ResponseEntity.ok(evenimentAnimalService.getTimeline(id));
    }

    // =========================================================================
    // AnimalIndividual — Cross-Tenant Transfer
    // =========================================================================

    /**
     * POST /api/animals/individual/{id}/transfer
     * Transferă un animal individual dintr-un tenant în altul.
     * Accesibil tuturor rolurilor (USER, ADMIN, SUPER_ADMIN) conform cerințelor de business.
     *
     * Body: { destinatarTenantId, destinatarGospodarieId, destinatarProprietarId, detaliiTransfer? }
     */
    @PostMapping("/individual/{id}/transfer")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> transferAnimal(
            @PathVariable Long id,
            @RequestBody CrossTenantTransferService.TransferRequest request) {
        if ("public".equals(TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot transfer outside tenant context.");
        }
        Long newAnimalId = transferService.transferAnimal(id, request);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Transfer finalizat cu succes.");
        result.put("sourceTenant", TenantContext.getCurrentTenant());
        result.put("destinatarTenantId", request.destinatarTenantId());
        result.put("newAnimalId", newAnimalId);
        return ResponseEntity.ok(result);
    }

    // =========================================================================
    // EfectivGrup — Snapshot Model
    // =========================================================================

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

    /**
     * PUT /api/animals/grup/{id}/snapshot
     * Adaugă un snapshot nou (creează un rând nou cu data curentă).
     * NU modifică rândul existent — modelul este append-only (ANSVSA traceability).
     */
    @PostMapping("/grup/{id}/snapshot")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> addGrupSnapshot(@PathVariable Long id, @RequestBody EfectivGrup grup) {
        if ("public".equals(TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot update outside tenant context.");
        }
        return ResponseEntity.ok(efectivGrupService.addSnapshot(id, grup));
    }

    /**
     * GET /api/animals/grup/{gospodarieId}/history
     * Returnează istoricul complet al efectivelor de grup pentru o gospodărie.
     */
    @GetMapping("/grup/{gospodarieId}/history")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getGrupHistory(@PathVariable Long gospodarieId) {
        if ("public".equals(TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot fetch outside tenant context.");
        }
        return ResponseEntity.ok(efectivGrupService.getHistoryByGospodarieId(gospodarieId));
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

    // =========================================================================
    // Combined Queries
    // =========================================================================

    /**
     * GET /api/animals/proprietar/{proprietarId}
     * Returnează toate animalele (individuale + grupuri) ale unui proprietar.
     */
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

    /**
     * GET /api/animals/gospodarie/{gospodarieId}
     * Returnează toate animalele (individuale + ultimul snapshot per specie de grup)
     * dintr-o gospodărie specificată. Necesar pentru view-ul gospodărie-details.
     */
    @GetMapping("/gospodarie/{gospodarieId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getAnimalsByGospodarie(@PathVariable Long gospodarieId) {
        if ("public".equals(TenantContext.getCurrentTenant())) {
            return ResponseEntity.badRequest().body("Cannot fetch outside tenant context.");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("individuals", animalIndividualService.getByGospodarieId(gospodarieId));
        result.put("grupuriCurente", efectivGrupService.getLatestByGospodarieId(gospodarieId));
        result.put("grupuriIstorice", efectivGrupService.getHistoryByGospodarieId(gospodarieId));
        return ResponseEntity.ok(result);
    }
}
