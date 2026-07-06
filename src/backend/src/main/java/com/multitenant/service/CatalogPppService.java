package com.multitenant.service;

import com.multitenant.dto.CatalogPppDTO;
import com.multitenant.model.registru.CatalogPpp;
import com.multitenant.repository.CatalogPppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogPppService {

    private final CatalogPppRepository catalogPppRepository;

    @Transactional(readOnly = true)
    public Page<CatalogPppDTO> getPppCatalog(String query, Pageable pageable) {
        Page<CatalogPpp> page;
        if (query != null && !query.trim().isEmpty()) {
            page = catalogPppRepository.findByDenumireComercialaContainingIgnoreCase(query, pageable);
        } else {
            page = catalogPppRepository.findAll(pageable);
        }
        return page.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public CatalogPppDTO getPppById(Long id) {
        CatalogPpp entity = catalogPppRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produsul PPP nu a fost găsit în catalog."));
        return mapToDto(entity);
    }

    @Transactional
    public CatalogPppDTO createPpp(CatalogPppDTO dto) {
        CatalogPpp entity = new CatalogPpp();
        mapToEntity(dto, entity);
        CatalogPpp saved = catalogPppRepository.save(entity);
        return mapToDto(saved);
    }

    @Transactional
    public CatalogPppDTO updatePpp(Long id, CatalogPppDTO dto) {
        CatalogPpp entity = catalogPppRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produsul PPP nu a fost găsit în catalog."));
        mapToEntity(dto, entity);
        CatalogPpp updated = catalogPppRepository.save(entity);
        return mapToDto(updated);
    }

    @Transactional
    public void deletePpp(Long id) {
        if (!catalogPppRepository.existsById(id)) {
            throw new RuntimeException("Produsul PPP nu a fost găsit în catalog.");
        }
        catalogPppRepository.deleteById(id);
    }

    public CatalogPppDTO mapToDto(CatalogPpp entity) {
        CatalogPppDTO dto = new CatalogPppDTO();
        dto.setId(entity.getId());
        dto.setDenumireComerciala(entity.getDenumireComerciala());
        dto.setTip(entity.getTip());
        dto.setDaunatorVizat(entity.getDaunatorVizat());
        dto.setDozaOmologata(entity.getDozaOmologata());
        dto.setTimpPauza(entity.getTimpPauza());
        return dto;
    }

    public void mapToEntity(CatalogPppDTO dto, CatalogPpp entity) {
        entity.setDenumireComerciala(dto.getDenumireComerciala());
        entity.setTip(dto.getTip());
        entity.setDaunatorVizat(dto.getDaunatorVizat());
        entity.setDozaOmologata(dto.getDozaOmologata());
        entity.setTimpPauza(dto.getTimpPauza());
    }
}
