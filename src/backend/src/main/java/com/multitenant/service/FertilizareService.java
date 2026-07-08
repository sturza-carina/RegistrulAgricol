package com.multitenant.service;

import com.multitenant.dto.FertilizareDTO;
import com.multitenant.model.registru.*;
import com.multitenant.repository.CatalogIngrasaminteRepository;
import com.multitenant.repository.ParcelaRepository;
import com.multitenant.repository.FertilizareRepository;
import com.multitenant.repository.CicluProductieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FertilizareService {

    private final FertilizareRepository fertilizareRepository;
    private final ParcelaRepository parcelaRepository;
    private final CatalogIngrasaminteRepository catalogIngrasaminteRepository;
    private final CicluProductieRepository cicluProductieRepository;

    @Transactional(readOnly = true)
    public Page<FertilizareDTO> getFertilizari(Long parcelaId, Pageable pageable) {
        Page<Fertilizare> page;
        if (parcelaId != null) {
            page = fertilizareRepository.findByParcela_Id(parcelaId, pageable);
        } else {
            page = fertilizareRepository.findAll(pageable);
        }
        return page.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public FertilizareDTO getFertilizareById(Long id) {
        Fertilizare entity = fertilizareRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Înregistrarea de fertilizare nu a fost găsită."));
        return mapToDto(entity);
    }

    @Transactional
    public FertilizareDTO createFertilizare(FertilizareDTO dto, boolean confirmInterdictie) {
        Fertilizare entity = new Fertilizare();
        saveOrUpdate(entity, dto, confirmInterdictie);
        return mapToDto(entity);
    }

    @Transactional
    public FertilizareDTO updateFertilizare(Long id, FertilizareDTO dto, boolean confirmInterdictie) {
        Fertilizare entity = fertilizareRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Înregistrarea de fertilizare nu a fost găsită."));
        saveOrUpdate(entity, dto, confirmInterdictie);
        return mapToDto(entity);
    }

    @Transactional
    public void deleteFertilizare(Long id) {
        if (!fertilizareRepository.existsById(id)) {
            throw new RuntimeException("Înregistrarea de fertilizare nu a fost găsită.");
        }
        fertilizareRepository.deleteById(id);
    }

    private void saveOrUpdate(Fertilizare entity, FertilizareDTO dto, boolean confirmInterdictie) {
        Parcela parcela = parcelaRepository.findById(dto.getParcelaId())
                .orElseThrow(() -> new RuntimeException("Parcela specificată nu există."));
        CatalogIngrasaminte ingrasamant = catalogIngrasaminteRepository.findById(dto.getCatalogIngrasaminteId())
                .orElseThrow(() -> new RuntimeException("Îngrășământul specificat nu există în catalog."));

        LocalDate date = dto.getDataAplicarii();
        
        // Auto link active cycle
        List<CicluProductie> activeCycles = cicluProductieRepository.findActiveCyclesOnDate(parcela.getId(), date);
        CicluProductie activeCycle = activeCycles.isEmpty() ? null : activeCycles.get(0);

        if (parcela.getTipMediu() == TipMediu.SOLAR || parcela.getTipMediu() == TipMediu.SERA_INCALZITA) {
            if (activeCycle == null) {
                throw new IllegalArgumentException("În spații protejate (Solar/Seră), fertilizările trebuie obligatoriu asociate unui ciclu de producție activ la data aplicării.");
            }
            entity.setCicluProductie(activeCycle);
        } else {
            entity.setCicluProductie(activeCycle);
        }

        // Check winter interdiction period (November 15 - March 15)
        if (isInWinterInterdictionPeriod(date) && !confirmInterdictie) {
            throw new IllegalArgumentException("Avertisment Perioadă de Interdicție! Data selectată (" + date 
                    + ") se află în perioada de interdicție din timpul iernii (15 noiembrie - 15 martie). Doriți să continuați?");
        }

        // Active substance calculation
        // unitateMasura can be "kg/ha" or "tone/ha" / "t/ha"
        double factor = 1.0;
        if (dto.getUnitateMasura() != null && (dto.getUnitateMasura().equalsIgnoreCase("tone/ha") 
                || dto.getUnitateMasura().equalsIgnoreCase("t/ha") || dto.getUnitateMasura().equalsIgnoreCase("to/ha"))) {
            factor = 1000.0;
        }
        
        double nitrogenActive = dto.getCantitateBruta() * factor * (ingrasamant.getProcentAzot() / 100.0);
        double phosphorusActive = dto.getCantitateBruta() * factor * (ingrasamant.getProcentFosfor() / 100.0);
        double potassiumActive = dto.getCantitateBruta() * factor * (ingrasamant.getProcentPotasiu() / 100.0);

        // Nitrate Directive check (170 kg N/ha/year limit for organic fertilizers)
        if (ingrasamant.getTip().equalsIgnoreCase("Organic")) {
            int year = date.getYear();
            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year, 12, 31);
            Double sumExisting = fertilizareRepository.sumAportAzotByParcelaAndDateRange(parcela.getId(), startDate, endDate);
            
            // Exclude current record's previous active nitrogen if updating
            if (entity.getId() != null) {
                sumExisting -= entity.getAportAzot();
            }
            
            double totalNitrogen = sumExisting + nitrogenActive;
            if (totalNitrogen > 170.0) {
                throw new IllegalArgumentException("Eroare Critică: Limita Directivei Nitraților Depășită! Totalul cumulat de Azot organic pe parcela '" 
                        + parcela.getDenumire() + "' în anul " + year + " ar fi de " + String.format("%.2f", totalNitrogen) 
                        + " kg N/ha, depășind limita legală maximă de 170.00 kg N/ha/an.");
            }
        }

        entity.setDataAplicarii(date);
        entity.setParcela(parcela);
        entity.setCatalogIngrasaminte(ingrasamant);
        entity.setCantitateBruta(dto.getCantitateBruta());
        entity.setUnitateMasura(dto.getUnitateMasura());
        entity.setAportAzot(nitrogenActive);
        entity.setAportFosfor(phosphorusActive);
        entity.setAportPotasiu(potassiumActive);

        fertilizareRepository.save(entity);
    }

    private boolean isInWinterInterdictionPeriod(LocalDate date) {
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        if (month == 11 && day >= 15) return true;
        if (month == 12 || month == 1 || month == 2) return true;
        if (month == 3 && day <= 15) return true;
        return false;
    }

    public FertilizareDTO mapToDto(Fertilizare entity) {
        FertilizareDTO dto = new FertilizareDTO();
        dto.setId(entity.getId());
        dto.setDataAplicarii(entity.getDataAplicarii());
        
        if (entity.getParcela() != null) {
            dto.setParcelaId(entity.getParcela().getId());
            dto.setParcelaDenumire(entity.getParcela().getDenumire());
        }
        
        if (entity.getCatalogIngrasaminte() != null) {
            dto.setCatalogIngrasaminteId(entity.getCatalogIngrasaminte().getId());
            dto.setCatalogIngrasaminteDenumire(entity.getCatalogIngrasaminte().getDenumire());
            dto.setCatalogIngrasaminteTip(entity.getCatalogIngrasaminte().getTip());
            dto.setProcentAzot(entity.getCatalogIngrasaminte().getProcentAzot());
            dto.setProcentFosfor(entity.getCatalogIngrasaminte().getProcentFosfor());
            dto.setProcentPotasiu(entity.getCatalogIngrasaminte().getProcentPotasiu());
        }
        
        dto.setCantitateBruta(entity.getCantitateBruta());
        dto.setUnitateMasura(entity.getUnitateMasura());
        dto.setAportAzot(entity.getAportAzot());
        dto.setAportFosfor(entity.getAportFosfor());
        dto.setAportPotasiu(entity.getAportPotasiu());
        
        if (entity.getCicluProductie() != null) {
            dto.setCicluProductieId(entity.getCicluProductie().getId());
            dto.setCicluProductieCultura(entity.getCicluProductie().getCultura());
        }
        
        return dto;
    }
}
