package com.multitenant.service;

import com.multitenant.dto.CicluProductieDTO;
import com.multitenant.model.registru.CicluProductie;
import com.multitenant.model.registru.CicluStatus;
import com.multitenant.model.registru.Parcela;
import com.multitenant.model.registru.TipMediu;
import com.multitenant.repository.CicluProductieRepository;
import com.multitenant.repository.ParcelaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CicluProductieService {

    private final CicluProductieRepository cicluProductieRepository;
    private final ParcelaRepository parcelaRepository;
    private final PdfGeneratorService pdfGeneratorService;

    @Transactional(readOnly = true)
    public Page<CicluProductieDTO> getCicluriByParcela(Long parcelaId, Pageable pageable) {
        return cicluProductieRepository.findByParcela_Id(parcelaId, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public CicluProductieDTO getCicluById(Long id) {
        CicluProductie entity = cicluProductieRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ciclul de producție nu a fost găsit."));
        return mapToDto(entity);
    }

    @Transactional
    public CicluProductieDTO createCiclu(CicluProductieDTO dto) {
        CicluProductie entity = new CicluProductie();
        saveOrUpdate(entity, dto);
        CicluProductieDTO savedDto = mapToDto(entity);
        // Attach warning check
        checkSoftWarning(dto, savedDto);
        return savedDto;
    }

    @Transactional
    public CicluProductieDTO updateCiclu(Long id, CicluProductieDTO dto) {
        CicluProductie entity = cicluProductieRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ciclul de producție nu a fost găsit."));
        saveOrUpdate(entity, dto);
        CicluProductieDTO savedDto = mapToDto(entity);
        checkSoftWarning(dto, savedDto);
        return savedDto;
    }

    @Transactional
    public void deleteCiclu(Long id) {
        if (!cicluProductieRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ciclul de producție nu a fost găsit.");
        }
        cicluProductieRepository.deleteById(id);
    }

    private void saveOrUpdate(CicluProductie entity, CicluProductieDTO dto) {
        Parcela parcela = parcelaRepository.findById(dto.getParcelaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parcela specificată nu există."));

        // Validation 1: Cycle overlap check
        if (dto.getStatus() == CicluStatus.ACTIV) {
            Optional<CicluProductie> activeCycle = cicluProductieRepository.findFirstByParcela_IdAndStatus(parcela.getId(), CicluStatus.ACTIV);
            if (activeCycle.isPresent() && (entity.getId() == null || !activeCycle.get().getId().equals(entity.getId()))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Există deja un ciclu de producție activ pe această parcelă. Finalizați sau defrișați ciclul activ înainte de a începe unul nou.");
            }
        }

        entity.setParcela(parcela);
        entity.setCultura(dto.getCultura());
        entity.setDataInfiintare(dto.getDataInfiintare());
        entity.setDataDefisare(dto.getDataDefisare());
        entity.setStatus(dto.getStatus());
        entity.setProgramSprijin(dto.isProgramSprijin());

        if (dto.getStatus() == CicluStatus.FINALIZAT && entity.getDataDefisare() == null) {
            entity.setDataDefisare(java.time.LocalDate.now());
        }

        cicluProductieRepository.save(entity);
    }

    private void checkSoftWarning(CicluProductieDTO requestDto, CicluProductieDTO responseDto) {
        Parcela parcela = parcelaRepository.findById(responseDto.getParcelaId()).orElse(null);
        if (parcela != null && (parcela.getTipMediu() == TipMediu.SOLAR || parcela.getTipMediu() == TipMediu.SERA_INCALZITA)) {
            boolean isSupportProgram = responseDto.isProgramSprijin() || 
                    (responseDto.getCultura() != null && responseDto.getCultura().toLowerCase().contains("tomate"));
            
            if (isSupportProgram) {
                double suprafataMp = parcela.getSuprafataUtilaMp() != null ? parcela.getSuprafataUtilaMp() : 0.0;
                if (suprafataMp < 1000.0) {
                    responseDto.setWarning("Warning: Suprafața utilă a parcelei (" + suprafataMp + 
                            " mp) este sub pragul minim legal de 1000 mp pentru programul de sprijin guvernamental (Tomate/Legume).");
                }
            }
        }
    }

    public CicluProductieDTO mapToDto(CicluProductie entity) {
        CicluProductieDTO dto = new CicluProductieDTO();
        dto.setId(entity.getId());
        if (entity.getParcela() != null) {
            dto.setParcelaId(entity.getParcela().getId());
            dto.setParcelaDenumire(entity.getParcela().getDenumire());
        }
        dto.setCultura(entity.getCultura());
        dto.setDataInfiintare(entity.getDataInfiintare());
        dto.setDataDefisare(entity.getDataDefisare());
        dto.setStatus(entity.getStatus());
        dto.setProgramSprijin(entity.isProgramSprijin());
        return dto;
    }

    @Transactional(readOnly = true)
    public byte[] exportPdf(Integer anAgricol) {
        int targetYear = anAgricol != null ? anAgricol : LocalDate.now().getYear();
        List<Parcela> allParcele = parcelaRepository.findAll();
        List<Parcela> protectedSpaces = allParcele.stream()
                .filter(p -> p.getTipMediu() == TipMediu.SOLAR || p.getTipMediu() == TipMediu.SERA_INCALZITA)
                .toList();

        java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy");

        List<Map<String, Object>> formattedParcele = new java.util.ArrayList<>();
        for (Parcela p : protectedSpaces) {
            List<CicluProductie> cycles = cicluProductieRepository.findByParcela_Id(p.getId()).stream()
                    .filter(c -> c.getDataInfiintare().getYear() <= targetYear && 
                            (c.getDataDefisare() == null || c.getDataDefisare().getYear() >= targetYear))
                    .toList();

            List<Map<String, Object>> formattedCycles = cycles.stream().map(c -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("cultura", c.getCultura());
                map.put("dataInfiintare", c.getDataInfiintare().format(df));
                map.put("dataDefrișare", c.getDataDefisare() != null ? c.getDataDefisare().format(df) : "Activ");
                map.put("status", c.getStatus().name());
                return map;
            }).toList();

            Map<String, Object> pMap = new java.util.HashMap<>();
            pMap.put("denumire", p.getDenumire());
            pMap.put("tipMediu", p.getTipMediu().name());
            pMap.put("suprafataUtilaMp", p.getSuprafataUtilaMp() != null ? p.getSuprafataUtilaMp() : 0.0);
            pMap.put("cicluri", formattedCycles);
            formattedParcele.add(pMap);
        }

        Map<String, Object> variables = new java.util.HashMap<>();
        variables.put("parcele", formattedParcele);
        variables.put("anAgricol", targetYear);

        return pdfGeneratorService.generatePdfFromHtml("registru-spatii-protejate", variables);
    }
}
