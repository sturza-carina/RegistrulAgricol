package com.multitenant.controller;

import com.multitenant.dto.PadureDTO;
import com.multitenant.service.PadureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.multitenant.annotation.TenantRequired;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/parcele/{parcelaId}/paduri")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@TenantRequired
public class PadureController {

    private final PadureService padureService;

    @GetMapping
    public ResponseEntity<Page<PadureDTO>> getAll(
            @PathVariable Long parcelaId,
            Pageable pageable) {
        return ResponseEntity.ok(padureService.getPaduriByParcela(parcelaId, pageable));
    }

    @PostMapping
    public ResponseEntity<PadureDTO> create(
            @PathVariable Long parcelaId,
            @Valid @RequestBody PadureDTO dto) {
        return ResponseEntity.ok(padureService.create(parcelaId, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PadureDTO> update(
            @PathVariable Long parcelaId,
            @PathVariable Long id,
            @Valid @RequestBody PadureDTO dto) {
        return ResponseEntity.ok(padureService.update(parcelaId, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long parcelaId,
            @PathVariable Long id) {
        padureService.delete(parcelaId, id);
        return ResponseEntity.noContent().build();
    }
}
