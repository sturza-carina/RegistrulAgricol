package com.multitenant.service;

import com.multitenant.dto.VitaDeVieDTO;
import com.multitenant.model.registru.Parcela;
import com.multitenant.model.registru.TipInregistrareVita;
import com.multitenant.model.registru.VitaDeVie;
import com.multitenant.repository.ParcelaRepository;
import com.multitenant.repository.VitaDeVieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VitaDeVieService {

    private final VitaDeVieRepository vitaDeVieRepository;
    private final ParcelaRepository parcelaRepository;

    @Transactional(readOnly = true)
    public Page<VitaDeVieDTO> getVitaDeVieByParcelaId(Long parcelaId, Pageable pageable) {
        return vitaDeVieRepository.findByParcela_Id(parcelaId, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<VitaDeVieDTO> getVitaDeVieByParcelaIdAndTip(Long parcelaId, TipInregistrareVita tip, Pageable pageable) {
        return vitaDeVieRepository.findByParcela_IdAndTipInregistrare(parcelaId, tip, pageable)
                .map(this::mapToDto);
    }

    @Transactional
    public VitaDeVieDTO addVitaToParcela(Long parcelaId, VitaDeVieDTO dto) {
        Parcela parcela = parcelaRepository.findById(parcelaId)
                .orElseThrow(() -> new RuntimeException("Parcela not found with id: " + parcelaId));

        VitaDeVie vita = new VitaDeVie();
        mapToEntity(dto, vita);
        vita.setParcela(parcela);

        VitaDeVie saved = vitaDeVieRepository.save(vita);
        return mapToDto(saved);
    }

    @Transactional
    public VitaDeVieDTO updateVita(Long parcelaId, Long vitaId, VitaDeVieDTO dto) {
        VitaDeVie vita = vitaDeVieRepository.findById(vitaId)
                .orElseThrow(() -> new RuntimeException("Înregistrarea nu a fost găsită cu id: " + vitaId));

        if (!vita.getParcela().getId().equals(parcelaId)) {
            throw new RuntimeException("Înregistrarea nu aparține parcelei specificate");
        }

        mapToEntity(dto, vita);
        VitaDeVie updated = vitaDeVieRepository.save(vita);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteVita(Long parcelaId, Long vitaId) {
        VitaDeVie vita = vitaDeVieRepository.findById(vitaId)
                .orElseThrow(() -> new RuntimeException("Înregistrarea nu a fost găsită cu id: " + vitaId));

        if (!vita.getParcela().getId().equals(parcelaId)) {
            throw new RuntimeException("Înregistrarea nu aparține parcelei specificate");
        }

        vitaDeVieRepository.delete(vita);
    }

    private VitaDeVieDTO mapToDto(VitaDeVie entity) {
        VitaDeVieDTO dto = new VitaDeVieDTO();
        dto.setId(entity.getId());
        dto.setTipInregistrare(entity.getTipInregistrare());
        dto.setSpecie(entity.getSpecie());
        dto.setSoi(entity.getSoi());
        dto.setAnPlantare(entity.getAnPlantare());
        dto.setNumarVite(entity.getNumarVite());
        dto.setSuprafataHa(entity.getSuprafataHa());
        dto.setDensitateViteHa(entity.getDensitateViteHa());
        dto.setStareVita(entity.getStareVita());
        dto.setSistemIntretinere(entity.getSistemIntretinere());
        dto.setSistemIrigare(entity.getSistemIrigare());
        dto.setProductieEstimataKg(entity.getProductieEstimataKg());
        dto.setObservatii(entity.getObservatii());
        dto.setParcelaId(entity.getParcelaId());
        return dto;
    }

    private void mapToEntity(VitaDeVieDTO dto, VitaDeVie entity) {
        entity.setTipInregistrare(dto.getTipInregistrare());
        entity.setSpecie(dto.getSpecie());
        entity.setSoi(dto.getSoi());
        entity.setAnPlantare(dto.getAnPlantare());
        entity.setNumarVite(dto.getNumarVite());
        entity.setSuprafataHa(dto.getSuprafataHa());
        entity.setDensitateViteHa(dto.getDensitateViteHa());
        entity.setStareVita(dto.getStareVita());
        entity.setSistemIntretinere(dto.getSistemIntretinere());
        entity.setSistemIrigare(dto.getSistemIrigare());
        entity.setProductieEstimataKg(dto.getProductieEstimataKg());
        entity.setObservatii(dto.getObservatii());
    }
}
