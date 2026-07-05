package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.dto.PasuneFaneataDTO;
import com.multitenant.model.registru.TipFolosintaPasune;
import com.multitenant.service.PasuneFaneataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parcele/{parcelaId}/pasuni-fanete")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@TenantRequired
public class PasuneFaneataController {

    private final PasuneFaneataService pasuneFaneataService;

    @GetMapping
    public ResponseEntity<?> getAll(
            @PathVariable Long parcelaId,
            @RequestParam(required = false) TipFolosintaPasune tip,
            Pageable pageable) {
        Page<PasuneFaneataDTO> result = tip != null
                ? pasuneFaneataService.getByParcelaIdAndTip(parcelaId, tip, pageable)
                : pasuneFaneataService.getByParcelaId(parcelaId, pageable);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> add(
            @PathVariable Long parcelaId,
            @Valid @RequestBody PasuneFaneataDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pasuneFaneataService.addToParcela(parcelaId, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long parcelaId,
            @PathVariable Long id,
            @Valid @RequestBody PasuneFaneataDTO dto) {
        return ResponseEntity.ok(pasuneFaneataService.update(parcelaId, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Long parcelaId,
            @PathVariable Long id) {
        pasuneFaneataService.delete(parcelaId, id);
        return ResponseEntity.noContent().build();
    }
}
