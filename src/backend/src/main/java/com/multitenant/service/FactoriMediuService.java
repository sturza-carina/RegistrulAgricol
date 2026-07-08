package com.multitenant.service;

import com.multitenant.dto.FactoriMediuDTO;
import com.multitenant.model.registru.FactoriMediu;
import com.multitenant.model.registru.Parcela;
import com.multitenant.model.registru.TipMediu;
import com.multitenant.repository.FactoriMediuRepository;
import com.multitenant.repository.ParcelaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class FactoriMediuService {

    private final FactoriMediuRepository factoriMediuRepository;
    private final ParcelaRepository parcelaRepository;

    @Transactional(readOnly = true)
    public Page<FactoriMediuDTO> getReadingsByParcela(Long parcelaId, Pageable pageable) {
        return factoriMediuRepository.findByParcela_Id(parcelaId, pageable).map(this::mapToDto);
    }

    @Transactional
    public FactoriMediuDTO createReading(FactoriMediuDTO dto) {
        FactoriMediu entity = new FactoriMediu();
        saveOrUpdate(entity, dto);
        return mapToDto(entity);
    }

    @Transactional
    public void deleteReading(Long id) {
        if (!factoriMediuRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Înregistrarea nu există.");
        }
        factoriMediuRepository.deleteById(id);
    }

    private void saveOrUpdate(FactoriMediu entity, FactoriMediuDTO dto) {
        Parcela parcela = parcelaRepository.findById(dto.getParcelaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parcela nu există."));

        if (parcela.getTipMediu() == TipMediu.CAMP_DESCHIS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Înregistrarea factorilor de mediu se poate face doar pentru sere sau solarii.");
        }

        entity.setParcela(parcela);
        entity.setTemperatura(dto.getTemperatura());
        entity.setUmiditateRelativa(dto.getUmiditateRelativa());
        entity.setDataInregistrare(dto.getDataInregistrare() != null ? dto.getDataInregistrare() : java.time.LocalDateTime.now());

        factoriMediuRepository.save(entity);
    }

    public FactoriMediuDTO mapToDto(FactoriMediu entity) {
        FactoriMediuDTO dto = new FactoriMediuDTO();
        dto.setId(entity.getId());
        if (entity.getParcela() != null) {
            dto.setParcelaId(entity.getParcela().getId());
            dto.setParcelaDenumire(entity.getParcela().getDenumire());
        }
        dto.setTemperatura(entity.getTemperatura());
        dto.setUmiditateRelativa(entity.getUmiditateRelativa());
        dto.setDataInregistrare(entity.getDataInregistrare());
        return dto;
    }
}
