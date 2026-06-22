package com.multitenant.service;

import com.multitenant.model.registru.Teren;
import com.multitenant.model.registru.Parcela;
import com.multitenant.model.registru.CategorieFolosinta;
import com.multitenant.repository.TerenRepository;
import com.multitenant.repository.ParcelaRepository;
import com.multitenant.repository.CategorieFolosintaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParcelaService {

    private final ParcelaRepository parcelaRepository;
    private final TerenRepository terenRepository;
    private final CategorieFolosintaRepository categorieFolosintaRepository;

    public ParcelaService(ParcelaRepository parcelaRepository, TerenRepository terenRepository,
                           CategorieFolosintaRepository categorieFolosintaRepository) {
        this.parcelaRepository = parcelaRepository;
        this.terenRepository = terenRepository;
        this.categorieFolosintaRepository = categorieFolosintaRepository;
    }

    private CategorieFolosinta resolveCategorie(Long categorieFolosintaId) {
        return categorieFolosintaRepository.findById(categorieFolosintaId)
                .orElseThrow(() -> new RuntimeException("Categorie Folosinta not found"));
    }

    public List<Parcela> getAllParceleForTenant() {
        return parcelaRepository.findAll();
    }

    public List<Parcela> getParceleForTeren(long terenId) {
        return parcelaRepository.findByTerenId(terenId);
    }

    public Parcela saveParcela(long terenId, Parcela parcela) {
        if (parcela == null) {
            throw new IllegalArgumentException("Parcela cannot be null");
        }
        Teren teren = terenRepository.findById(terenId)
                .orElseThrow(() -> new RuntimeException("Teren not found"));
        parcela.setTeren(teren);

        if (parcela.getCategorieFolosintaId() != null) {
            parcela.setCategorieFolosinta(resolveCategorie(parcela.getCategorieFolosintaId()));
        }

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
        if (updatedParcela.getCategorieFolosintaId() != null)
            existing.setCategorieFolosinta(resolveCategorie(updatedParcela.getCategorieFolosintaId()));

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
