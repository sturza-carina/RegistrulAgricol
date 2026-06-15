package com.multitenant.service;

import com.multitenant.model.registru.Teren;
import com.multitenant.model.registru.Parcela;
import com.multitenant.model.registru.Gospodarie;
import com.multitenant.repository.TerenRepository;
import com.multitenant.repository.ParcelaRepository;
import com.multitenant.repository.GospodarieRepository;
import com.multitenant.dto.TerenWithParcelaDTO;
import com.multitenant.dto.TerenCreateDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.springframework.lang.NonNull;

@Service
@SuppressWarnings("null")
public class TerenService {

    private final TerenRepository terenRepository;
    private final ParcelaRepository parcelaRepository;
    private final GospodarieRepository gospodarieRepository;

    public TerenService(TerenRepository terenRepository, ParcelaRepository parcelaRepository, GospodarieRepository gospodarieRepository) {
        this.terenRepository = terenRepository;
        this.parcelaRepository = parcelaRepository;
        this.gospodarieRepository = gospodarieRepository;
    }

    public List<Teren> getAllTerenuri() {
        return terenRepository.findAll();
    }

    public List<Teren> getTerenByGospodarieId(Long gospodarieId) {
        return terenRepository.findByGospodarieId(gospodarieId);
    }

    public Teren getTerenById(@NonNull Long id) {
        return terenRepository.findById(id).orElseThrow(() -> new RuntimeException("Teren not found"));
    }

    public Teren createTeren(Teren teren) {
        if (teren == null) {
            throw new IllegalArgumentException("Teren cannot be null");
        }
        return terenRepository.save(teren);
    }

    @Transactional
    public Teren createTerenFromDTO(@NonNull TerenCreateDTO dto) {
        if (dto.getGospodarieId() == null) throw new IllegalArgumentException("gospodarieId is required");

        Gospodarie gospodarie = gospodarieRepository.findById(dto.getGospodarieId())
            .orElseThrow(() -> new RuntimeException("Gospodarie not found: " + dto.getGospodarieId()));

        Teren teren = new Teren();
        teren.setDenumire(dto.getDenumire());
        teren.setTipTeren(dto.getTipTeren());
        teren.setStereo70Coordinates(dto.getStereo70Coordinates());
        teren.setPolygon(dto.getPolygon());
        teren.setGospodarie(gospodarie);
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

    public Teren updateTeren(@NonNull Long id, Teren updatedTeren) {
        if (updatedTeren == null) {
            throw new IllegalArgumentException("Teren cannot be null");
        }
        Teren existing = getTerenById(id);
        existing.setDenumire(updatedTeren.getDenumire());
        if (updatedTeren.getGospodarie() != null) existing.setGospodarie(updatedTeren.getGospodarie());
        return terenRepository.save(existing);
    }

    public void deleteTeren(@NonNull Long id) {
        terenRepository.deleteById(id);
    }
}
