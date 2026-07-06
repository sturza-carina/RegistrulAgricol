package com.multitenant.service;

import com.multitenant.dto.PomDTO;
import com.multitenant.model.registru.Parcela;
import com.multitenant.model.registru.Pom;
import com.multitenant.model.registru.TipInregistrarePom;
import com.multitenant.repository.ParcelaRepository;
import com.multitenant.repository.PomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PomService {

    private final PomRepository pomRepository;
    private final ParcelaRepository parcelaRepository;

    @Transactional(readOnly = true)
    public Page<PomDTO> getPomiByParcelaId(Long parcelaId, Pageable pageable) {
        return pomRepository.findByParcela_Id(parcelaId, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<PomDTO> getPomiByParcelaIdAndTip(Long parcelaId, TipInregistrarePom tip, Pageable pageable) {
        return pomRepository.findByParcela_IdAndTipInregistrare(parcelaId, tip, pageable)
                .map(this::mapToDto);
    }

    @Transactional
    public PomDTO addPomToParcela(Long parcelaId, PomDTO dto) {
        Parcela parcela = parcelaRepository.findById(parcelaId)
                .orElseThrow(() -> new RuntimeException("Parcela not found with id: " + parcelaId));

        Pom pom = new Pom();
        mapToEntity(dto, pom);
        pom.setParcela(parcela);

        Pom saved = pomRepository.save(pom);
        return mapToDto(saved);
    }

    @Transactional
    public PomDTO updatePom(Long parcelaId, Long pomId, PomDTO dto) {
        Pom pom = pomRepository.findById(pomId)
                .orElseThrow(() -> new RuntimeException("Pom not found with id: " + pomId));

        if (!pom.getParcela().getId().equals(parcelaId)) {
            throw new RuntimeException("Pomul nu aparține parcelei specificate");
        }

        mapToEntity(dto, pom);
        Pom updated = pomRepository.save(pom);
        return mapToDto(updated);
    }

    @Transactional
    public void deletePom(Long parcelaId, Long pomId) {
        Pom pom = pomRepository.findById(pomId)
                .orElseThrow(() -> new RuntimeException("Pom not found with id: " + pomId));

        if (!pom.getParcela().getId().equals(parcelaId)) {
            throw new RuntimeException("Pomul nu aparține parcelei specificate");
        }

        pomRepository.delete(pom);
    }

    private PomDTO mapToDto(Pom entity) {
        PomDTO dto = new PomDTO();
        dto.setId(entity.getId());
        dto.setTipInregistrare(entity.getTipInregistrare());
        dto.setSpecie(entity.getSpecie());
        dto.setSoi(entity.getSoi());
        dto.setAnPlantare(entity.getAnPlantare());
        dto.setNumarPomi(entity.getNumarPomi());
        dto.setSuprafataHa(entity.getSuprafataHa());
        dto.setDensitatePomiHa(entity.getDensitatePomiHa());
        dto.setStarePomi(entity.getStarePomi());
        dto.setSistemIntretinere(entity.getSistemIntretinere());
        dto.setSistemIrigare(entity.getSistemIrigare());
        dto.setProductieEstimataKg(entity.getProductieEstimataKg());
        dto.setObservatii(entity.getObservatii());
        dto.setParcelaId(entity.getParcelaId());
        return dto;
    }

    private void mapToEntity(PomDTO dto, Pom entity) {
        entity.setTipInregistrare(dto.getTipInregistrare());
        entity.setSpecie(dto.getSpecie());
        entity.setSoi(dto.getSoi());
        entity.setAnPlantare(dto.getAnPlantare());
        entity.setNumarPomi(dto.getNumarPomi());
        entity.setSuprafataHa(dto.getSuprafataHa());
        entity.setDensitatePomiHa(dto.getDensitatePomiHa());
        entity.setStarePomi(dto.getStarePomi());
        entity.setSistemIntretinere(dto.getSistemIntretinere());
        entity.setSistemIrigare(dto.getSistemIrigare());
        entity.setProductieEstimataKg(dto.getProductieEstimataKg());
        entity.setObservatii(dto.getObservatii());
    }
}