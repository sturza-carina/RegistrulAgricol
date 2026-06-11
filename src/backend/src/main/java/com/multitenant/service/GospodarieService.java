package com.multitenant.service;

import com.multitenant.model.registru.Gospodarie;
import com.multitenant.repository.GospodarieRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import com.multitenant.repository.TerenRepository;
import com.multitenant.model.registru.Teren;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GospodarieService {

    private final GospodarieRepository gospodarieRepository;
    private final TerenRepository terenRepository;

    public GospodarieService(GospodarieRepository gospodarieRepository, TerenRepository terenRepository) {
        this.gospodarieRepository = gospodarieRepository;
        this.terenRepository = terenRepository;
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
        Gospodarie saved = gospodarieRepository.save(gospodarie);
        
        Teren teren = new Teren();
        teren.setDenumire("Teren Agricol " + saved.getCodGospodarie());
        teren.setGospodarie(saved);
        terenRepository.save(teren);
        
        return saved;
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
        if (updatedGospodarie.getUat() != null) existing.setUat(updatedGospodarie.getUat());

        return gospodarieRepository.save(existing);
    }

    public void deleteGospodarie(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        gospodarieRepository.deleteById(id);
    }
}
