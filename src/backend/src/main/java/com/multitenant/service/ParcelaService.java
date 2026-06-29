package com.multitenant.service;

import com.multitenant.model.registru.Teren;
import com.multitenant.model.registru.Parcela;
import com.multitenant.repository.TerenRepository;
import com.multitenant.repository.ParcelaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class ParcelaService {

    private final ParcelaRepository parcelaRepository;
    private final TerenRepository terenRepository;

    public ParcelaService(ParcelaRepository parcelaRepository, TerenRepository terenRepository) {
        this.parcelaRepository = parcelaRepository;
        this.terenRepository = terenRepository;
    }

    public Page<Parcela> getAllParceleForTenant(Pageable pageable) {
        return parcelaRepository.findAll(pageable);
    }

    public Page<Parcela> getParceleForTeren(long terenId, Pageable pageable) {
        return parcelaRepository.findByTerenId(terenId, pageable);
    }

    public Parcela saveParcela(long terenId, Parcela parcela) {
        if (parcela == null) {
            throw new IllegalArgumentException("Parcela cannot be null");
        }
        Teren teren = terenRepository.findById(terenId)
                .orElseThrow(() -> new RuntimeException("Teren not found"));
        parcela.setTeren(teren);

        if (parcela.getStereo70Coordinates() != null && !parcela.getStereo70Coordinates().trim().isEmpty()) {
            // Already set by frontend or explicitly passed
        }
        return parcelaRepository.save(parcela);
    }

    public Parcela updateParcela(long id, Parcela updatedParcela) {
        if (updatedParcela == null) {
            throw new IllegalArgumentException("Parcela cannot be null");
        }
        Parcela existing = parcelaRepository.findById(id).orElse(null);
        if (existing == null) {
            throw new RuntimeException("Parcela not found");
        }

        if (updatedParcela.getDenumire() != null)
            existing.setDenumire(updatedParcela.getDenumire());
        if (updatedParcela.getSuprafata() != null)
            existing.setSuprafata(updatedParcela.getSuprafata());
        if (updatedParcela.getCategorieFolosinta() != null)
            existing.setCategorieFolosinta(updatedParcela.getCategorieFolosinta());
        
        if (updatedParcela.getStereo70Coordinates() != null && !updatedParcela.getStereo70Coordinates().trim().isEmpty()) {
            existing.setStereo70Coordinates(updatedParcela.getStereo70Coordinates());
        }
        if (updatedParcela.getPolygon() != null) {
            existing.setPolygon(updatedParcela.getPolygon());
        }

        return parcelaRepository.save(existing);
    }



    public void deleteParcela(long id) {
        parcelaRepository.deleteById(id);
    }
}
