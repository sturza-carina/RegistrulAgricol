package com.multitenant.service;

import com.multitenant.dto.CulturaParcelaDto;
import com.multitenant.model.registru.CulturaParcela;
import com.multitenant.model.registru.Parcela;
import com.multitenant.repository.CulturaParcelaRepository;
import com.multitenant.repository.ParcelaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CulturaParcelaService {

    private final CulturaParcelaRepository culturaParcelaRepository;
    private final ParcelaRepository parcelaRepository;

    @Transactional(readOnly = true)
    public List<CulturaParcelaDto> getCulturiByParcelaId(Long parcelaId) {
        return culturaParcelaRepository.findByParcela_Id(parcelaId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CulturaParcelaDto addCulturaToParcela(Long parcelaId, CulturaParcelaDto dto) {
        Parcela parcela = parcelaRepository.findById(parcelaId)
                .orElseThrow(() -> new RuntimeException("Parcela not found with id: " + parcelaId));

        CulturaParcela cultura = new CulturaParcela();
        mapToEntity(dto, cultura);
        cultura.setParcela(parcela);

        CulturaParcela saved = culturaParcelaRepository.save(cultura);
        return mapToDto(saved);
    }

    @Transactional
    public CulturaParcelaDto updateCultura(Long parcelaId, Long culturaId, CulturaParcelaDto dto) {
        CulturaParcela cultura = culturaParcelaRepository.findById(culturaId)
                .orElseThrow(() -> new RuntimeException("Cultura not found with id: " + culturaId));

        if (!cultura.getParcela().getId().equals(parcelaId)) {
            throw new RuntimeException("Cultura does not belong to the specified parcela");
        }

        mapToEntity(dto, cultura);
        CulturaParcela updated = culturaParcelaRepository.save(cultura);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteCultura(Long parcelaId, Long culturaId) {
        CulturaParcela cultura = culturaParcelaRepository.findById(culturaId)
                .orElseThrow(() -> new RuntimeException("Cultura not found with id: " + culturaId));

        if (!cultura.getParcela().getId().equals(parcelaId)) {
            throw new RuntimeException("Cultura does not belong to the specified parcela");
        }

        culturaParcelaRepository.delete(cultura);
    }

    private CulturaParcelaDto mapToDto(CulturaParcela entity) {
        CulturaParcelaDto dto = new CulturaParcelaDto();
        dto.setId(entity.getId());
        dto.setAnAgricol(entity.getAnAgricol());
        dto.setSpecieCultura(entity.getSpecieCultura());
        dto.setSuprafataCultivataHa(entity.getSuprafataCultivataHa());
        dto.setDataInsamantare(entity.getDataInsamantare());
        dto.setDataRecoltare(entity.getDataRecoltare());
        dto.setProductieHectarKg(entity.getProductieHectarKg());
        dto.setProductieTotalaTone(entity.getProductieTotalaTone());
        dto.setSistemIrigare(entity.getSistemIrigare());
        dto.setTipSol(entity.getTipSol());
        dto.setCulturaPrecedenta(entity.getCulturaPrecedenta());
        dto.setParcelaId(entity.getParcelaId());
        return dto;
    }

    private void mapToEntity(CulturaParcelaDto dto, CulturaParcela entity) {
        entity.setAnAgricol(dto.getAnAgricol());
        entity.setSpecieCultura(dto.getSpecieCultura());
        entity.setSuprafataCultivataHa(dto.getSuprafataCultivataHa());
        entity.setDataInsamantare(dto.getDataInsamantare());
        entity.setDataRecoltare(dto.getDataRecoltare());
        entity.setProductieHectarKg(dto.getProductieHectarKg());
        entity.setProductieTotalaTone(dto.getProductieTotalaTone());
        entity.setSistemIrigare(dto.getSistemIrigare());
        entity.setTipSol(dto.getTipSol());
        entity.setCulturaPrecedenta(dto.getCulturaPrecedenta());
    }
}
