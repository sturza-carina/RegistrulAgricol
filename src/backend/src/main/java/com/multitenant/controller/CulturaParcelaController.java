package com.multitenant.controller;

import com.multitenant.dto.CulturaParcelaDto;
import com.multitenant.service.CulturaParcelaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/parcele/{parcelaId}/culturi")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
public class CulturaParcelaController {

    private final CulturaParcelaService culturaParcelaService;

    @GetMapping
    public ResponseEntity<List<CulturaParcelaDto>> getCulturi(@PathVariable Long parcelaId) {
        return ResponseEntity.ok(culturaParcelaService.getCulturiByParcelaId(parcelaId));
    }

    @PostMapping
    public ResponseEntity<CulturaParcelaDto> addCultura(
            @PathVariable Long parcelaId,
            @RequestBody CulturaParcelaDto dto) {
        CulturaParcelaDto created = culturaParcelaService.addCulturaToParcela(parcelaId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{culturaId}")
    public ResponseEntity<CulturaParcelaDto> updateCultura(
            @PathVariable Long parcelaId,
            @PathVariable Long culturaId,
            @RequestBody CulturaParcelaDto dto) {
        return ResponseEntity.ok(culturaParcelaService.updateCultura(parcelaId, culturaId, dto));
    }

    @DeleteMapping("/{culturaId}")
    public ResponseEntity<Void> deleteCultura(
            @PathVariable Long parcelaId,
            @PathVariable Long culturaId) {
        culturaParcelaService.deleteCultura(parcelaId, culturaId);
        return ResponseEntity.noContent().build();
    }
}
