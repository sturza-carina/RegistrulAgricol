package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.annotation.GdprAudited;
import com.multitenant.model.registru.Gospodarie;
import com.multitenant.service.GospodarieService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
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
}
