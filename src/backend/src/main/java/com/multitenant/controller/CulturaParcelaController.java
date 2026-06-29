package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.dto.CulturaParcelaDto;
import com.multitenant.service.CulturaParcelaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/parcele/{parcelaId}/culturi")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@TenantRequired
public class CulturaParcelaController {

    private final CulturaParcelaService culturaParcelaService;

    @GetMapping
    public ResponseEntity<?> getCulturi(@PathVariable Long parcelaId, Pageable pageable) {
        return ResponseEntity.ok(culturaParcelaService.getCulturiByParcelaId(parcelaId, pageable));
    }

    @PostMapping
    public ResponseEntity<?> addCultura(
            @PathVariable Long parcelaId,
            @RequestBody CulturaParcelaDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(culturaParcelaService.addCulturaToParcela(parcelaId, dto));
    }

    @PutMapping("/{culturaId}")
    public ResponseEntity<?> updateCultura(
            @PathVariable Long parcelaId,
            @PathVariable Long culturaId,
            @RequestBody CulturaParcelaDto dto) {
        return ResponseEntity.ok(culturaParcelaService.updateCultura(parcelaId, culturaId, dto));
    }

    @DeleteMapping("/{culturaId}")
    public ResponseEntity<?> deleteCultura(
            @PathVariable Long parcelaId,
            @PathVariable Long culturaId) {
        culturaParcelaService.deleteCultura(parcelaId, culturaId);
        return ResponseEntity.noContent().build();
    }
}