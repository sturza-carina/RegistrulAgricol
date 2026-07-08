package com.multitenant.service;

import com.multitenant.dto.PadureDTO;
import com.multitenant.model.registru.Padure;
import com.multitenant.model.registru.Parcela;
import com.multitenant.repository.PadureRepository;
import com.multitenant.repository.ParcelaRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PadureService {

    private final PadureRepository padureRepository;
    private final ParcelaRepository parcelaRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public Page<PadureDTO> getPaduriByParcela(Long parcelaId, Pageable pageable) {
        return padureRepository.findByParcela_Id(parcelaId, pageable)
                .map(this::convertToDTO);
    }

    @Transactional
    public PadureDTO create(Long parcelaId, PadureDTO dto) {
        Parcela parcela = parcelaRepository.findById(parcelaId)
                .orElseThrow(() -> new RuntimeException("Parcela not found"));

        Padure entity = convertToEntity(dto);
        entity.setParcela(parcela);

        Padure saved = padureRepository.save(entity);
        return convertToDTO(saved);
    }

    @Transactional
    public PadureDTO update(Long parcelaId, Long id, PadureDTO dto) {
        Padure entity = padureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Padure not found"));

        if (!entity.getParcela().getId().equals(parcelaId)) {
            throw new RuntimeException("Padure does not belong to the specified parcela");
        }

        entity.setTipVegetatie(dto.getTipVegetatie());
        entity.setSpeciePredominanta(dto.getSpeciePredominanta());
        entity.setSuprafataHa(dto.getSuprafataHa());
        entity.setAnPlantare(dto.getAnPlantare());
        entity.setStareVegetatie(dto.getStareVegetatie());
        entity.setObservatii(dto.getObservatii());

        Padure saved = padureRepository.save(entity);
        return convertToDTO(saved);
    }

    @Transactional
    public void delete(Long parcelaId, Long id) {
        Padure entity = padureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Padure not found"));

        if (!entity.getParcela().getId().equals(parcelaId)) {
            throw new RuntimeException("Padure does not belong to the specified parcela");
        }

        padureRepository.delete(entity);
    }

    private PadureDTO convertToDTO(Padure entity) {
        PadureDTO dto = modelMapper.map(entity, PadureDTO.class);
        dto.setParcelaId(entity.getParcelaId());
        return dto;
    }

    private Padure convertToEntity(PadureDTO dto) {
        return modelMapper.map(dto, Padure.class);
    }
}
