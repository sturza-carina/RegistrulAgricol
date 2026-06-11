package com.multitenant.service;

import com.multitenant.model.registru.Teren;
import com.multitenant.model.registru.Parcela;
import com.multitenant.repository.TerenRepository;
import com.multitenant.repository.ParcelaRepository;
import com.multitenant.dto.TerenWithParcelaDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TerenService {

    private final TerenRepository terenRepository;
    private final ParcelaRepository parcelaRepository;

    public TerenService(TerenRepository terenRepository, ParcelaRepository parcelaRepository) {
        this.terenRepository = terenRepository;
        this.parcelaRepository = parcelaRepository;
    }

    public List<Teren> getAllTerenuri() {
        return terenRepository.findAll();
    }

    public Teren getTerenByGospodarieId(Long gospodarieId) {
        return terenRepository.findByGospodarieId(gospodarieId).orElse(null);
    }

    public Teren getTerenById(Long id) {
        return terenRepository.findById(id).orElseThrow(() -> new RuntimeException("Teren not found"));
    }

    public Teren createTeren(Teren teren) {
        if (teren == null) {
            throw new IllegalArgumentException("Teren cannot be null");
        }
        return terenRepository.save(teren);
    }

    @Transactional
    public Teren createTerenWithParcela(TerenWithParcelaDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("DTO cannot be null");
        }
        Teren teren = dto.getTeren();
        if (teren == null) {
            throw new IllegalArgumentException("Teren cannot be null");
        }
        Teren savedTeren = terenRepository.save(teren);
        Parcela parcela = dto.getParcela();
        if (parcela != null) {
            parcela.setTeren(savedTeren);
            parcelaRepository.save(parcela);
        }
        return savedTeren;
    }

    public Teren updateTeren(Long id, Teren updatedTeren) {
        if (updatedTeren == null) {
            throw new IllegalArgumentException("Teren cannot be null");
        }
        Teren existing = getTerenById(id);
        existing.setDenumire(updatedTeren.getDenumire());
        if (updatedTeren.getGospodarie() != null) existing.setGospodarie(updatedTeren.getGospodarie());
        return terenRepository.save(existing);
    }

    public void deleteTeren(Long id) {
        terenRepository.deleteById(id);
    }
}
