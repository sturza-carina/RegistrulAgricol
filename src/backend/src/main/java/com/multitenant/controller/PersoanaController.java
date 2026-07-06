package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.annotation.GdprAudited;
import com.multitenant.model.persoana.Persoana;
import com.multitenant.service.PersoanaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping({"/api/persons", "/api/persoane"})
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@TenantRequired
public class PersoanaController {

    private final PersoanaService persoanaService;

    public PersoanaController(PersoanaService persoanaService) {
        this.persoanaService = persoanaService;
    }

    @PostMapping
    @GdprAudited(entity = "Persoana")
    public ResponseEntity<?> createPerson(@Valid @RequestBody Persoana persoana) {
        return ResponseEntity.ok(persoanaService.createPerson(persoana));
    }

    @GetMapping
    @GdprAudited(entity = "Persoana")
    public ResponseEntity<?> getAllPersons(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            Pageable pageable) {
        return ResponseEntity.ok(persoanaService.getAllPersons(search, type, pageable));
    }

    @GetMapping("/gospodarie/{gospodarieId}")
    @GdprAudited(entity = "Persoana")
    public ResponseEntity<?> getPersonsByGospodarieId(@PathVariable Long gospodarieId, Pageable pageable) {
        return ResponseEntity.ok(persoanaService.getPersonsByGospodarieId(gospodarieId, pageable));
    }

    @GetMapping("/{id}")
    @GdprAudited(entity = "Persoana")
    public ResponseEntity<?> getPersonById(@PathVariable Long id) {
        return ResponseEntity.ok(persoanaService.getPersonById(id));
    }

    @PutMapping("/{id}")
    @GdprAudited(entity = "Persoana")
    public ResponseEntity<?> updatePerson(@PathVariable Long id, @Valid @RequestBody Persoana persoana) {
        return ResponseEntity.ok(persoanaService.updatePerson(id, persoana));
    }

    @DeleteMapping("/{id}")
    @GdprAudited(entity = "Persoana")
    public ResponseEntity<?> deletePerson(@PathVariable Long id) {
        persoanaService.deletePerson(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{persoanaId}/gospodarii/{gospodarieId}")
    @GdprAudited(entity = "Persoana")
    public ResponseEntity<?> addPersonToGospodarie(@PathVariable Long persoanaId, @PathVariable Long gospodarieId) {
        persoanaService.addPersonToGospodarie(persoanaId, gospodarieId);
        return ResponseEntity.ok().build();
    }
}
