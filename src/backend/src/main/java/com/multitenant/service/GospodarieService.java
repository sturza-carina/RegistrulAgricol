package com.multitenant.service;

import com.multitenant.model.registru.Gospodarie;
import com.multitenant.repository.GospodarieRepository;
import com.multitenant.repository.UatRepository;
import com.multitenant.model.core.Uat;
import org.springframework.stereotype.Service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Service
public class GospodarieService {

    private final GospodarieRepository gospodarieRepository;
    private final UatRepository uatRepository;

    public GospodarieService(GospodarieRepository gospodarieRepository, UatRepository uatRepository) {
        this.gospodarieRepository = gospodarieRepository;
        this.uatRepository = uatRepository;
    }

    public List<Gospodarie> getAllGospodarii() {
        return gospodarieRepository.findAll();
    }

    public Gospodarie getGospodarieById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return gospodarieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gospodarie not found"));
    }

    @Transactional
    public Gospodarie createGospodarie(Gospodarie gospodarie) {
        if (gospodarie == null) {
            throw new IllegalArgumentException("Gospodarie cannot be null");
        }
        
        if (gospodarie.getUat() != null && gospodarie.getUat().getId() != null) {
            Uat managedUat = uatRepository.findById(gospodarie.getUat().getId())
                    .orElseThrow(() -> new IllegalArgumentException("UAT not found"));
            gospodarie.setUat(managedUat);
        }
        
        return gospodarieRepository.save(gospodarie);
    }

    public Gospodarie updateGospodarie(Long id, Gospodarie updatedGospodarie) {
        if (updatedGospodarie == null) {
            throw new IllegalArgumentException("Gospodarie cannot be null");
        }
        Gospodarie existing = getGospodarieById(id);
        
        if (updatedGospodarie.getCodGospodarie() != null) existing.setCodGospodarie(updatedGospodarie.getCodGospodarie());
        if (updatedGospodarie.getAdresa() != null) existing.setAdresa(updatedGospodarie.getAdresa());
        if (updatedGospodarie.getTipGospodarie() != null) existing.setTipGospodarie(updatedGospodarie.getTipGospodarie());
        existing.setActiva(updatedGospodarie.isActiva());
        if (updatedGospodarie.getUat() != null) {
            if (updatedGospodarie.getUat().getId() != null) {
                Uat managedUat = uatRepository.findById(updatedGospodarie.getUat().getId())
                        .orElseThrow(() -> new IllegalArgumentException("UAT not found"));
                existing.setUat(managedUat);
            } else {
                existing.setUat(updatedGospodarie.getUat());
            }
        }

        return gospodarieRepository.save(existing);
    }

    public void deleteGospodarie(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        gospodarieRepository.deleteById(id);
    }

    public List<Gospodarie> getAllGospodarii(String uatCode) {
        if (uatCode != null && !uatCode.isBlank()) {
            return gospodarieRepository.findByUat_CodSiruta(uatCode);
        }
        return gospodarieRepository.findAll();
    }
}
