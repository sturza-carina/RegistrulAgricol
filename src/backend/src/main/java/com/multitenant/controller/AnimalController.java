package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
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
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/animals")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@TenantRequired
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
    public ResponseEntity<?> getAllIndividuals(Pageable pageable) {
        return ResponseEntity.ok(animalIndividualService.getAll(pageable));
    }

    @GetMapping("/individual/{id}")
    public ResponseEntity<?> getIndividualById(@PathVariable Long id) {
        return ResponseEntity.ok(animalIndividualService.getById(id));
    }

    @PostMapping("/individual")
    public ResponseEntity<?> createIndividual(@RequestBody AnimalIndividual animal) {
        return ResponseEntity.ok(animalIndividualService.create(animal));
    }

    @PutMapping("/individual/{id}")
    public ResponseEntity<?> updateIndividual(@PathVariable Long id, @RequestBody AnimalIndividual animal) {
        return ResponseEntity.ok(animalIndividualService.update(id, animal));
    }

    @DeleteMapping("/individual/{id}")
    public ResponseEntity<?> deleteIndividual(@PathVariable Long id) {
        animalIndividualService.delete(id);
        return ResponseEntity.ok().build();
    }

    // =========================================================================
    // AnimalIndividual — Timeline (Evenimente)
    // =========================================================================

    @PostMapping("/individual/{id}/evenimente")
    public ResponseEntity<?> adaugaEveniment(
            @PathVariable Long id,
            @RequestBody EvenimentAnimal eveniment) {
        return ResponseEntity.ok(evenimentAnimalService.adaugaEveniment(id, eveniment));
    }

    @GetMapping("/individual/{id}/evenimente")
    public ResponseEntity<?> getTimeline(@PathVariable Long id) {
        return ResponseEntity.ok(evenimentAnimalService.getTimeline(id));
    }

    // =========================================================================
    // AnimalIndividual — Cross-Tenant Transfer
    // =========================================================================

    @PostMapping("/individual/{id}/transfer")
    public ResponseEntity<?> transferAnimal(
            @PathVariable Long id,
            @RequestBody CrossTenantTransferService.TransferRequest request) {
        Long newAnimalId = transferService.transferAnimal(id, request);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Transfer finalizat cu succes.");
        result.put("sourceTenant", com.multitenant.config.tenant.TenantContext.getCurrentTenant());
        result.put("destinatarTenantId", request.destinatarTenantId());
        result.put("newAnimalId", newAnimalId);
        return ResponseEntity.ok(result);
    }

    // =========================================================================
    // EfectivGrup — Snapshot Model
    // =========================================================================

    @GetMapping("/grup")
    public ResponseEntity<?> getAllGroups(Pageable pageable) {
        return ResponseEntity.ok(efectivGrupService.getAll(pageable));
    }

    @GetMapping("/grup/{id}")
    public ResponseEntity<?> getGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(efectivGrupService.getById(id));
    }

    @PostMapping("/grup")
    public ResponseEntity<?> createGroup(@RequestBody EfectivGrup grup) {
        return ResponseEntity.ok(efectivGrupService.create(grup));
    }

    @PostMapping("/grup/{id}/snapshot")
    public ResponseEntity<?> addGrupSnapshot(@PathVariable Long id, @RequestBody EfectivGrup grup) {
        return ResponseEntity.ok(efectivGrupService.addSnapshot(id, grup));
    }

    @GetMapping("/grup/{gospodarieId}/history")
    public ResponseEntity<?> getGrupHistory(@PathVariable Long gospodarieId, Pageable pageable) {
        return ResponseEntity.ok(efectivGrupService.getHistoryByGospodarieId(gospodarieId, pageable));
    }

    @DeleteMapping("/grup/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable Long id) {
        efectivGrupService.delete(id);
        return ResponseEntity.ok().build();
    }

    // =========================================================================
    // Combined Queries
    // =========================================================================

    @GetMapping("/proprietar/{proprietarId}")
    public ResponseEntity<?> getAnimalsByProprietar(@PathVariable Long proprietarId, Pageable pageable) {
        Map<String, Object> result = new HashMap<>();
        result.put("individuals", animalIndividualService.getByProprietarId(proprietarId, pageable));
        result.put("groups", efectivGrupService.getByProprietarId(proprietarId, pageable));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/gospodarie/{gospodarieId}")
    public ResponseEntity<?> getAnimalsByGospodarie(@PathVariable Long gospodarieId, Pageable pageable) {
        Map<String, Object> result = new HashMap<>();
        result.put("individuals", animalIndividualService.getByGospodarieId(gospodarieId, pageable));
        result.put("grupuriCurente", efectivGrupService.getLatestByGospodarieId(gospodarieId));
        result.put("grupuriIstorice", efectivGrupService.getHistoryByGospodarieId(gospodarieId, pageable));
        return ResponseEntity.ok(result);
    }
}

