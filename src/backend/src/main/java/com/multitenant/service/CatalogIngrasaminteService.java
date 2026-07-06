package com.multitenant.service;

import com.multitenant.dto.CatalogIngrasaminteDTO;
import com.multitenant.model.registru.CatalogIngrasaminte;
import com.multitenant.repository.CatalogIngrasaminteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogIngrasaminteService {

    private final CatalogIngrasaminteRepository catalogIngrasaminteRepository;

    @Transactional(readOnly = true)
    public Page<CatalogIngrasaminteDTO> getIngrasaminteCatalog(String query, Pageable pageable) {
        Page<CatalogIngrasaminte> page;
        if (query != null && !query.trim().isEmpty()) {
            page = catalogIngrasaminteRepository.findByDenumireContainingIgnoreCase(query, pageable);
        } else {
            page = catalogIngrasaminteRepository.findAll(pageable);
        }
        return page.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public CatalogIngrasaminteDTO getIngrasamantById(Long id) {
        CatalogIngrasaminte entity = catalogIngrasaminteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Îngrășământul nu a fost găsit în catalog."));
        return mapToDto(entity);
    }

    @Transactional
    public CatalogIngrasaminteDTO createIngrasamant(CatalogIngrasaminteDTO dto) {
        CatalogIngrasaminte entity = new CatalogIngrasaminte();
        mapToEntity(dto, entity);
        CatalogIngrasaminte saved = catalogIngrasaminteRepository.save(entity);
        return mapToDto(saved);
    }

    @Transactional
    public CatalogIngrasaminteDTO updateIngrasamant(Long id, CatalogIngrasaminteDTO dto) {
        CatalogIngrasaminte entity = catalogIngrasaminteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Îngrășământul nu a fost găsit în catalog."));
        mapToEntity(dto, entity);
        CatalogIngrasaminte updated = catalogIngrasaminteRepository.save(entity);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteIngrasamant(Long id) {
        if (!catalogIngrasaminteRepository.existsById(id)) {
            throw new RuntimeException("Îngrășământul nu a fost găsit în catalog.");
        }
        catalogIngrasaminteRepository.deleteById(id);
    }

    public CatalogIngrasaminteDTO mapToDto(CatalogIngrasaminte entity) {
        CatalogIngrasaminteDTO dto = new CatalogIngrasaminteDTO();
        dto.setId(entity.getId());
        dto.setDenumire(entity.getDenumire());
        dto.setTip(entity.getTip());
        dto.setProcentAzot(entity.getProcentAzot());
        dto.setProcentFosfor(entity.getProcentFosfor());
        dto.setProcentPotasiu(entity.getProcentPotasiu());
        return dto;
    }

    public void mapToEntity(CatalogIngrasaminteDTO dto, CatalogIngrasaminte entity) {
        entity.setDenumire(dto.getDenumire());
        entity.setTip(dto.getTip());
        entity.setProcentAzot(dto.getProcentAzot());
        entity.setProcentFosfor(dto.getProcentFosfor());
        entity.setProcentPotasiu(dto.getProcentPotasiu());
    }
}
