package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.dto.SursaApaDTO;
import com.multitenant.service.SursaApaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/parcele/{parcelaId}/surse-apa")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@TenantRequired
public class SursaApaController {

    private final SursaApaService sursaApaService;

    @GetMapping
    public ResponseEntity<?> getSurse(@PathVariable Long parcelaId, Pageable pageable) {
        return ResponseEntity.ok(sursaApaService.getSurseByParcelaId(parcelaId, pageable));
    }

    @PostMapping
    public ResponseEntity<?> addSursa(
            @PathVariable Long parcelaId,
            @RequestBody SursaApaDTO dto) {
        SursaApaDTO created = sursaApaService.addSursaToParcela(parcelaId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{sursaId}")
    public ResponseEntity<?> updateSursa(
            @PathVariable Long parcelaId,
            @PathVariable Long sursaId,
            @RequestBody SursaApaDTO dto) {
        return ResponseEntity.ok(sursaApaService.updateSursa(parcelaId, sursaId, dto));
    }

    @DeleteMapping("/{sursaId}")
    public ResponseEntity<?> deleteSursa(
            @PathVariable Long parcelaId,
            @PathVariable Long sursaId) {
        sursaApaService.deleteSursa(parcelaId, sursaId);
        return ResponseEntity.ok().build();
    }
}

