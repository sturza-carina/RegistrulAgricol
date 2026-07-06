package com.multitenant.service;

import com.multitenant.dto.TratamentFitosanitarDTO;
import com.multitenant.model.registru.CatalogPpp;
import com.multitenant.model.registru.Parcela;
import com.multitenant.model.registru.TratamentFitosanitar;
import com.multitenant.repository.CatalogPppRepository;
import com.multitenant.repository.ParcelaRepository;
import com.multitenant.repository.TratamentFitosanitarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TratamentFitosanitarService {

    private final TratamentFitosanitarRepository tratamentFitosanitarRepository;
    private final ParcelaRepository parcelaRepository;
    private final CatalogPppRepository catalogPppRepository;

    @Transactional(readOnly = true)
    public Page<TratamentFitosanitarDTO> getTratamente(Long parcelaId, Pageable pageable) {
        Page<TratamentFitosanitar> page;
        if (parcelaId != null) {
            page = tratamentFitosanitarRepository.findByParcela_Id(parcelaId, pageable);
        } else {
            page = tratamentFitosanitarRepository.findAll(pageable);
        }
        return page.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public TratamentFitosanitarDTO getTratamentById(Long id) {
        TratamentFitosanitar entity = tratamentFitosanitarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tratamentul fitosanitar nu a fost găsit."));
        return mapToDto(entity);
    }

    @Transactional
    public TratamentFitosanitarDTO createTratament(TratamentFitosanitarDTO dto) {
        TratamentFitosanitar entity = new TratamentFitosanitar();
        saveOrUpdate(entity, dto);
        return mapToDto(entity);
    }

    @Transactional
    public TratamentFitosanitarDTO updateTratament(Long id, TratamentFitosanitarDTO dto) {
        TratamentFitosanitar entity = tratamentFitosanitarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tratamentul fitosanitar nu a fost găsit."));
        saveOrUpdate(entity, dto);
        return mapToDto(entity);
    }

    @Transactional
    public void deleteTratament(Long id) {
        if (!tratamentFitosanitarRepository.existsById(id)) {
            throw new RuntimeException("Tratamentul fitosanitar nu a fost găsit.");
        }
        tratamentFitosanitarRepository.deleteById(id);
    }

    private void saveOrUpdate(TratamentFitosanitar entity, TratamentFitosanitarDTO dto) {
        Parcela parcela = parcelaRepository.findById(dto.getParcelaId())
                .orElseThrow(() -> new RuntimeException("Parcela specificată nu există."));
        CatalogPpp ppp = catalogPppRepository.findById(dto.getCatalogPppId())
                .orElseThrow(() -> new RuntimeException("Produsul PPP selectat nu există în catalog."));

        // Doza validation & overdosage check
        boolean isOverdosed = dto.getDozaUtilizata() > ppp.getDozaOmologata();
        if (isOverdosed) {
            if (dto.getJustificareSupradozaj() == null || dto.getJustificareSupradozaj().trim().isEmpty()) {
                throw new IllegalArgumentException("Doza utilizată (" + dto.getDozaUtilizata() + ") depășește doza omologată (" 
                        + ppp.getDozaOmologata() + "). Vă rugăm să specificați o justificare pentru a înregistra acest tratament.");
            }
            entity.setDozaDepasita(true);
            entity.setJustificareSupradozaj(dto.getJustificareSupradozaj());
        } else {
            entity.setDozaDepasita(false);
            entity.setJustificareSupradozaj(null);
        }

        // Pause time validation
        if (dto.getDataIncepereRecoltare() != null) {
            LocalDate minHarvestDate = dto.getDataEfectuarii().toLocalDate().plusDays(ppp.getTimpPauza());
            if (dto.getDataIncepereRecoltare().isBefore(minHarvestDate)) {
                throw new IllegalArgumentException("Nerespectare Timp de Pauză ANF! Data recoltării nu poate fi mai devreme decât " 
                        + minHarvestDate + " (timp de pauză necesar: " + ppp.getTimpPauza() + " zile pentru " + ppp.getDenumireComerciala() + ").");
            }
        }

        entity.setDataEfectuarii(dto.getDataEfectuarii());
        entity.setFenofaza(dto.getFenofaza());
        entity.setParcela(parcela);
        entity.setAgentDaunator(dto.getAgentDaunator());
        entity.setCatalogPpp(ppp);
        entity.setDozaUtilizata(dto.getDozaUtilizata());
        entity.setSuprafataTratata(dto.getSuprafataTratata());
        
        // Auto calculate total quantity
        entity.setCantitateTotala(dto.getSuprafataTratata() * dto.getDozaUtilizata());
        
        entity.setResponsabil(dto.getResponsabil());
        entity.setSemnaturaElectronica(dto.getSemnaturaElectronica());
        entity.setDataIncepereRecoltare(dto.getDataIncepereRecoltare());
        entity.setDocumentDareConsum(dto.getDocumentDareConsum());

        tratamentFitosanitarRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<TratamentFitosanitarDTO> getAllTratamente() {
        return tratamentFitosanitarRepository.findAllWithRelations().stream()
                .map(this::mapToDto)
                .toList();
    }

    public TratamentFitosanitarDTO mapToDto(TratamentFitosanitar entity) {
        TratamentFitosanitarDTO dto = new TratamentFitosanitarDTO();
        dto.setId(entity.getId());
        dto.setDataEfectuarii(entity.getDataEfectuarii());
        dto.setFenofaza(entity.getFenofaza());
        
        if (entity.getParcela() != null) {
            dto.setParcelaId(entity.getParcela().getId());
            dto.setParcelaDenumire(entity.getParcela().getDenumire());
        }
        
        dto.setAgentDaunator(entity.getAgentDaunator());
        
        if (entity.getCatalogPpp() != null) {
            dto.setCatalogPppId(entity.getCatalogPpp().getId());
            dto.setCatalogPppDenumire(entity.getCatalogPpp().getDenumireComerciala());
            dto.setCatalogPppDozaOmologata(entity.getCatalogPpp().getDozaOmologata());
            dto.setCatalogPppTimpPauza(entity.getCatalogPpp().getTimpPauza());
        }
        
        dto.setDozaUtilizata(entity.getDozaUtilizata());
        dto.setSuprafataTratata(entity.getSuprafataTratata());
        dto.setCantitateTotala(entity.getCantitateTotala());
        dto.setResponsabil(entity.getResponsabil());
        dto.setSemnaturaElectronica(entity.getSemnaturaElectronica());
        dto.setDataIncepereRecoltare(entity.getDataIncepereRecoltare());
        dto.setDocumentDareConsum(entity.getDocumentDareConsum());
        dto.setDozaDepasita(entity.getDozaDepasita());
        dto.setJustificareSupradozaj(entity.getJustificareSupradozaj());
        
        return dto;
    }
}
