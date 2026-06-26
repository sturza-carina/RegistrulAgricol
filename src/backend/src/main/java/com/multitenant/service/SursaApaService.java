package com.multitenant.service;

import com.multitenant.dto.SursaApaDTO;
import com.multitenant.model.registru.Parcela;
import com.multitenant.model.registru.SursaApa;
import com.multitenant.repository.ParcelaRepository;
import com.multitenant.repository.SursaApaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SursaApaService {

    private final SursaApaRepository sursaApaRepository;
    private final ParcelaRepository parcelaRepository;

    public List<SursaApaDTO> getSurseByParcelaId(Long parcelaId) {
        return sursaApaRepository.findByParcela_Id(parcelaId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public SursaApaDTO addSursaToParcela(Long parcelaId, SursaApaDTO dto) {
        Parcela parcela = parcelaRepository.findById(parcelaId)
                .orElseThrow(() -> new RuntimeException("Parcela cu id " + parcelaId + " nu a fost gasita"));

        if (dto.getDebitMcOra() != null && dto.getDebitMcOra() < 0) {
            throw new IllegalArgumentException("debitMcOra trebuie sa fie >= 0");
        }

        SursaApa entity = toEntity(dto, parcela);
        return toDto(sursaApaRepository.save(entity));
    }

    @Transactional
    public SursaApaDTO updateSursa(Long parcelaId, Long sursaId, SursaApaDTO dto) {
        parcelaRepository.findById(parcelaId)
                .orElseThrow(() -> new RuntimeException("Parcela cu id " + parcelaId + " nu a fost gasita"));

        SursaApa existing = sursaApaRepository.findById(sursaId)
                .orElseThrow(() -> new RuntimeException("SursaApa cu id " + sursaId + " nu a fost gasita"));

        if (!existing.getParcela().getId().equals(parcelaId)) {
            throw new IllegalArgumentException("SursaApa cu id " + sursaId + " nu apartine parcelei " + parcelaId);
        }

        if (dto.getDebitMcOra() != null && dto.getDebitMcOra() < 0) {
            throw new IllegalArgumentException("debitMcOra trebuie sa fie >= 0");
        }

        existing.setTipSursa(dto.getTipSursa());
        existing.setDebitMcOra(dto.getDebitMcOra());
        existing.setStareFunctionare(dto.isStareFunctionare());

        return toDto(sursaApaRepository.save(existing));
    }

    @Transactional
    public void deleteSursa(Long parcelaId, Long sursaId) {
        parcelaRepository.findById(parcelaId)
                .orElseThrow(() -> new RuntimeException("Parcela cu id " + parcelaId + " nu a fost gasita"));

        SursaApa existing = sursaApaRepository.findById(sursaId)
                .orElseThrow(() -> new RuntimeException("SursaApa cu id " + sursaId + " nu a fost gasita"));

        if (!existing.getParcela().getId().equals(parcelaId)) {
            throw new IllegalArgumentException("SursaApa cu id " + sursaId + " nu apartine parcelei " + parcelaId);
        }

        sursaApaRepository.delete(existing);
    }

    private SursaApaDTO toDto(SursaApa entity) {
        SursaApaDTO dto = new SursaApaDTO();
        dto.setId(entity.getId());
        dto.setParcelaId(entity.getParcelaId());
        dto.setTipSursa(entity.getTipSursa());
        dto.setDebitMcOra(entity.getDebitMcOra());
        dto.setStareFunctionare(entity.isStareFunctionare());
        return dto;
    }

    private SursaApa toEntity(SursaApaDTO dto, Parcela parcela) {
        SursaApa entity = new SursaApa();
        entity.setParcela(parcela);
        entity.setTipSursa(dto.getTipSursa());
        entity.setDebitMcOra(dto.getDebitMcOra());
        entity.setStareFunctionare(dto.isStareFunctionare());
        return entity;
    }
}
