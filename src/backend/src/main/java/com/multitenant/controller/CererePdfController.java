package com.multitenant.controller;

import com.multitenant.model.core.Cetatean;
import com.multitenant.model.core.PublicUat;
import com.multitenant.model.registru.Cerere;
import com.multitenant.model.registru.TipCerere;
import com.multitenant.repository.CerereRepository;
import com.multitenant.repository.PublicUatRepository;
import com.multitenant.service.PdfGeneratorService;
import com.multitenant.config.tenant.TenantContext;
import com.multitenant.model.persoana.Persoana;
import com.multitenant.model.persoana.PersoanaFizica;
import com.multitenant.model.registru.AtestatProducator;
import com.multitenant.repository.AtestatProducatorRepository;
import com.multitenant.repository.PersoanaRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/public/cereri", "/api/admin/cereri"})
public class CererePdfController {

    private final CerereRepository cerereRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final PublicUatRepository publicUatRepository;
    private final PersoanaRepository persoanaRepository;
    private final AtestatProducatorRepository atestatProducatorRepository;

    public CererePdfController(CerereRepository cerereRepository, 
                               PdfGeneratorService pdfGeneratorService, 
                               PublicUatRepository publicUatRepository,
                               PersoanaRepository persoanaRepository,
                               AtestatProducatorRepository atestatProducatorRepository) {
        this.cerereRepository = cerereRepository;
        this.pdfGeneratorService = pdfGeneratorService;
        this.publicUatRepository = publicUatRepository;
        this.persoanaRepository = persoanaRepository;
        this.atestatProducatorRepository = atestatProducatorRepository;
    }

    private String getTemplateName(TipCerere tip) {
        if (tip == null) return "cerere_generica";
        switch (tip) {
            case ELIBERARE_ATESTAT_PRODUCATOR:
                return "atestat_producator";
            case ELIBERARE_CARNET_COMERCIALIZARE:
                return "carnet_comercializare";
            case ADEVERINTA_ROL:
                return "adeverinta_rol";
            default:
                return "cerere_generica";
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadCererePdf(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("FUNC_PRIMARIE"));
        
        Cerere cerere = null;
        boolean contextSetManually = false;

        try {
            if (isAdmin) {
                // Admin context already sets tenant ID correctly
                cerere = cerereRepository.findById(id).orElse(null);
            } else if (auth != null && auth.getPrincipal() instanceof Cetatean) {
                // Citizen context needs to search across tenants
                List<String> distinctTenants = publicUatRepository.findAll().stream()
                        .map(PublicUat::getTenantId)
                        .filter(t -> t != null && !t.trim().isEmpty())
                        .distinct()
                        .collect(Collectors.toList());

                for (String tenantId : distinctTenants) {
                    TenantContext.setCurrentTenant(tenantId);
                    try {
                        Cerere found = cerereRepository.findById(id).orElse(null);
                        if (found != null) {
                            cerere = found;
                            contextSetManually = true;
                            break;
                        }
                    } catch (Exception e) {
                        // Ignore schema missing errors for certain tenants
                    }
                    if (cerere == null) {
                        TenantContext.clear();
                    }
                }
            }

            if (cerere == null) {
                throw new RuntimeException("Cerere not found");
            }

            boolean isOwner = false;
            if (auth != null && auth.getPrincipal() instanceof Cetatean) {
                Cetatean cetatean = (Cetatean) auth.getPrincipal();
                if (cerere.getUserId() != null && cerere.getUserId().equals(cetatean.getId())) {
                    isOwner = true;
                }
            }
            
            if (!isAdmin && !isOwner) {
                return ResponseEntity.status(403).build();
            }

            if (!"ACCEPTED".equals(cerere.getStatus().name())) {
                throw new RuntimeException("Cannot generate PDF for unapproved cerere");
            }

            Map<String, Object> variables = new HashMap<>();
            variables.put("cerere", cerere);
            
            String templateName = getTemplateName(cerere.getTipCerere());

            if (cerere.getTipCerere() == TipCerere.ELIBERARE_ATESTAT_PRODUCATOR && cerere.getCnpCui() != null) {
                Persoana persoana = persoanaRepository.findByCnpClar(cerere.getCnpCui()).orElse(null);
                if (persoana instanceof PersoanaFizica) {
                    PersoanaFizica pf = (PersoanaFizica) persoana;
                    variables.put("persoana", pf);
                    // Force lazy initialization of identity documents
                    if (pf.getIdentityDocuments() != null && !pf.getIdentityDocuments().isEmpty()) {
                        variables.put("actIdentitate", pf.getIdentityDocuments().get(0));
                    }
                    List<AtestatProducator> atestate = atestatProducatorRepository.findByPersoanaId(pf.getId());
                    if (!atestate.isEmpty()) {
                        variables.put("atestat", atestate.get(atestate.size() - 1));
                    }
                }
            }

            byte[] pdfBytes = pdfGeneratorService.generatePdfFromHtml(templateName, variables);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "document_" + cerere.getCodCerere() + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } finally {
            if (contextSetManually) {
                TenantContext.clear();
            }
        }
    }
}
