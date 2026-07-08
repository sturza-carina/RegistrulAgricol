package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.dto.FactoriMediuDTO;
import com.multitenant.service.FactoriMediuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/factori-mediu")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@TenantRequired
@RequiredArgsConstructor
public class FactoriMediuController {

    private final FactoriMediuService factoriMediuService;

    @GetMapping("/parcela/{parcelaId}")
    public ResponseEntity<Page<FactoriMediuDTO>> getReadingsByParcela(@PathVariable Long parcelaId, Pageable pageable) {
        return ResponseEntity.ok(factoriMediuService.getReadingsByParcela(parcelaId, pageable));
    }

    @PostMapping
    public ResponseEntity<FactoriMediuDTO> createReading(@Valid @RequestBody FactoriMediuDTO dto) {
        return ResponseEntity.ok(factoriMediuService.createReading(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReading(@PathVariable Long id) {
        factoriMediuService.deleteReading(id);
        return ResponseEntity.ok().build();
    }
}
