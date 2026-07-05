package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.dto.PomDTO;
import com.multitenant.model.registru.TipInregistrarePom;
import com.multitenant.service.PomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parcele/{parcelaId}/pomi")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@TenantRequired
public class PomController {

    private final PomService pomService;

    @GetMapping
    public ResponseEntity<?> getPomi(
            @PathVariable Long parcelaId,
            @RequestParam(required = false) TipInregistrarePom tip,
            Pageable pageable) {
        Page<PomDTO> result = tip != null
                ? pomService.getPomiByParcelaIdAndTip(parcelaId, tip, pageable)
                : pomService.getPomiByParcelaId(parcelaId, pageable);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> addPom(
            @PathVariable Long parcelaId,
            @Valid @RequestBody PomDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pomService.addPomToParcela(parcelaId, dto));
    }

    @PutMapping("/{pomId}")
    public ResponseEntity<?> updatePom(
            @PathVariable Long parcelaId,
            @PathVariable Long pomId,
            @Valid @RequestBody PomDTO dto) {
        return ResponseEntity.ok(pomService.updatePom(parcelaId, pomId, dto));
    }

    @DeleteMapping("/{pomId}")
    public ResponseEntity<?> deletePom(
            @PathVariable Long parcelaId,
            @PathVariable Long pomId) {
        pomService.deletePom(parcelaId, pomId);
        return ResponseEntity.noContent().build();
    }
}