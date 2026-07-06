package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.dto.FertilizareDTO;
import com.multitenant.service.FertilizareService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fertilizari")
@RequiredArgsConstructor
@TenantRequired
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
public class FertilizareController {

    private final FertilizareService fertilizareService;

    @GetMapping
    public ResponseEntity<Page<FertilizareDTO>> getFertilizari(
            @RequestParam(value = "parcelaId", required = false) Long parcelaId,
            Pageable pageable) {
        return ResponseEntity.ok(fertilizareService.getFertilizari(parcelaId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FertilizareDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(fertilizareService.getFertilizareById(id));
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody FertilizareDTO dto,
            @RequestParam(value = "confirmInterdictie", required = false, defaultValue = "false") boolean confirmInterdictie) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(fertilizareService.createFertilizare(dto, confirmInterdictie));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Eroare la înregistrarea fertilizării: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody FertilizareDTO dto,
            @RequestParam(value = "confirmInterdictie", required = false, defaultValue = "false") boolean confirmInterdictie) {
        try {
            return ResponseEntity.ok(fertilizareService.updateFertilizare(id, dto, confirmInterdictie));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Eroare la modificarea fertilizării: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fertilizareService.deleteFertilizare(id);
        return ResponseEntity.noContent().build();
    }
}
