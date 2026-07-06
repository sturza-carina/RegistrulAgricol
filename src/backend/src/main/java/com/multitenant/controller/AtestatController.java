package com.multitenant.controller;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.dto.AtestatStatusDTO;
import com.multitenant.model.core.Cetatean;
import com.multitenant.model.core.PublicUat;
import com.multitenant.model.registru.AtestatProducator;
import com.multitenant.model.registru.CarnetComercializare;
import com.multitenant.model.registru.Cerere;
import com.multitenant.model.registru.TipCerere;
import com.multitenant.repository.AtestatProducatorRepository;
import com.multitenant.repository.CarnetComercializareRepository;
import com.multitenant.repository.CerereRepository;
import com.multitenant.repository.PublicUatRepository;
import com.multitenant.repository.core.CetateanRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.multitenant.util.CryptoUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/atestate")
public class AtestatController {

    private final AtestatProducatorRepository atestatProducatorRepository;
    private final CarnetComercializareRepository carnetComercializareRepository;
    private final PublicUatRepository publicUatRepository;
    private final CerereRepository cerereRepository;
    private final CetateanRepository cetateanRepository;

    public AtestatController(AtestatProducatorRepository atestatProducatorRepository, 
                             CarnetComercializareRepository carnetComercializareRepository,
                             PublicUatRepository publicUatRepository,
                             CerereRepository cerereRepository,
                             CetateanRepository cetateanRepository) {
        this.atestatProducatorRepository = atestatProducatorRepository;
        this.carnetComercializareRepository = carnetComercializareRepository;
        this.publicUatRepository = publicUatRepository;
        this.cerereRepository = cerereRepository;
        this.cetateanRepository = cetateanRepository;
    }

    @GetMapping("/my-status")
    public org.springframework.http.ResponseEntity<AtestatStatusDTO> getMyStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof Cetatean)) {
            return org.springframework.http.ResponseEntity.status(401).build();
        }

        Cetatean sessionCetatean = (Cetatean) auth.getPrincipal();
        Cetatean cetatean = cetateanRepository.findById(sessionCetatean.getId()).orElse(sessionCetatean);
        
        String cnp = cetatean.getCnp();
        
        AtestatStatusDTO status = new AtestatStatusDTO();
        // Use global flags!
        status.setAreAtestat(cetatean.isAreAtestatProducator());
        status.setAreCarnet(cetatean.isAreCarnetComercializare());

        List<PublicUat> allUats = publicUatRepository.findAll();
        List<String> distinctTenants = allUats.stream()
                .map(PublicUat::getTenantId)
                .filter(t -> t != null && !t.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());

        String cnpHash = CryptoUtils.hashSha256(cnp);
        
        Cerere latestAtestatCerere = null;
        Cerere latestCarnetCerere = null;

        for (String tenantId : distinctTenants) {
            TenantContext.setCurrentTenant(tenantId);
            try {
                // Find latest atestat details
                List<AtestatProducator> atestate = atestatProducatorRepository.findByCnpHash(cnpHash);
                if (!atestate.isEmpty()) {
                    AtestatProducator latestAtestat = atestate.get(atestate.size() - 1);
                    status.setNumarAtestat(latestAtestat.getSeria() + " " + latestAtestat.getNumarAtestat());
                    status.setAtestatValabilPanaLa(latestAtestat.getDataEliberare().plusMonths(latestAtestat.getValabilitateLuni()));
                }

                // Find latest carnet details
                List<CarnetComercializare> carnete = carnetComercializareRepository.findByCnpHash(cnpHash);
                if (!carnete.isEmpty()) {
                    CarnetComercializare latestCarnet = carnete.get(carnete.size() - 1);
                    status.setNumarCarnet(latestCarnet.getSeria() + " " + latestCarnet.getNumarCarnet());
                    status.setCarnetDataEliberare(latestCarnet.getDataEliberare());
                }
                
                // Find cereri for user across all tenants
                List<Cerere> cereri = cerereRepository.findByUserId(cetatean.getId());
                
                Optional<Cerere> atestatCer = cereri.stream()
                    .filter(c -> c.getTipCerere() == TipCerere.ELIBERARE_ATESTAT_PRODUCATOR)
                    .max(Comparator.comparing(Cerere::getCreatedAt));
                    
                if (atestatCer.isPresent()) {
                    if (latestAtestatCerere == null || atestatCer.get().getCreatedAt().isAfter(latestAtestatCerere.getCreatedAt())) {
                        latestAtestatCerere = atestatCer.get();
                    }
                }
                
                Optional<Cerere> carnetCer = cereri.stream()
                    .filter(c -> c.getTipCerere() == TipCerere.ELIBERARE_CARNET_COMERCIALIZARE)
                    .max(Comparator.comparing(Cerere::getCreatedAt));
                    
                if (carnetCer.isPresent()) {
                    if (latestCarnetCerere == null || carnetCer.get().getCreatedAt().isAfter(latestCarnetCerere.getCreatedAt())) {
                        latestCarnetCerere = carnetCer.get();
                    }
                }

            } catch (Exception e) {
                System.err.println("Exception in AtestatController for tenant " + tenantId + ": " + e.getMessage());
                e.printStackTrace();
            } finally {
                TenantContext.clear();
            }
        }
        
        if (latestAtestatCerere != null) {
            status.setStareCerereAtestat(latestAtestatCerere.getStatus().name());
        }
        
        if (latestCarnetCerere != null) {
            status.setStareCerereCarnet(latestCarnetCerere.getStatus().name());
        }

        return org.springframework.http.ResponseEntity.ok(status);
    }
}