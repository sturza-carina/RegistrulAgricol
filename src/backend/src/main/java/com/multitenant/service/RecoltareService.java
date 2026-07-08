package com.multitenant.service;

import com.multitenant.dto.RecoltareDTO;
import com.multitenant.dto.CentralizatorRecoltareDto;
import com.multitenant.model.registru.*;
import com.multitenant.repository.CicluProductieRepository;
import com.multitenant.repository.ParcelaRepository;
import com.multitenant.repository.RecoltareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecoltareService {

    private final RecoltareRepository recoltareRepository;
    private final ParcelaRepository parcelaRepository;
    private final CicluProductieRepository cicluProductieRepository;

    @Transactional(readOnly = true)
    public Page<RecoltareDTO> getRecoltariByParcela(Long parcelaId, Pageable pageable) {
        return recoltareRepository.findByParcela_Id(parcelaId, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public RecoltareDTO getRecoltareById(Long id) {
        Recoltare entity = recoltareRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recoltarea nu a fost găsită."));
        return mapToDto(entity);
    }

    @Transactional
    public RecoltareDTO createRecoltare(RecoltareDTO dto) {
        Recoltare entity = new Recoltare();
        saveOrUpdate(entity, dto);
        return mapToDto(entity);
    }

    @Transactional
    public RecoltareDTO updateRecoltare(Long id, RecoltareDTO dto) {
        Recoltare entity = recoltareRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recoltarea nu a fost găsită."));
        saveOrUpdate(entity, dto);
        return mapToDto(entity);
    }

    @Transactional
    public void deleteRecoltare(Long id) {
        if (!recoltareRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recoltarea nu a fost găsită.");
        }
        recoltareRepository.deleteById(id);
    }

    private void saveOrUpdate(Recoltare entity, RecoltareDTO dto) {
        Parcela parcela = parcelaRepository.findById(dto.getParcelaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parcela specificată nu există."));

        LocalDate date = dto.getDataRecoltare();
        List<CicluProductie> activeCycles = cicluProductieRepository.findActiveCyclesOnDate(parcela.getId(), date);
        CicluProductie activeCycle = activeCycles.isEmpty() ? null : activeCycles.get(0);

        if (parcela.getTipMediu() == TipMediu.SOLAR || parcela.getTipMediu() == TipMediu.SERA_INCALZITA) {
            // Mandatory cycle association for greenhouse/solarium
            if (activeCycle == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Eroare: În spații protejate (Solar/Seră), recoltările trebuie obligatoriu asociate unui ciclu de producție activ în ziua recoltării.");
            }
            entity.setCicluProductie(activeCycle);
            entity.setCultura(activeCycle.getCultura()); // override crop with cycle's crop
        } else {
            // Optional cycle association for open field
            entity.setCicluProductie(activeCycle);
            entity.setCultura(dto.getCultura() != null ? dto.getCultura() : (activeCycle != null ? activeCycle.getCultura() : ""));
        }

        entity.setParcela(parcela);
        entity.setDataRecoltare(date);
        entity.setCantitateKg(dto.getCantitateKg());

        recoltareRepository.save(entity);
    }

    public RecoltareDTO mapToDto(Recoltare entity) {
        RecoltareDTO dto = new RecoltareDTO();
        dto.setId(entity.getId());
        if (entity.getParcela() != null) {
            dto.setParcelaId(entity.getParcela().getId());
            dto.setParcelaDenumire(entity.getParcela().getDenumire());
        }
        if (entity.getCicluProductie() != null) {
            dto.setCicluProductieId(entity.getCicluProductie().getId());
            dto.setCicluProductieCultura(entity.getCicluProductie().getCultura());
        }
        dto.setCultura(entity.getCultura());
        dto.setDataRecoltare(entity.getDataRecoltare());
        dto.setCantitateKg(entity.getCantitateKg());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<CentralizatorRecoltareDto> getCentralizatorRecoltari(Integer anAgricol) {
        int targetYear = anAgricol != null ? anAgricol : LocalDate.now().getYear();
        List<Recoltare> harvests = recoltareRepository.findAllByAnAgricol(targetYear);

        // Group harvests by normalized culture name and parcel tipMediu
        Map<String, Map<TipMediu, List<Recoltare>>> grouped = harvests.stream()
                .filter(h -> h.getCultura() != null && !h.getCultura().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        h -> {
                            String c = h.getCultura().trim();
                            return c.substring(0, 1).toUpperCase() + c.substring(1).toLowerCase();
                        },
                        Collectors.groupingBy(h -> h.getParcela().getTipMediu())
                ));

        List<CentralizatorRecoltareDto> result = new java.util.ArrayList<>();

        for (Map.Entry<String, Map<TipMediu, List<Recoltare>>> cultureEntry : grouped.entrySet()) {
            String cultura = cultureEntry.getKey();
            for (Map.Entry<TipMediu, List<Recoltare>> mediuEntry : cultureEntry.getValue().entrySet()) {
                TipMediu mediu = mediuEntry.getKey();
                List<Recoltare> recList = mediuEntry.getValue();

                double totalKg = recList.stream().mapToDouble(Recoltare::getCantitateKg).sum();

                // Compute unique parcels surface area involved
                double totalMp = 0.0;
                Set<Long> uniqueParcelIds = new java.util.HashSet<>();
                for (Recoltare r : recList) {
                    Parcela p = r.getParcela();
                    if (p != null && !uniqueParcelIds.contains(p.getId())) {
                        uniqueParcelIds.add(p.getId());
                        if (mediu == TipMediu.CAMP_DESCHIS) {
                            totalMp += p.getSuprafata() * 10000.0; // ha to mp
                        } else {
                            totalMp += p.getSuprafataUtilaMp() != null ? p.getSuprafataUtilaMp() : 0.0;
                        }
                    }
                }

                double randament = totalMp > 0 ? totalKg / totalMp : 0.0;

                result.add(new CentralizatorRecoltareDto(
                        cultura,
                        mediu.name(),
                        totalKg,
                        totalMp,
                        randament
                ));
            }
        }

        return result;
    }
}
