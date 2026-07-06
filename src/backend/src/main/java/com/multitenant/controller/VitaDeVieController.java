package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.dto.VitaDeVieDTO;
import com.multitenant.model.registru.TipInregistrareVita;
import com.multitenant.service.VitaDeVieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parcele/{parcelaId}/vita-de-vie")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@TenantRequired
public class VitaDeVieController {

    private final VitaDeVieService vitaDeVieService;

    @GetMapping
    public ResponseEntity<?> getVitaDeVie(
            @PathVariable Long parcelaId,
            @RequestParam(required = false) TipInregistrareVita tip,
            Pageable pageable) {
        Page<VitaDeVieDTO> result = tip != null
                ? vitaDeVieService.getVitaDeVieByParcelaIdAndTip(parcelaId, tip, pageable)
                : vitaDeVieService.getVitaDeVieByParcelaId(parcelaId, pageable);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> addVita(
            @PathVariable Long parcelaId,
            @Valid @RequestBody VitaDeVieDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vitaDeVieService.addVitaToParcela(parcelaId, dto));
    }

    @PutMapping("/{vitaId}")
    public ResponseEntity<?> updateVita(
            @PathVariable Long parcelaId,
            @PathVariable Long vitaId,
            @Valid @RequestBody VitaDeVieDTO dto) {
        return ResponseEntity.ok(vitaDeVieService.updateVita(parcelaId, vitaId, dto));
    }

    @DeleteMapping("/{vitaId}")
    public ResponseEntity<?> deleteVita(
            @PathVariable Long parcelaId,
            @PathVariable Long vitaId) {
        vitaDeVieService.deleteVita(parcelaId, vitaId);
        return ResponseEntity.noContent().build();
    }
}
