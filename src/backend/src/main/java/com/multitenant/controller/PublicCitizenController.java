package com.multitenant.controller;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.model.core.PublicUat;
import com.multitenant.model.persoana.Persoana;
import com.multitenant.model.persoana.PersoanaFizica;
import com.multitenant.repository.PersoanaRepository;
import com.multitenant.repository.PublicUatRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/public/cetateni")
public class PublicCitizenController {

    private final PersoanaRepository persoanaRepository;
    private final PublicUatRepository publicUatRepository;
    private final com.multitenant.repository.core.CetateanRepository cetateanRepository;
    private final com.multitenant.service.GospodarieService gospodarieService;

    public PublicCitizenController(PersoanaRepository persoanaRepository, PublicUatRepository publicUatRepository,
            com.multitenant.repository.core.CetateanRepository cetateanRepository,
            com.multitenant.service.GospodarieService gospodarieService) {
        this.persoanaRepository = persoanaRepository;
        this.publicUatRepository = publicUatRepository;
        this.cetateanRepository = cetateanRepository;
        this.gospodarieService = gospodarieService;
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyCitizen(
            @RequestParam String cnp,
            @RequestParam String initials,
            @RequestParam Long uatId) {

        PublicUat pUat = publicUatRepository.findById(uatId)
                .orElseThrow(() -> new RuntimeException("UAT not found"));

        TenantContext.setCurrentTenant(pUat.getTenantId());
        try {
            Optional<Persoana> optPerson = persoanaRepository.findByCnpClar(cnp);
            if (optPerson.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message",
                        "Datele de identificare sunt incorecte. Nu exista nicio persoana cu acest CNP."));
            }

            Persoana person = optPerson.get();
            if (!(person instanceof PersoanaFizica)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Datele de identificare sunt incorecte."));
            }

            PersoanaFizica physicalPerson = (PersoanaFizica) person;
            if (!matchInitials(physicalPerson.getFirstName(), physicalPerson.getLastName(), initials)) {
                return ResponseEntity.badRequest().body(Map.of("message",
                        "Datele de identificare sunt incorecte. Initialele numelui nu se potrivesc."));
            }

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "firstName", physicalPerson.getFirstName(),
                    "lastName", physicalPerson.getLastName()));
        } finally {
            TenantContext.clear();
        }
    }

    private boolean matchInitials(String firstName, String lastName, String inputInitials) {
        if (inputInitials == null || inputInitials.trim().isEmpty()) {
            return false;
        }

        String cleanInput = inputInitials.replaceAll("[^a-zA-Z]", "").toUpperCase();
        String fFirst = (firstName != null && !firstName.trim().isEmpty())
                ? firstName.trim().substring(0, 1).toUpperCase()
                : "";
        String lFirst = (lastName != null && !lastName.trim().isEmpty()) ? lastName.trim().substring(0, 1).toUpperCase()
                : "";

        String cleanCalculated1 = fFirst + lFirst;
        String cleanCalculated2 = lFirst + fFirst;

        return cleanInput.equals(cleanCalculated1) || cleanInput.equals(cleanCalculated2);
    }

    @GetMapping("/me/gospodarii")
    public ResponseEntity<java.util.List<com.multitenant.dto.GospodarieDTO>> getMyGospodarii() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof com.multitenant.model.core.Cetatean)) {
            return ResponseEntity.status(401).build();
        }
        com.multitenant.model.core.Cetatean cetatean = (com.multitenant.model.core.Cetatean) auth.getPrincipal();

        Optional<com.multitenant.model.core.Cetatean> dbCetatean = cetateanRepository.findById(cetatean.getId());
        if (dbCetatean.isEmpty())
            return ResponseEntity.status(401).build();
        String cnp = dbCetatean.get().getCnp();

        java.util.List<com.multitenant.dto.GospodarieDTO> allGospodarii = new java.util.ArrayList<>();
        java.util.List<PublicUat> allUats = publicUatRepository.findAll();
        java.util.List<String> distinctTenants = allUats.stream()
                .map(PublicUat::getTenantId)
                .filter(t -> t != null && !t.trim().isEmpty())
                .distinct()
                .collect(java.util.stream.Collectors.toList());

        for (String tenantId : distinctTenants) {
            com.multitenant.config.tenant.TenantContext.setCurrentTenant(tenantId);
            try {
                java.util.List<com.multitenant.dto.GospodarieDTO> tenantGospodarii = gospodarieService
                        .getGospodariiForPersoana(cnp);
                if (tenantGospodarii != null) {
                    allGospodarii.addAll(tenantGospodarii);
                }
            } catch (Exception ex) {
            } finally {
                com.multitenant.config.tenant.TenantContext.clear();
            }
        }
        return ResponseEntity.ok(allGospodarii);
    }
}
