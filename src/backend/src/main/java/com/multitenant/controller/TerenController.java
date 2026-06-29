package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.model.registru.Teren;
import com.multitenant.service.TerenService;
import com.multitenant.dto.TerenWithParcelaDTO;
import com.multitenant.dto.TerenCreateDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import java.util.List;

@RestController
@RequestMapping("/api/terenuri")
@SuppressWarnings("null")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@TenantRequired
public class TerenController {

    private final TerenService terenService;

    public TerenController(TerenService terenService) {
        this.terenService = terenService;
    }

    @GetMapping
    public ResponseEntity<?> getAllTerenuri(Pageable pageable) {
        return ResponseEntity.ok(terenService.getAllTerenuri(pageable));
    }

    @GetMapping("/gospodarie/{gospodarieId}")
    public ResponseEntity<?> getTerenByGospodarieId(@PathVariable Long gospodarieId, Pageable pageable) {
        Page<Teren> terenuri = terenService.getTerenByGospodarieId(gospodarieId, pageable);
        return ResponseEntity.ok(terenuri);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTerenById(@PathVariable Long id) {
        return ResponseEntity.ok(terenService.getTerenById(id));
    }

    @PostMapping
    public ResponseEntity<?> createTeren(@Valid @RequestBody TerenCreateDTO dto) {
        try {
            return ResponseEntity.ok(terenService.createTerenFromDTO(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Eroare la salvare teren: " + e.getMessage());
        }
    }

    @PostMapping("/with-parcela")
    public ResponseEntity<?> createTerenWithParcela(@Valid @RequestBody TerenWithParcelaDTO dto) {
        return ResponseEntity.ok(terenService.createTerenWithParcela(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeren(@PathVariable Long id, @Valid @RequestBody Teren teren) {
        return ResponseEntity.ok(terenService.updateTeren(id, teren));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeren(@PathVariable Long id) {
        terenService.deleteTeren(id);
        return ResponseEntity.ok().build();
    }
}

