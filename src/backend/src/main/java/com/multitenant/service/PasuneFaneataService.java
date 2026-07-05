package com.multitenant.service;

import com.multitenant.dto.PasuneFaneataDTO;
import com.multitenant.model.registru.Parcela;
import com.multitenant.model.registru.PasuneFaneata;
import com.multitenant.model.registru.TipFolosintaPasune;
import com.multitenant.repository.ParcelaRepository;
import com.multitenant.repository.PasuneFaneataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PasuneFaneataService {

    private final PasuneFaneataRepository pasuneFaneataRepository;
    private final ParcelaRepository parcelaRepository;

    @Transactional(readOnly = true)
    public Page<PasuneFaneataDTO> getByParcelaId(Long parcelaId, Pageable pageable) {
        return pasuneFaneataRepository.findByParcela_Id(parcelaId, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<PasuneFaneataDTO> getByParcelaIdAndTip(Long parcelaId, TipFolosintaPasune tip, Pageable pageable) {
        return pasuneFaneataRepository.findByParcela_IdAndTipFolosinta(parcelaId, tip, pageable)
                .map(this::mapToDto);
    }

    @Transactional
    public PasuneFaneataDTO addToParcela(Long parcelaId, PasuneFaneataDTO dto) {
        Parcela parcela = parcelaRepository.findById(parcelaId)
                .orElseThrow(() -> new RuntimeException("Parcela not found with id: " + parcelaId));

        PasuneFaneata entity = new PasuneFaneata();
        mapToEntity(dto, entity);
        entity.setParcela(parcela);

        PasuneFaneata saved = pasuneFaneataRepository.save(entity);
        return mapToDto(saved);
    }

    @Transactional
    public PasuneFaneataDTO update(Long parcelaId, Long id, PasuneFaneataDTO dto) {
        PasuneFaneata entity = pasuneFaneataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Înregistrarea nu a fost găsită cu id: " + id));

        if (!entity.getParcela().getId().equals(parcelaId)) {
            throw new RuntimeException("Înregistrarea nu aparține parcelei specificate");
        }

        mapToEntity(dto, entity);
        PasuneFaneata updated = pasuneFaneataRepository.save(entity);
        return mapToDto(updated);
    }

    @Transactional
    public void delete(Long parcelaId, Long id) {
        PasuneFaneata entity = pasuneFaneataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Înregistrarea nu a fost găsită cu id: " + id));

        if (!entity.getParcela().getId().equals(parcelaId)) {
            throw new RuntimeException("Înregistrarea nu aparține parcelei specificate");
        }

        pasuneFaneataRepository.delete(entity);
    }

    private PasuneFaneataDTO mapToDto(PasuneFaneata entity) {
        PasuneFaneataDTO dto = new PasuneFaneataDTO();
        dto.setId(entity.getId());
        dto.setTipFolosinta(entity.getTipFolosinta());
        dto.setSuprafataHa(entity.getSuprafataHa());
        dto.setSpeciiDominante(entity.getSpeciiDominante());
        dto.setNumarAnimalePasunat(entity.getNumarAnimalePasunat());
        dto.setNumarCosiriAnuale(entity.getNumarCosiriAnuale());
        dto.setProductieEstimataKgHa(entity.getProductieEstimataKgHa());
        dto.setStareVegetatie(entity.getStareVegetatie());
        dto.setSistemIntretinere(entity.getSistemIntretinere());
        dto.setSistemIrigare(entity.getSistemIrigare());
        dto.setObservatii(entity.getObservatii());
        dto.setParcelaId(entity.getParcelaId());
        return dto;
    }

    private void mapToEntity(PasuneFaneataDTO dto, PasuneFaneata entity) {
        entity.setTipFolosinta(dto.getTipFolosinta());
        entity.setSuprafataHa(dto.getSuprafataHa());
        entity.setSpeciiDominante(dto.getSpeciiDominante());
        entity.setNumarAnimalePasunat(dto.getNumarAnimalePasunat());
        entity.setNumarCosiriAnuale(dto.getNumarCosiriAnuale());
        entity.setProductieEstimataKgHa(dto.getProductieEstimataKgHa());
        entity.setStareVegetatie(dto.getStareVegetatie());
        entity.setSistemIntretinere(dto.getSistemIntretinere());
        entity.setSistemIrigare(dto.getSistemIrigare());
        entity.setObservatii(dto.getObservatii());
    }
}
