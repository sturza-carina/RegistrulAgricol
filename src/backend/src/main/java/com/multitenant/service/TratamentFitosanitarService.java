package com.multitenant.service;

import com.multitenant.dto.TratamentFitosanitarDTO;
import com.multitenant.model.registru.*;
import com.multitenant.repository.CatalogPppRepository;
import com.multitenant.repository.ParcelaRepository;
import com.multitenant.repository.TratamentFitosanitarRepository;
import com.multitenant.repository.CicluProductieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TratamentFitosanitarService {

    private final TratamentFitosanitarRepository tratamentFitosanitarRepository;
    private final ParcelaRepository parcelaRepository;
    private final CatalogPppRepository catalogPppRepository;
    private final CicluProductieRepository cicluProductieRepository;

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

        LocalDate date = dto.getDataEfectuarii().toLocalDate();
        List<CicluProductie> activeCycles = cicluProductieRepository.findActiveCyclesOnDate(parcela.getId(), date);
        CicluProductie activeCycle = activeCycles.isEmpty() ? null : activeCycles.get(0);

        if (parcela.getTipMediu() == TipMediu.SOLAR || parcela.getTipMediu() == TipMediu.SERA_INCALZITA) {
            if (activeCycle == null) {
                throw new IllegalArgumentException("În spații protejate (Solar/Seră), tratamentele trebuie obligatoriu asociate unui ciclu de producție activ la data efectuării.");
            }
            entity.setCicluProductie(activeCycle);
        } else {
            entity.setCicluProductie(activeCycle);
        }

        boolean isBiological = ppp.getTip().equalsIgnoreCase("Combatere Biologică / Polenizatori") 
                || ppp.getTip().equalsIgnoreCase("Combatere Biologica / Polenizatori")
                || ppp.getTip().toLowerCase().contains("biologic");

        if (isBiological) {
            entity.setDozaDepasita(false);
            entity.setJustificareSupradozaj(null);
        } else {
            Double dozaUtilizataLHa = convertToLitersPerHectare(dto.getDozaUtilizata(), dto.getUnitateMasuraDoza());
            boolean isOverdosed = dozaUtilizataLHa > ppp.getDozaOmologata();
            if (isOverdosed) {
                if (dto.getJustificareSupradozaj() == null || dto.getJustificareSupradozaj().trim().isEmpty()) {
                    throw new IllegalArgumentException("Doza utilizată (" + dto.getDozaUtilizata() + " " + (dto.getUnitateMasuraDoza() != null ? dto.getUnitateMasuraDoza() : "L/ha") 
                            + ", echivalentă cu " + String.format("%.3f", dozaUtilizataLHa) + " L/ha) depășește doza omologată (" 
                            + ppp.getDozaOmologata() + " L/ha). Vă rugăm să specificați o justificare pentru a înregistra acest tratament.");
                }
                entity.setDozaDepasita(true);
                entity.setJustificareSupradozaj(dto.getJustificareSupradozaj());
            } else {
                entity.setDozaDepasita(false);
                entity.setJustificareSupradozaj(null);
            }
        }

        // Pause time validation
        if (!isBiological && dto.getDataIncepereRecoltare() != null) {
            LocalDate minHarvestDate = date.plusDays(ppp.getTimpPauza());
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
        entity.setCantitateTotala(dto.getSuprafataTratata() * dto.getDozaUtilizata());
        entity.setResponsabil(dto.getResponsabil());
        entity.setSemnaturaElectronica(dto.getSemnaturaElectronica());
        entity.setDataIncepereRecoltare(dto.getDataIncepereRecoltare());
        entity.setDocumentDareConsum(dto.getDocumentDareConsum());
        entity.setUnitateMasuraDoza(dto.getUnitateMasuraDoza());

        if (isBiological) {
            entity.setDataLansarii(dto.getDataLansarii() != null ? dto.getDataLansarii() : date);
            entity.setNumarCutiiIndivizi(dto.getNumarCutiiIndivizi());
        } else {
            entity.setDataLansarii(null);
            entity.setNumarCutiiIndivizi(null);
        }

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
        
        if (entity.getCicluProductie() != null) {
            dto.setCicluProductieId(entity.getCicluProductie().getId());
            dto.setCicluProductieCultura(entity.getCicluProductie().getCultura());
        }
        dto.setUnitateMasuraDoza(entity.getUnitateMasuraDoza());
        dto.setDataLansarii(entity.getDataLansarii());
        dto.setNumarCutiiIndivizi(entity.getNumarCutiiIndivizi());
        
        return dto;
    }

    private Double convertToLitersPerHectare(Double doza, String unitate) {
        if (doza == null) return 0.0;
        if (unitate == null || unitate.trim().isEmpty() || unitate.equalsIgnoreCase("L/ha") || unitate.equalsIgnoreCase("kg/ha")) {
            return doza;
        }
        String u = unitate.toLowerCase().trim();
        if (u.contains("ml/10l") || u.contains("g/10l") || u.contains("ml/10 l") || u.contains("g/10 l")) {
            return doza / 10.0;
        }
        if (u.contains("ml/1000mp") || u.contains("g/1000mp") || u.contains("ml/1000 mp") || u.contains("g/1000 mp")) {
            return doza / 100.0;
        }
        if (u.contains("l/1000mp") || u.contains("kg/1000mp") || u.contains("l/1000 mp") || u.contains("kg/1000 mp")) {
            return doza * 10.0;
        }
        return doza;
    }
}
