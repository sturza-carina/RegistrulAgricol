package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.annotation.GdprAudited;
import com.multitenant.model.registru.Gospodarie;
import com.multitenant.service.GospodarieService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.multitenant.dto.IstoricMembruDTO;
import java.util.List;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/gospodarii")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@TenantRequired
public class GospodarieController {

    private final GospodarieService gospodarieService;

    public GospodarieController(GospodarieService gospodarieService) {
        this.gospodarieService = gospodarieService;
    }

    @GetMapping("/{id}")
    @GdprAudited(entity = "Gospodarie")
    public ResponseEntity<?> getGospodarieById(@PathVariable Long id) {
        return ResponseEntity.ok(gospodarieService.getGospodarieById(id));
    }

    @PostMapping
    @GdprAudited(entity = "Gospodarie")
    public ResponseEntity<?> createGospodarie(@RequestBody Gospodarie gospodarie) {
        return ResponseEntity.ok(gospodarieService.createGospodarie(gospodarie));
    }

    @PutMapping("/{id}")
    @GdprAudited(entity = "Gospodarie")
    public ResponseEntity<?> updateGospodarie(@PathVariable Long id, @RequestBody Gospodarie gospodarie) {
        return ResponseEntity.ok(gospodarieService.updateGospodarie(id, gospodarie));
    }

    @DeleteMapping("/{id}")
    @GdprAudited(entity = "Gospodarie")
    public ResponseEntity<?> deleteGospodarie(@PathVariable Long id) {
        gospodarieService.deleteGospodarie(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @GdprAudited(entity = "Gospodarie")
    public ResponseEntity<?> getAllGospodarii(
            @RequestParam(required = false) String uatCode, Pageable pageable) {
        return ResponseEntity.ok(gospodarieService.getAllGospodarii(uatCode, pageable));
    }

    @PutMapping("/{gospodarieId}/cap-gospodarie/{persoanaId}")
    @GdprAudited(entity = "Gospodarie")
    public ResponseEntity<?> seteazaCapGospodariePath(@PathVariable Long gospodarieId, @PathVariable Long persoanaId) {
        gospodarieService.seteazaCapGospodarie(gospodarieId, persoanaId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{gospodarieId}/cap-gospodarie")
    @GdprAudited(entity = "Gospodarie")
    public ResponseEntity<?> seteazaCapGospodarieQuery(@PathVariable Long gospodarieId, @RequestParam(required = false) Long persoanaId) {
        gospodarieService.seteazaCapGospodarie(gospodarieId, persoanaId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{gospodarieId}/istoric-membri")
    @GdprAudited(entity = "Gospodarie")
    public ResponseEntity<List<IstoricMembruDTO>> getIstoricMembri(@PathVariable Long gospodarieId) {
        return ResponseEntity.ok(gospodarieService.getIstoricMembri(gospodarieId));
    }

    @PostMapping("/{gospodarieId}/istoric-membri")
    @GdprAudited(entity = "Gospodarie")
    public ResponseEntity<?> adaugaEvenimentIstoric(@PathVariable Long gospodarieId, @RequestBody IstoricMembruDTO dto) {
        gospodarieService.adaugaEvenimentIstoric(gospodarieId, dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{gospodarieId}/istoric-membri/{evenimentId}")
    @GdprAudited(entity = "Gospodarie")
    public ResponseEntity<?> updateEvenimentIstoric(
            @PathVariable Long gospodarieId,
            @PathVariable Long evenimentId,
            @RequestBody IstoricMembruDTO dto) {
        gospodarieService.updateEvenimentIstoric(gospodarieId, evenimentId, dto);
        return ResponseEntity.ok().build();
    }
}
