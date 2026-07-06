package com.multitenant.controller;

import com.multitenant.dto.CerereDTO;
import com.multitenant.model.registru.StatusCerere;
import com.multitenant.service.CerereService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/cereri")
public class AdminCerereController {

    private final CerereService cerereService;

    public AdminCerereController(CerereService cerereService) {
        this.cerereService = cerereService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<CerereDTO>> getAllCereri() {
        // TenantContext is already set by TenantFilter based on authenticated user's tenantId
        return ResponseEntity.ok(cerereService.getAllForTenant());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CerereDTO> updateStatus(@PathVariable Long id, @RequestParam StatusCerere status) {
        return ResponseEntity.ok(cerereService.updateStatus(id, status));
    }
}
