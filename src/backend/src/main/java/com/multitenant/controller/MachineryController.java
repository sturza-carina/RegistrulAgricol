package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.dto.MachineryDTO;
import com.multitenant.service.MachineryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/machinery")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@TenantRequired
public class MachineryController {

    private final MachineryService machineryService;

    public MachineryController(MachineryService machineryService) {
        this.machineryService = machineryService;
    }

    @GetMapping
    public ResponseEntity<?> getAllMachinery(Pageable pageable) {
        return ResponseEntity.ok(machineryService.getAll(pageable));
    }

    @GetMapping("/gospodarie/{gospodarieId}")
    public ResponseEntity<?> getMachineryByGospodarie(@PathVariable Long gospodarieId, Pageable pageable) {
        return ResponseEntity.ok(machineryService.getAllByGospodarie(gospodarieId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMachineryById(@PathVariable Long id) {
        return ResponseEntity.ok(machineryService.getById(id));
    }

    @PostMapping
    public ResponseEntity<?> createMachinery(@RequestBody MachineryDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(machineryService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMachinery(@PathVariable Long id, @RequestBody MachineryDTO dto) {
        return ResponseEntity.ok(machineryService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMachinery(@PathVariable Long id) {
        machineryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

