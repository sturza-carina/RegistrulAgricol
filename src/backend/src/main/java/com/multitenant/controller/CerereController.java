package com.multitenant.controller;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.dto.CerereDTO;
import com.multitenant.service.CerereService;
import com.multitenant.model.core.PublicUat;
import com.multitenant.repository.PublicUatRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.multitenant.repository.CerereRepository;
import com.multitenant.security.UserDetailsImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;

@RestController
@RequestMapping("/api/public/cereri")
public class CerereController {

    private final CerereService cerereService;
    private final PublicUatRepository publicUatRepository;
    private final CerereRepository cerereRepository;
    private final ModelMapper modelMapper;

    public CerereController(CerereService cerereService, PublicUatRepository publicUatRepository, CerereRepository cerereRepository, ModelMapper modelMapper) {
        this.cerereService = cerereService;
        this.publicUatRepository = publicUatRepository;
        this.cerereRepository = cerereRepository;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    public org.springframework.http.ResponseEntity<CerereDTO> submitCerere(@RequestBody CerereDTO cerereDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof com.multitenant.model.core.Cetatean) {
            com.multitenant.model.core.Cetatean cetatean = (com.multitenant.model.core.Cetatean) auth.getPrincipal();
            cerereDTO.setUserId(cetatean.getId());
        }
        
        // Find tenant by UAT to switch context
        PublicUat pUat = publicUatRepository.findById(cerereDTO.getUatId()).orElseThrow(() -> new RuntimeException("UAT not found"));
        TenantContext.setCurrentTenant(pUat.getTenantId());
        try {
            CerereDTO created = cerereService.createCerere(cerereDTO);
            return org.springframework.http.ResponseEntity.ok(created);
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/{codCerere}")
    public org.springframework.http.ResponseEntity<CerereDTO> checkStatus(@PathVariable String codCerere, @RequestParam Long uatId) {
        PublicUat pUat = publicUatRepository.findById(uatId).orElseThrow(() -> new RuntimeException("UAT not found"));
        TenantContext.setCurrentTenant(pUat.getTenantId());
        try {
            CerereDTO cerere = cerereService.getByCodCerere(codCerere);
            if (cerere == null) {
                return org.springframework.http.ResponseEntity.notFound().build();
            }
            return org.springframework.http.ResponseEntity.ok(cerere);
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/my-cereri")
    public org.springframework.http.ResponseEntity<List<CerereDTO>> getMyCereri() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("[DEBUG] getMyCereri - auth: " + auth);
        if (auth != null) {
            System.out.println("[DEBUG] getMyCereri - principal class: " + (auth.getPrincipal() != null ? auth.getPrincipal().getClass().getName() : "null"));
            System.out.println("[DEBUG] getMyCereri - isAuthenticated: " + auth.isAuthenticated());
        }
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof com.multitenant.model.core.Cetatean)) {
            System.out.println("[DEBUG] getMyCereri - returning 401!");
            return org.springframework.http.ResponseEntity.status(401).build();
        }

        com.multitenant.model.core.Cetatean cetatean = (com.multitenant.model.core.Cetatean) auth.getPrincipal();
        Long cetateanId = cetatean.getId();
        List<CerereDTO> allCereri = new ArrayList<>();
        List<PublicUat> allUats = publicUatRepository.findAll();
        
        List<String> distinctTenants = allUats.stream()
                .map(PublicUat::getTenantId)
                .filter(t -> t != null && !t.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());

        for (String tenantId : distinctTenants) {
            TenantContext.setCurrentTenant(tenantId);
            try {
                List<CerereDTO> tenantCereri = cerereRepository.findByUserId(cetateanId).stream()
                        .map(c -> modelMapper.map(c, CerereDTO.class))
                        .collect(Collectors.toList());
                allCereri.addAll(tenantCereri);
            } finally {
                TenantContext.clear();
            }
        }
        
        try {
            System.out.println("Returning cereri payload: " + new com.fasterxml.jackson.databind.ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()).writeValueAsString(allCereri));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return org.springframework.http.ResponseEntity.ok(allCereri);
    }

    @GetMapping("/by-cnp/{cnp}")
    public org.springframework.http.ResponseEntity<java.util.List<CerereDTO>> getByCnp(@PathVariable String cnp, @RequestParam Long uatId) {
        PublicUat pUat = publicUatRepository.findById(uatId).orElseThrow(() -> new RuntimeException("UAT not found"));
        TenantContext.setCurrentTenant(pUat.getTenantId());
        try {
            java.util.List<CerereDTO> cereri = cerereService.getByCnp(cnp);
            return org.springframework.http.ResponseEntity.ok(cereri);
        } finally {
            TenantContext.clear();
        }
    }
}
