package com.multitenant.service;

import com.multitenant.model.registru.CategorieFolosinta;
import com.multitenant.model.registru.Teren;
import com.multitenant.repository.CategorieFolosintaRepository;
import com.multitenant.repository.TerenRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategorieFolosintaService {

    private final CategorieFolosintaRepository categorieFolosintaRepository;
    private final TerenRepository terenRepository;

    public CategorieFolosintaService(CategorieFolosintaRepository categorieFolosintaRepository,
                                     TerenRepository terenRepository) {
        this.categorieFolosintaRepository = categorieFolosintaRepository;
        this.terenRepository = terenRepository;
    }

    public List<CategorieFolosinta> getCategoriiForTeren(Long terenId) {
        return categorieFolosintaRepository.findByTerenId(terenId);
    }

    public CategorieFolosinta getCategorieById(Long id) {
        return categorieFolosintaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CategorieFolosinta not found"));
    }

    public CategorieFolosinta saveCategorie(Long terenId, CategorieFolosinta categorie) {
        if (categorie == null) {
            throw new IllegalArgumentException("CategorieFolosinta cannot be null");
        }

        Teren teren = terenRepository.findById(terenId)
                .orElseThrow(() -> new RuntimeException("Teren not found"));
        categorie.setTeren(teren);
        return categorieFolosintaRepository.save(categorie);
    }

    public CategorieFolosinta updateCategorie(Long id, CategorieFolosinta updatedCategorie) {
        if (updatedCategorie == null) {
            throw new IllegalArgumentException("CategorieFolosinta cannot be null");
        }
        CategorieFolosinta existing = getCategorieById(id);
        if (updatedCategorie.getDenumire() != null) {
            existing.setDenumire(updatedCategorie.getDenumire());
        }
        if (updatedCategorie.getDescriere() != null) {
            existing.setDescriere(updatedCategorie.getDescriere());
        }
        return categorieFolosintaRepository.save(existing);
    }

    public void deleteCategorie(Long id) {
        categorieFolosintaRepository.deleteById(id);
    }
}
