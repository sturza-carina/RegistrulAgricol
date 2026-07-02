package com.multitenant.controller;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.dto.CerereDTO;
import com.multitenant.service.CerereService;
import com.multitenant.model.core.PublicUat;
import com.multitenant.repository.PublicUatRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/cereri")
public class CerereController {

    private final CerereService cerereService;
    private final PublicUatRepository publicUatRepository;

    public CerereController(CerereService cerereService, PublicUatRepository publicUatRepository) {
        this.cerereService = cerereService;
        this.publicUatRepository = publicUatRepository;
    }

    @PostMapping
    public ResponseEntity<CerereDTO> submitCerere(@RequestBody CerereDTO cerereDTO) {
        // Find tenant by UAT to switch context
        PublicUat pUat = publicUatRepository.findById(cerereDTO.getUatId()).orElseThrow(() -> new RuntimeException("UAT not found"));
        TenantContext.setCurrentTenant(pUat.getTenantId());
        try {
            CerereDTO created = cerereService.createCerere(cerereDTO);
            return ResponseEntity.ok(created);
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/{codCerere}")
    public ResponseEntity<CerereDTO> checkStatus(@PathVariable String codCerere, @RequestParam Long uatId) {
        PublicUat pUat = publicUatRepository.findById(uatId).orElseThrow(() -> new RuntimeException("UAT not found"));
        TenantContext.setCurrentTenant(pUat.getTenantId());
        try {
            CerereDTO cerere = cerereService.getByCodCerere(codCerere);
            if (cerere == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(cerere);
        } finally {
            TenantContext.clear();
        }
    }
}
