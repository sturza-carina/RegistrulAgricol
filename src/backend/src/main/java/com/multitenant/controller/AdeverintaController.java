package com.multitenant.controller;

import com.multitenant.model.registru.Gospodarie;
import com.multitenant.model.persoana.Persoana;
import com.multitenant.model.persoana.PersoanaFizica;
import com.multitenant.model.core.Uat;
import com.multitenant.repository.GospodarieRepository;
import com.multitenant.repository.PersoanaRepository;
import com.multitenant.repository.UatRepository;
import com.multitenant.service.PdfGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/adeverinte")
public class AdeverintaController {

    private final PdfGeneratorService pdfGeneratorService;
    private final GospodarieRepository gospodarieRepository;
    private final PersoanaRepository persoanaRepository;
    private final UatRepository uatRepository;

    @Autowired
    public AdeverintaController(PdfGeneratorService pdfGeneratorService,
                                GospodarieRepository gospodarieRepository,
                                PersoanaRepository persoanaRepository,
                                UatRepository uatRepository) {
        this.pdfGeneratorService = pdfGeneratorService;
        this.gospodarieRepository = gospodarieRepository;
        this.persoanaRepository = persoanaRepository;
        this.uatRepository = uatRepository;
    }

    @GetMapping("/rol-agricol/{gospodarieId}/persoana/{persoanaId}")
    public ResponseEntity<byte[]> generateAdeverintaRolAgricol(
            @PathVariable Long gospodarieId,
            @PathVariable Long persoanaId) {
        
        Gospodarie gospodarie = gospodarieRepository.findById(gospodarieId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gospodaria nu a fost gasita"));
        
        Persoana persoana = persoanaRepository.findById(persoanaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Persoana nu a fost gasita"));

        // Fetching required data mapping
        Map<String, Object> variables = new HashMap<>();
        
        String judetName = "Exemplu Județ";
        String primarieName = "Exemplu Primărie";
        
        if (gospodarie.getUat() != null) {
            judetName = gospodarie.getUat().getJudet();
            primarieName = gospodarie.getUat().getDenumire();
        } else {
            // Fallback: look up the first UAT registered for this tenant
            List<Uat> tenantUats = uatRepository.findAll();
            if (!tenantUats.isEmpty()) {
                judetName = tenantUats.get(0).getJudet();
                primarieName = tenantUats.get(0).getDenumire();
            }
        }
        
        variables.put("judet", judetName);
        variables.put("primarie", primarieName);
        String numeComplet = "";
        if (persoana instanceof PersoanaFizica pf) {
            numeComplet = (pf.getLastName() != null ? pf.getLastName() : "") + " " + (pf.getFirstName() != null ? pf.getFirstName() : "");
            variables.put("identificare", "CNP");
            variables.put("cnp", pf.getCnp() != null ? pf.getCnp() : "-");
        } else if (persoana instanceof com.multitenant.model.persoana.PersoanaJuridica pj) {
            numeComplet = pj.getCompanyName() != null ? pj.getCompanyName() : "-";
            variables.put("identificare", "CUI");
            variables.put("cnp", pj.getCui() != null ? pj.getCui() : "-");
        }
        variables.put("numeComplet", numeComplet.trim());
        
        String adresaCompleta = "Necunoscut";
        if (persoana.getAdresa() != null) {
            com.multitenant.model.common.Adresa a = persoana.getAdresa();
            adresaCompleta = (a.getCounty() != null ? a.getCounty() : "") + ", " +
                             (a.getLocalitate() != null ? a.getLocalitate() : "") + ", str. " +
                             (a.getStreet() != null ? a.getStreet() : "") + " nr. " +
                             (a.getStreetNumber() != null ? a.getStreetNumber() : "");
        }
        variables.put("domiciliu", adresaCompleta);
        variables.put("codGospodarie", gospodarie.getCodGospodarie());
        variables.put("volum", "1");
        variables.put("pozitie", "1");
        
        // This should be calculated from real data linked to the Gospodarie
        variables.put("suprafataTotala", "0.00");
        variables.put("totalAnimale", "0");

        byte[] pdfBytes = pdfGeneratorService.generatePdfFromHtml("adeverinta-rol-agricol", variables);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "adeverinta-rol-agricol.pdf"); // 'inline' opens in browser tab

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
