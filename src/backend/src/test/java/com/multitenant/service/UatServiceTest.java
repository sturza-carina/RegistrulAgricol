package com.multitenant.service;

import com.multitenant.model.core.PublicUat;
import com.multitenant.model.core.Uat;
import com.multitenant.repository.PublicUatRepository;
import com.multitenant.repository.UatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class UatServiceTest {

    @Mock
    private UatRepository uatRepository;

    @Mock
    private PublicUatRepository publicUatRepository;

    @InjectMocks
    private UatService uatService;

    // ── createPublicUat ──────────────────────────────────────────────────────

    @Test
    void createPublicUat_success() {
        PublicUat uat = buildPublicUat("12345", "Cluj-Napoca", "Cluj", "Municipiu");
        when(publicUatRepository.existsByCodSiruta("12345")).thenReturn(false);
        when(publicUatRepository.save(uat)).thenReturn(uat);

        PublicUat result = uatService.createPublicUat(uat);

        assertThat(result.getCodSiruta()).isEqualTo("12345");
        verify(publicUatRepository).save(uat);
    }

    @Test
    void createPublicUat_missingCodSiruta_throwsBadRequest() {
        PublicUat uat = buildPublicUat(null, "Cluj-Napoca", "Cluj", "Municipiu");

        assertThatThrownBy(() -> uatService.createPublicUat(uat))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("codSiruta is required");
    }

    @Test
    void createPublicUat_blankCodSiruta_throwsBadRequest() {
        PublicUat uat = buildPublicUat("   ", "Cluj-Napoca", "Cluj", "Municipiu");

        assertThatThrownBy(() -> uatService.createPublicUat(uat))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("codSiruta is required");
    }

    @Test
    void createPublicUat_duplicateCodSiruta_throwsConflict() {
        PublicUat uat = buildPublicUat("12345", "Cluj-Napoca", "Cluj", "Municipiu");
        when(publicUatRepository.existsByCodSiruta("12345")).thenReturn(true);

        assertThatThrownBy(() -> uatService.createPublicUat(uat))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createPublicUat_setsIsActiveTrue_whenNull() {
        PublicUat uat = buildPublicUat("12345", "Cluj-Napoca", "Cluj", "Municipiu");
        uat.setIsActive(null);
        when(publicUatRepository.existsByCodSiruta("12345")).thenReturn(false);
        when(publicUatRepository.save(uat)).thenReturn(uat);

        uatService.createPublicUat(uat);

        assertThat(uat.getIsActive()).isTrue();
    }

    // ── getAllPublicUats ──────────────────────────────────────────────────────

    @Test
    void getAllPublicUats_returnsAll() {
        List<PublicUat> list = List.of(
                buildPublicUat("1", "A", "X", "Municipiu"),
                buildPublicUat("2", "B", "Y", "Comună")
        );
        when(publicUatRepository.findByTenantIdIsNull()).thenReturn(list);

        List<PublicUat> result = uatService.getAllPublicUats();

        assertThat(result).hasSize(2);
        verify(publicUatRepository).findByTenantIdIsNull();
    }

    // ── updatePublicUat ──────────────────────────────────────────────────────

    @Test
    void updatePublicUat_success_updatesFields() {
        PublicUat existing = buildPublicUat("12345", "Cluj-Napoca", "Cluj", "Municipiu");
        PublicUat request = new PublicUat();
        request.setDenumire("Cluj-Napoca Updated");
        request.setJudet("Cluj Updated");
        request.setTipUat("Oraș");
        request.setIsActive(false);

        when(publicUatRepository.findByCodSiruta("12345")).thenReturn(Optional.of(existing));
        when(publicUatRepository.save(existing)).thenReturn(existing);

        PublicUat result = uatService.updatePublicUat("12345", request);

        assertThat(result.getDenumire()).isEqualTo("Cluj-Napoca Updated");
        assertThat(result.getJudet()).isEqualTo("Cluj Updated");
        assertThat(result.getTipUat()).isEqualTo("Oraș");
        assertThat(result.getIsActive()).isFalse();
    }

    @Test
    void updatePublicUat_partialUpdate_onlyChangesProvidedFields() {
        PublicUat existing = buildPublicUat("12345", "Cluj-Napoca", "Cluj", "Municipiu");
        PublicUat request = new PublicUat();
        request.setDenumire("Nou Nume");

        when(publicUatRepository.findByCodSiruta("12345")).thenReturn(Optional.of(existing));
        when(publicUatRepository.save(existing)).thenReturn(existing);

        PublicUat result = uatService.updatePublicUat("12345", request);

        assertThat(result.getDenumire()).isEqualTo("Nou Nume");
        assertThat(result.getJudet()).isEqualTo("Cluj");       // neschimbat
        assertThat(result.getTipUat()).isEqualTo("Municipiu"); // neschimbat
    }

    @Test
    void updatePublicUat_notFound_throwsNotFound() {
        when(publicUatRepository.findByCodSiruta("99999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> uatService.updatePublicUat("99999", new PublicUat()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not found");
    }

    // ── deletePublicUat ──────────────────────────────────────────────────────

    @Test
    void deletePublicUat_success() {
        PublicUat uat = buildPublicUat("12345", "Cluj-Napoca", "Cluj", "Municipiu");
        when(publicUatRepository.findByCodSiruta("12345")).thenReturn(Optional.of(uat));

        uatService.deletePublicUat("12345");

        verify(publicUatRepository).delete(uat);
    }

    @Test
    void deletePublicUat_notFound_throwsNotFound() {
        when(publicUatRepository.findByCodSiruta("99999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> uatService.deletePublicUat("99999"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not found");
    }

    // ── assignUatToTenant ────────────────────────────────────────────────────

    @Test
    void assignUatToTenant_success_copiesFromPublic() {
        PublicUat globalUat = buildPublicUat("12345", "Cluj-Napoca", "Cluj", "Municipiu");
        when(publicUatRepository.findByCodSiruta("12345")).thenReturn(Optional.of(globalUat));
        when(uatRepository.save(any(Uat.class))).thenAnswer(inv -> inv.getArgument(0));

        Uat result = uatService.assignUatToTenant("12345");

        assertThat(result.getCodSiruta()).isEqualTo("12345");
        assertThat(result.getDenumire()).isEqualTo("Cluj-Napoca");
        verify(uatRepository).save(any(Uat.class));
    }

    @Test
    void assignUatToTenant_notInGlobalRegistry_throwsNotFound() {
        when(publicUatRepository.findByCodSiruta("99999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> uatService.assignUatToTenant("99999"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not found in the global registry");
    }

    @Test
    void assignUatToTenant_alreadyAssigned_throwsConflict() {
        PublicUat globalUat = buildPublicUat("12345", "Cluj-Napoca", "Cluj", "Municipiu");
        globalUat.setTenantId("cluj");
        when(publicUatRepository.findByCodSiruta("12345")).thenReturn(Optional.of(globalUat));

        try (var mockedTenantContext = mockStatic(com.multitenant.config.tenant.TenantContext.class)) {
            mockedTenantContext.when(com.multitenant.config.tenant.TenantContext::getCurrentTenant).thenReturn("cluj");
            
            assertThatThrownBy(() -> uatService.assignUatToTenant("12345"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("already assigned");
        }
    }

    // ── removeUatFromTenant ──────────────────────────────────────────────────

    @Test
    void removeUatFromTenant_success() {
        Uat tenantUat = buildTenantUat("12345", "Cluj-Napoca", "Cluj", "Municipiu");
        when(uatRepository.findByCodSiruta("12345")).thenReturn(Optional.of(tenantUat));

        uatService.removeUatFromTenant("12345");

        verify(uatRepository).delete(tenantUat);
    }

    @Test
    void removeUatFromTenant_notAssigned_throwsNotFound() {
        when(uatRepository.findByCodSiruta("99999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> uatService.removeUatFromTenant("99999"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not assigned to this tenant");
    }

    // ── getTenantUats ─────────────────────────────────────────────────────────

    @Test
    void getTenantUats_returnsAll() {
        List<Uat> list = List.of(buildTenantUat("1", "A", "X", "Municipiu"));
        when(uatRepository.findAll()).thenReturn(list);

        List<Uat> result = uatService.getTenantUats();

        assertThat(result).hasSize(1);
        verify(uatRepository).findAll();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private PublicUat buildPublicUat(String codSiruta, String denumire, String judet, String tipUat) {
        PublicUat uat = new PublicUat();
        uat.setId(1L);
        uat.setCodSiruta(codSiruta);
        uat.setDenumire(denumire);
        uat.setJudet(judet);
        uat.setTipUat(tipUat);
        uat.setIsActive(true);
        return uat;
    }

    private Uat buildTenantUat(String codSiruta, String denumire, String judet, String tipUat) {
        Uat uat = new Uat();
        uat.setId(1L);
        uat.setCodSiruta(codSiruta);
        uat.setDenumire(denumire);
        uat.setJudet(judet);
        uat.setTipUat(tipUat);
        uat.setIsActive(true);
        return uat;
    }
}
