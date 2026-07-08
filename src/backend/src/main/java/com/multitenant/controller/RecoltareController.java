package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.dto.RecoltareDTO;
import com.multitenant.dto.CentralizatorRecoltareDto;
import com.multitenant.service.RecoltareService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/recoltari")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@TenantRequired
@RequiredArgsConstructor
public class RecoltareController {

    private final RecoltareService recoltareService;

    @GetMapping("/parcela/{parcelaId}")
    public ResponseEntity<Page<RecoltareDTO>> getRecoltariByParcela(@PathVariable Long parcelaId, Pageable pageable) {
        return ResponseEntity.ok(recoltareService.getRecoltariByParcela(parcelaId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecoltareDTO> getRecoltareById(@PathVariable Long id) {
        return ResponseEntity.ok(recoltareService.getRecoltareById(id));
    }

    @PostMapping
    public ResponseEntity<RecoltareDTO> createRecoltare(@Valid @RequestBody RecoltareDTO dto) {
        return ResponseEntity.ok(recoltareService.createRecoltare(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecoltareDTO> updateRecoltare(@PathVariable Long id, @Valid @RequestBody RecoltareDTO dto) {
        return ResponseEntity.ok(recoltareService.updateRecoltare(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecoltare(@PathVariable Long id) {
        recoltareService.deleteRecoltare(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/centralizator")
    public ResponseEntity<List<CentralizatorRecoltareDto>> getCentralizator(@RequestParam(required = false) Integer anAgricol) {
        return ResponseEntity.ok(recoltareService.getCentralizatorRecoltari(anAgricol));
    }
}
