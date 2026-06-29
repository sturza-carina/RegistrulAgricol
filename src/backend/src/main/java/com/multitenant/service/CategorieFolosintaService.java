package com.multitenant.service;

import com.multitenant.model.registru.CategorieFolosinta;
import com.multitenant.model.registru.Teren;
import com.multitenant.repository.CategorieFolosintaRepository;
import com.multitenant.repository.TerenRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@SuppressWarnings("null")
public class CategorieFolosintaService {

    private final CategorieFolosintaRepository categorieFolosintaRepository;
    private final TerenRepository terenRepository;

    public CategorieFolosintaService(CategorieFolosintaRepository categorieFolosintaRepository,
                                     TerenRepository terenRepository) {
        this.categorieFolosintaRepository = categorieFolosintaRepository;
        this.terenRepository = terenRepository;
    }

    public Page<CategorieFolosinta> getCategoriiForTeren(@NonNull Long terenId, Pageable pageable) {
        return categorieFolosintaRepository.findByTerenId(terenId, pageable);
    }

    public CategorieFolosinta getCategorieById(@NonNull Long id) {
        return categorieFolosintaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CategorieFolosinta not found"));
    }

    public CategorieFolosinta saveCategorie(@NonNull Long terenId, @NonNull CategorieFolosinta categorie) {
        Teren teren = terenRepository.findById(terenId)
                .orElseThrow(() -> new RuntimeException("Teren not found"));
        categorie.setTeren(teren);
        return categorieFolosintaRepository.save(categorie);
    }

    public CategorieFolosinta updateCategorie(@NonNull Long id, @NonNull CategorieFolosinta updatedCategorie) {
        CategorieFolosinta existing = getCategorieById(id);
        if (updatedCategorie.getDenumire() != null) {
            existing.setDenumire(updatedCategorie.getDenumire());
        }
        if (updatedCategorie.getDescriere() != null) {
            existing.setDescriere(updatedCategorie.getDescriere());
        }
        return categorieFolosintaRepository.save(existing);
    }

    public void deleteCategorie(@NonNull Long id) {
        categorieFolosintaRepository.deleteById(id);
    }
}
