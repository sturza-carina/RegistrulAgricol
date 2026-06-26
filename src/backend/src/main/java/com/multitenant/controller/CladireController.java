package com.multitenant.controller;

import com.multitenant.dto.CladireDTO;
import com.multitenant.service.CladireService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/gospodarii/{gospodarieId}/cladiri")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@SuppressWarnings("null")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
public class CladireController {

    private final CladireService cladireService;

    public CladireController(CladireService cladireService) {
        this.cladireService = cladireService;
    }

    @GetMapping
    public ResponseEntity<List<CladireDTO>> getCladiri(@PathVariable Long gospodarieId) {
        return ResponseEntity.ok(cladireService.getCladiriByGospodarieId(gospodarieId));
    }

    @PostMapping
    public ResponseEntity<CladireDTO> createCladire(@PathVariable Long gospodarieId, @RequestBody CladireDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cladireService.createCladire(gospodarieId, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CladireDTO> updateCladire(@PathVariable Long gospodarieId, @PathVariable Long id, @RequestBody CladireDTO dto) {
        return ResponseEntity.ok(cladireService.updateCladire(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCladire(@PathVariable Long gospodarieId, @PathVariable Long id) {
        cladireService.deleteCladire(id);
        return ResponseEntity.noContent().build();
    }
}
