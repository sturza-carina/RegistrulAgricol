package com.multitenant.service;

import com.multitenant.model.core.Uat;
import com.multitenant.repository.UatRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UatService {

    private final UatRepository uatRepository;
    private final com.multitenant.repository.TenantRepository tenantRepository;

    public UatService(UatRepository uatRepository, com.multitenant.repository.TenantRepository tenantRepository) {
        this.uatRepository = uatRepository;
        this.tenantRepository = tenantRepository;
    }

    public Uat createUat(Uat uat) {
        if (uat.getCodSiruta() == null || uat.getCodSiruta().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "codSiruta is required");
        }

        String originalTenant = com.multitenant.config.tenant.TenantContext.getCurrentTenant();
        try {
            com.multitenant.config.tenant.TenantContext.setCurrentTenant("public");

            if (uatRepository.existsByCodSiruta(uat.getCodSiruta())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "UAT with codSiruta " + uat.getCodSiruta() + " already exists.");
            }

            if (uat.getIsActive() == null) {
                uat.setIsActive(true);
            }

            if (originalTenant != null && !"public".equals(originalTenant)) {
                com.multitenant.model.core.Tenant tenant = tenantRepository.findById(originalTenant)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));
                uat.setTenant(tenant);
            }

            return uatRepository.save(uat);
        } finally {
            com.multitenant.config.tenant.TenantContext.setCurrentTenant(originalTenant);
        }
    }

    public List<Uat> getAllUats() {
        String currentTenant = com.multitenant.config.tenant.TenantContext.getCurrentTenant();
        String originalTenant = currentTenant;
        try {
            com.multitenant.config.tenant.TenantContext.setCurrentTenant("public");
            if ("public".equals(currentTenant) || currentTenant == null) {
                return uatRepository.findAll();
            } else {
                return uatRepository.findByTenant_Id(currentTenant);
            }
        } finally {
            com.multitenant.config.tenant.TenantContext.setCurrentTenant(originalTenant);
        }
    }

    public Uat getUatByCodSiruta(String codSiruta) {
        if (codSiruta == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "codSiruta must not be null");
        }
        String originalTenant = com.multitenant.config.tenant.TenantContext.getCurrentTenant();
        try {
            com.multitenant.config.tenant.TenantContext.setCurrentTenant("public");
            return uatRepository.findByCodSiruta(codSiruta)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "UAT with codSiruta " + codSiruta + " not found."));
        } finally {
            com.multitenant.config.tenant.TenantContext.setCurrentTenant(originalTenant);
        }
    }

    public Uat updateUat(String codSiruta, Uat request) {
        String originalTenant = com.multitenant.config.tenant.TenantContext.getCurrentTenant();
        try {
            com.multitenant.config.tenant.TenantContext.setCurrentTenant("public");
            Uat existing = getUatByCodSiruta(codSiruta);

            if (existing == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "UAT not found");
            }

            if (request.getDenumire() != null) {
                existing.setDenumire(request.getDenumire());
            }
            if (request.getJudet() != null) {
                existing.setJudet(request.getJudet());
            }
            if (request.getTipUat() != null) {
                existing.setTipUat(request.getTipUat());
            }
            if (request.getIsActive() != null) {
                existing.setIsActive(request.getIsActive());
            }

            return uatRepository.save(existing);
        } finally {
            com.multitenant.config.tenant.TenantContext.setCurrentTenant(originalTenant);
        }
    }

    public void deleteUat(String codSiruta) {
        String originalTenant = com.multitenant.config.tenant.TenantContext.getCurrentTenant();
        try {
            com.multitenant.config.tenant.TenantContext.setCurrentTenant("public");
            Uat existing = getUatByCodSiruta(codSiruta);
            if (existing != null) {
                uatRepository.delete(existing);
            }
        } finally {
            com.multitenant.config.tenant.TenantContext.setCurrentTenant(originalTenant);
        }
    }
}
