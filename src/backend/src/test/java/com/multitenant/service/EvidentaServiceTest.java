package com.multitenant.service;

import com.multitenant.dto.TratamentFitosanitarDTO;
import com.multitenant.dto.FertilizareDTO;
import com.multitenant.model.registru.CatalogPpp;
import com.multitenant.model.registru.CatalogIngrasaminte;
import com.multitenant.model.registru.Parcela;
import com.multitenant.model.registru.TratamentFitosanitar;
import com.multitenant.model.registru.Fertilizare;
import com.multitenant.repository.CatalogPppRepository;
import com.multitenant.repository.CatalogIngrasaminteRepository;
import com.multitenant.repository.ParcelaRepository;
import com.multitenant.repository.TratamentFitosanitarRepository;
import com.multitenant.repository.FertilizareRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class EvidentaServiceTest {

    @Mock private TratamentFitosanitarRepository fitosanitarRepository;
    @Mock private ParcelaRepository parcelaRepository;
    @Mock private CatalogPppRepository catalogPppRepository;
    @Mock private CatalogIngrasaminteRepository catalogIngrasaminteRepository;
    @Mock private FertilizareRepository fertilizareRepository;

    @InjectMocks private TratamentFitosanitarService fitosanitarService;
    @InjectMocks private FertilizareService fertilizareService;

    @Test
    void fitosanitar_calculatesTotalQuantityCorrectly() {
        Parcela parcela = new Parcela();
        parcela.setId(1L);
        parcela.setDenumire("Parcela A");

        CatalogPpp ppp = new CatalogPpp();
        ppp.setId(10L);
        ppp.setDenumireComerciala("Champ 77");
        ppp.setDozaOmologata(2.0);
        ppp.setTimpPauza(7);

        when(parcelaRepository.findById(1L)).thenReturn(Optional.of(parcela));
        when(catalogPppRepository.findById(10L)).thenReturn(Optional.of(ppp));

        TratamentFitosanitarDTO dto = new TratamentFitosanitarDTO();
        dto.setParcelaId(1L);
        dto.setCatalogPppId(10L);
        dto.setDozaUtilizata(1.5);
        dto.setSuprafataTratata(4.0);
        dto.setDataEfectuarii(LocalDateTime.now());
        dto.setFenofaza("infratire");
        dto.setResponsabil("Ion Popescu");

        TratamentFitosanitarDTO result = fitosanitarService.createTratament(dto);

        assertThat(result.getCantitateTotala()).isEqualTo(6.0); // 1.5 * 4.0
        verify(fitosanitarRepository).save(any(TratamentFitosanitar.class));
    }

    @Test
    void fitosanitar_overdoseWithoutJustification_throwsException() {
        Parcela parcela = new Parcela();
        parcela.setId(1L);

        CatalogPpp ppp = new CatalogPpp();
        ppp.setId(10L);
        ppp.setDozaOmologata(2.0);
        ppp.setTimpPauza(7);

        when(parcelaRepository.findById(1L)).thenReturn(Optional.of(parcela));
        when(catalogPppRepository.findById(10L)).thenReturn(Optional.of(ppp));

        TratamentFitosanitarDTO dto = new TratamentFitosanitarDTO();
        dto.setParcelaId(1L);
        dto.setCatalogPppId(10L);
        dto.setDozaUtilizata(2.5); // Overdose! (2.5 > 2.0)
        dto.setSuprafataTratata(4.0);
        dto.setDataEfectuarii(LocalDateTime.now());
        dto.setFenofaza("infratire");
        dto.setResponsabil("Ion Popescu");
        dto.setJustificareSupradozaj(null); // No justification

        assertThatThrownBy(() -> fitosanitarService.createTratament(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("depășește doza omologată");
    }

    @Test
    void fitosanitar_overdoseWithJustification_savesAndAudits() {
        Parcela parcela = new Parcela();
        parcela.setId(1L);

        CatalogPpp ppp = new CatalogPpp();
        ppp.setId(10L);
        ppp.setDozaOmologata(2.0);
        ppp.setTimpPauza(7);

        when(parcelaRepository.findById(1L)).thenReturn(Optional.of(parcela));
        when(catalogPppRepository.findById(10L)).thenReturn(Optional.of(ppp));

        TratamentFitosanitarDTO dto = new TratamentFitosanitarDTO();
        dto.setParcelaId(1L);
        dto.setCatalogPppId(10L);
        dto.setDozaUtilizata(2.5); // Overdose!
        dto.setSuprafataTratata(4.0);
        dto.setDataEfectuarii(LocalDateTime.now());
        dto.setFenofaza("infratire");
        dto.setResponsabil("Ion Popescu");
        dto.setJustificareSupradozaj("Infestare masiva de daunatori");

        TratamentFitosanitarDTO result = fitosanitarService.createTratament(dto);

        assertThat(result.getDozaDepasita()).isTrue();
        assertThat(result.getJustificareSupradozaj()).isEqualTo("Infestare masiva de daunatori");
        verify(fitosanitarRepository).save(any(TratamentFitosanitar.class));
    }

    @Test
    void fitosanitar_safetyPauseViolation_throwsException() {
        Parcela parcela = new Parcela();
        parcela.setId(1L);

        CatalogPpp ppp = new CatalogPpp();
        ppp.setId(10L);
        ppp.setDozaOmologata(2.0);
        ppp.setTimpPauza(7); // Requires 7 days safety gap

        when(parcelaRepository.findById(1L)).thenReturn(Optional.of(parcela));
        when(catalogPppRepository.findById(10L)).thenReturn(Optional.of(ppp));

        TratamentFitosanitarDTO dto = new TratamentFitosanitarDTO();
        dto.setParcelaId(1L);
        dto.setCatalogPppId(10L);
        dto.setDozaUtilizata(1.5);
        dto.setSuprafataTratata(4.0);
        dto.setDataEfectuarii(LocalDateTime.of(2026, 7, 1, 10, 0));
        dto.setFenofaza("infratire");
        dto.setResponsabil("Ion Popescu");
        dto.setDataIncepereRecoltare(LocalDate.of(2026, 7, 5)); // Safety violation: only 4 days after!

        assertThatThrownBy(() -> fitosanitarService.createTratament(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nerespectare Timp de Pauză ANF");
    }

    @Test
    void fertilizare_calculatesActiveSubstancesCorrectly() {
        Parcela parcela = new Parcela();
        parcela.setId(1L);

        CatalogIngrasaminte ing = new CatalogIngrasaminte();
        ing.setId(20L);
        ing.setTip("Chimic");
        ing.setProcentAzot(15.0);
        ing.setProcentFosfor(10.0);
        ing.setProcentPotasiu(20.0);

        when(parcelaRepository.findById(1L)).thenReturn(Optional.of(parcela));
        when(catalogIngrasaminteRepository.findById(20L)).thenReturn(Optional.of(ing));

        FertilizareDTO dto = new FertilizareDTO();
        dto.setParcelaId(1L);
        dto.setCatalogIngrasaminteId(20L);
        dto.setDataAplicarii(LocalDate.of(2026, 7, 10));
        dto.setCantitateBruta(2.0);
        dto.setUnitateMasura("tone/ha"); // scales *1000 => 2000 kg/ha applied

        FertilizareDTO result = fertilizareService.createFertilizare(dto, false);

        assertThat(result.getAportAzot()).isEqualTo(300.0); // 2000 * 0.15
        assertThat(result.getAportFosfor()).isEqualTo(200.0); // 2000 * 0.10
        assertThat(result.getAportPotasiu()).isEqualTo(400.0); // 2000 * 0.20
        verify(fertilizareRepository).save(any(Fertilizare.class));
    }

    @Test
    void fertilizare_winterBlackoutWithoutForce_throwsException() {
        Parcela parcela = new Parcela();
        parcela.setId(1L);

        CatalogIngrasaminte ing = new CatalogIngrasaminte();
        ing.setId(20L);
        ing.setTip("Chimic");

        when(parcelaRepository.findById(1L)).thenReturn(Optional.of(parcela));
        when(catalogIngrasaminteRepository.findById(20L)).thenReturn(Optional.of(ing));

        FertilizareDTO dto = new FertilizareDTO();
        dto.setParcelaId(1L);
        dto.setCatalogIngrasaminteId(20L);
        dto.setDataAplicarii(LocalDate.of(2026, 12, 10)); // Winter! (Nov 15 - Mar 15)
        dto.setCantitateBruta(100.0);
        dto.setUnitateMasura("kg/ha");

        assertThatThrownBy(() -> fertilizareService.createFertilizare(dto, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Avertisment Perioadă de Interdicție");
    }

    @Test
    void fertilizare_exceedsNitratesDirectiveLimit_throwsException() {
        Parcela parcela = new Parcela();
        parcela.setId(1L);

        CatalogIngrasaminte ing = new CatalogIngrasaminte();
        ing.setId(20L);
        ing.setTip("Organic"); // Nitrate limit applies to organic N!
        ing.setProcentAzot(1.5);

        when(parcelaRepository.findById(1L)).thenReturn(Optional.of(parcela));
        when(catalogIngrasaminteRepository.findById(20L)).thenReturn(Optional.of(ing));
        
        // Mock current N sum in calendar year as 100 kg N/ha
        when(fertilizareRepository.sumAportAzotByParcelaAndDateRange(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(100.0);

        FertilizareDTO dto = new FertilizareDTO();
        dto.setParcelaId(1L);
        dto.setCatalogIngrasaminteId(20L);
        dto.setDataAplicarii(LocalDate.of(2026, 7, 10));
        dto.setCantitateBruta(6.0);
        dto.setUnitateMasura("tone/ha"); // active N = 6000 * 0.015 = 90 kg N/ha. Total = 100 + 90 = 190 kg N/ha (> 170!)

        assertThatThrownBy(() -> fertilizareService.createFertilizare(dto, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Limita Directivei Nitraților Depășită");
    }
}
