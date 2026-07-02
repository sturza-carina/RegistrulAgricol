package com.multitenant.service;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.model.core.PublicUat;
import com.multitenant.model.core.Uat;
import com.multitenant.repository.PublicUatRepository;
import com.multitenant.repository.UatRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

@Service
public class UatService {

    private final UatRepository uatRepository;
    private final PublicUatRepository publicUatRepository;

    public UatService(UatRepository uatRepository, PublicUatRepository publicUatRepository) {
        this.uatRepository = uatRepository;
        this.publicUatRepository = publicUatRepository;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GLOBAL public.uat operations (Super Admin only)
    // ─────────────────────────────────────────────────────────────────────────

    public List<PublicUat> getAllPublicUats() {
        return publicUatRepository.findByTenantIdIsNull();
    }

    public PublicUat createPublicUat(PublicUat uat) {
        if (uat.getCodSiruta() == null || uat.getCodSiruta().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "codSiruta is required");
        }
        if (publicUatRepository.existsByCodSiruta(uat.getCodSiruta())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "UAT with codSiruta " + uat.getCodSiruta() + " already exists in the global registry.");
        }
        if (uat.getIsActive() == null) uat.setIsActive(true);
        return publicUatRepository.save(uat);
    }

    public PublicUat updatePublicUat(String codSiruta, PublicUat request) {
        PublicUat existing = publicUatRepository.findByCodSiruta(codSiruta)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "UAT with codSiruta " + codSiruta + " not found in global registry."));
        if (request.getDenumire() != null) existing.setDenumire(request.getDenumire());
        if (request.getJudet() != null) existing.setJudet(request.getJudet());
        if (request.getTipUat() != null) existing.setTipUat(request.getTipUat());
        if (request.getIsActive() != null) existing.setIsActive(request.getIsActive());
        return publicUatRepository.save(existing);
    }

    public void deletePublicUat(String codSiruta) {
        PublicUat existing = publicUatRepository.findByCodSiruta(codSiruta)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "UAT with codSiruta " + codSiruta + " not found in global registry."));
        publicUatRepository.delete(existing);
    }

    @Cacheable("judete")
    public List<String> getDistinctJudete() {
        return publicUatRepository.findDistinctJudeteOrderByJudetAsc();
    }

    @Cacheable(value = "localitati", key = "#judet")
    public List<PublicUat> getLocalitatiByJudet(String judet) {
        return publicUatRepository.findByJudetOrderByDenumireAsc(judet);
    }

    @Cacheable("toate_localitatile")
    public List<PublicUat> getAllLocalitati() {
        return publicUatRepository.findAll();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TENANT-LOCAL uat operations (Tenant Admin / User)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the UATs in the current tenant's local schema.
     */
    public List<Uat> getTenantUats() {
        return uatRepository.findAll();
    }

    /**
     * Assigns a UAT from the global registry into the current tenant's local uat table.
     * The UAT data is copied from public.uat into tenant_X.uat.
     */
    public Uat assignUatToTenant(String codSiruta) {
        // Verify it exists in the global registry
        PublicUat globalUat = publicUatRepository.findByCodSiruta(codSiruta)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "UAT " + codSiruta + " not found in the global registry."));

        // Check if already assigned in this tenant or another tenant
        if (globalUat.getTenantId() != null) {
            if (globalUat.getTenantId().equals(TenantContext.getCurrentTenant())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "UAT " + codSiruta + " is already assigned to this tenant.");
            } else {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "UAT " + codSiruta + " is already claimed by another tenant.");
            }
        }

        // Copy from global into tenant-local table
        Uat tenantUat = new Uat();
        tenantUat.setCodSiruta(globalUat.getCodSiruta());
        tenantUat.setDenumire(globalUat.getDenumire());
        tenantUat.setJudet(globalUat.getJudet());
        tenantUat.setTipUat(globalUat.getTipUat());
        tenantUat.setIsActive(globalUat.getIsActive());
        tenantUat = uatRepository.save(tenantUat);
        
        // Mark it as claimed in the global registry
        globalUat.setTenantId(TenantContext.getCurrentTenant());
        publicUatRepository.save(globalUat);
        
        return tenantUat;
    }

    /**
     * Removes a UAT from the current tenant's local uat table.
     */
    public void removeUatFromTenant(String codSiruta) {
        Uat existing = uatRepository.findByCodSiruta(codSiruta)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "UAT " + codSiruta + " is not assigned to this tenant."));
        uatRepository.delete(existing);
        
        // Free it in the global registry
        publicUatRepository.findByCodSiruta(codSiruta).ifPresent(globalUat -> {
            globalUat.setTenantId(null);
            publicUatRepository.save(globalUat);
        });
    }
}
