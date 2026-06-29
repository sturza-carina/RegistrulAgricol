package com.multitenant.service;

import com.multitenant.model.registru.Gospodarie;
import com.multitenant.repository.GospodarieRepository;
import com.multitenant.repository.UatRepository;
import com.multitenant.model.core.Uat;
import org.springframework.stereotype.Service;

import java.util.List;

import com.multitenant.dto.GospodarieDTO;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class GospodarieService {

    private final GospodarieRepository gospodarieRepository;
    private final UatRepository uatRepository;
    private final ModelMapper modelMapper;

    public GospodarieService(GospodarieRepository gospodarieRepository, UatRepository uatRepository, ModelMapper modelMapper) {
        this.gospodarieRepository = gospodarieRepository;
        this.uatRepository = uatRepository;
        this.modelMapper = modelMapper;
    }

    public Page<GospodarieDTO> getAllGospodarii(Pageable pageable) {
        return gospodarieRepository.findAllByOrderByIdDesc(pageable)
                .map(entity -> modelMapper.map(entity, GospodarieDTO.class));
    }

    public GospodarieDTO getGospodarieById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        Gospodarie entity = gospodarieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gospodarie not found"));
        return modelMapper.map(entity, GospodarieDTO.class);
    }

    @Transactional
    public GospodarieDTO createGospodarie(Gospodarie gospodarie) {
        if (gospodarie == null) {
            throw new IllegalArgumentException("Gospodarie cannot be null");
        }
        
        if (gospodarie.getUat() != null && gospodarie.getUat().getId() != null) {
            Uat managedUat = uatRepository.findById(gospodarie.getUat().getId())
                    .orElseThrow(() -> new IllegalArgumentException("UAT not found"));
            gospodarie.setUat(managedUat);
        }
        
        Gospodarie saved = gospodarieRepository.save(gospodarie);
        return modelMapper.map(saved, GospodarieDTO.class);
    }

    public GospodarieDTO updateGospodarie(Long id, Gospodarie updatedGospodarie) {
        if (updatedGospodarie == null) {
            throw new IllegalArgumentException("Gospodarie cannot be null");
        }
        Gospodarie existing = gospodarieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gospodarie not found"));
        
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

        Gospodarie saved = gospodarieRepository.save(existing);
        return modelMapper.map(saved, GospodarieDTO.class);
    }

    public void deleteGospodarie(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        gospodarieRepository.deleteById(id);
    }

    public Page<GospodarieDTO> getAllGospodarii(String uatCode, Pageable pageable) {
        Page<Gospodarie> result;
        if (uatCode != null && !uatCode.isBlank()) {
            result = gospodarieRepository.findByUat_CodSirutaOrderByIdDesc(uatCode, pageable);
        } else {
            result = gospodarieRepository.findAllByOrderByIdDesc(pageable);
        }
        return result.map(entity -> modelMapper.map(entity, GospodarieDTO.class));
    }
}
