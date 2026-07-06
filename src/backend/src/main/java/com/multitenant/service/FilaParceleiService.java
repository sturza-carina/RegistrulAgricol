package com.multitenant.service;

import com.multitenant.dto.FilaParceleiDTO;
import com.multitenant.model.registru.TratamentFitosanitar;
import com.multitenant.model.registru.Fertilizare;
import com.multitenant.repository.TratamentFitosanitarRepository;
import com.multitenant.repository.FertilizareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilaParceleiService {

    private final TratamentFitosanitarRepository tratamentFitosanitarRepository;
    private final FertilizareRepository fertilizareRepository;

    @Transactional(readOnly = true)
    public List<FilaParceleiDTO> getFilaParcelei(Long parcelaId, Integer anAgricol) {
        List<FilaParceleiDTO> list = new ArrayList<>();

        // Fetch fitosanitare for this parcel
        List<TratamentFitosanitar> tratamente = tratamentFitosanitarRepository.findByParcelaIdWithRelations(parcelaId);
        for (TratamentFitosanitar t : tratamente) {
            if (anAgricol != null && t.getDataEfectuarii().getYear() != anAgricol) {
                continue;
            }
            FilaParceleiDTO f = new FilaParceleiDTO();
            f.setData(t.getDataEfectuarii().toLocalDate());
            f.setTipInterventie("Pesticid (PPP)");
            f.setDenumireProdus(t.getCatalogPpp() != null ? t.getCatalogPpp().getDenumireComerciala() : "Necunoscut");
            f.setTipProdus(t.getCatalogPpp() != null ? t.getCatalogPpp().getTip() : "-");
            f.setDozaCantitate(t.getDozaUtilizata());
            f.setUnitateMasura("L/ha sau kg/ha");
            f.setSuprafataTratata(t.getSuprafataTratata());
            f.setDetaliiTinta("Dăunător vizat: " + t.getAgentDaunator());
            f.setResponsabil(t.getResponsabil());
            f.setStatusAlerta(t.getDozaDepasita() ? "Supradozaj: " + t.getJustificareSupradozaj() : "Doza conformă");
            list.add(f);
        }

        // Fetch fertilizari for this parcel
        List<Fertilizare> fertilizari = fertilizareRepository.findByParcelaIdWithRelations(parcelaId);
        for (Fertilizare fert : fertilizari) {
            if (anAgricol != null && fert.getDataAplicarii().getYear() != anAgricol) {
                continue;
            }
            FilaParceleiDTO f = new FilaParceleiDTO();
            f.setData(fert.getDataAplicarii());
            f.setTipInterventie("Fertilizare");
            f.setDenumireProdus(fert.getCatalogIngrasaminte() != null ? fert.getCatalogIngrasaminte().getDenumire() : "Necunoscut");
            f.setTipProdus(fert.getCatalogIngrasaminte() != null ? fert.getCatalogIngrasaminte().getTip() : "-");
            f.setDozaCantitate(fert.getCantitateBruta());
            f.setUnitateMasura(fert.getUnitateMasura());
            f.setSuprafataTratata(null);
            f.setDetaliiTinta(String.format("Aport active: N: %.1f, P: %.1f, K: %.1f kg/ha", 
                    fert.getAportAzot(), fert.getAportFosfor(), fert.getAportPotasiu()));
            f.setResponsabil("-");
            f.setStatusAlerta("N-P-K calculat");
            list.add(f);
        }

        // Sort chronologically by date descending (most recent first)
        list.sort(Comparator.comparing(FilaParceleiDTO::getData).reversed());
        return list;
    }
}
