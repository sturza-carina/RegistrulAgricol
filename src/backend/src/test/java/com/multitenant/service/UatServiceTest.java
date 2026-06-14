package com.multitenant.service;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.model.core.Tenant;
import com.multitenant.model.core.Uat;
import com.multitenant.repository.TenantRepository;
import com.multitenant.repository.UatRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
class UatServiceTest {

    @Mock
    private UatRepository uatRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private UatService uatService;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant("public");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // createUat
    @Test
    void createUat_success() {
        Uat uat = buildUat("12345", "Cluj-Napoca", "Cluj", "Municipiu");
        when(uatRepository.existsByCodSiruta("12345")).thenReturn(false);
        when(uatRepository.save(uat)).thenReturn(uat);

        Uat result = uatService.createUat(uat);

        assertThat(result.getCodSiruta()).isEqualTo("12345");
        verify(uatRepository).save(uat);
    }

    @Test
    void createUat_missingCodSiruta_throwsBadRequest() {
        Uat uat = buildUat(null, "Cluj-Napoca", "Cluj", "Municipiu");

        assertThatThrownBy(() -> uatService.createUat(uat))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("codSiruta is required");
    }

    @Test
    void createUat_blankCodSiruta_throwsBadRequest() {
        Uat uat = buildUat("   ", "Cluj-Napoca", "Cluj", "Municipiu");

        assertThatThrownBy(() -> uatService.createUat(uat))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("codSiruta is required");
    }

    @Test
    void createUat_duplicateCodSiruta_throwsConflict() {
        Uat uat = buildUat("12345", "Cluj-Napoca", "Cluj", "Municipiu");
        when(uatRepository.existsByCodSiruta("12345")).thenReturn(true);

        assertThatThrownBy(() -> uatService.createUat(uat))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createUat_setsIsActiveTrue_whenNull() {
        Uat uat = buildUat("12345", "Cluj-Napoca", "Cluj", "Municipiu");
        uat.setIsActive(null);
        when(uatRepository.existsByCodSiruta("12345")).thenReturn(false);
        when(uatRepository.save(uat)).thenReturn(uat);

        uatService.createUat(uat);

        assertThat(uat.getIsActive()).isTrue();
    }

    @Test
    void createUat_withTenantContext_setsTenant() {
        TenantContext.setCurrentTenant("cluj");
        Uat uat = buildUat("12345", "Cluj-Napoca", "Cluj", "Municipiu");
        Tenant tenant = new Tenant();
        tenant.setId("cluj");

        when(uatRepository.existsByCodSiruta("12345")).thenReturn(false);
        when(tenantRepository.findById("cluj")).thenReturn(Optional.of(tenant));
        when(uatRepository.save(uat)).thenReturn(uat);

        uatService.createUat(uat);

        assertThat(uat.getTenant()).isEqualTo(tenant);
    }

    @Test
    void createUat_withTenantContext_tenantNotFound_throwsNotFound() {
        TenantContext.setCurrentTenant("inexistent");
        Uat uat = buildUat("12345", "Cluj-Napoca", "Cluj", "Municipiu");

        when(uatRepository.existsByCodSiruta("12345")).thenReturn(false);
        when(tenantRepository.findById("inexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> uatService.createUat(uat))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Tenant not found");
    }

    // getAllUats
    @Test
    void getAllUats_publicContext_returnsAll() {
        TenantContext.setCurrentTenant("public");
        List<Uat> list = List.of(buildUat("1", "A", "X", "Municipiu"), buildUat("2", "B", "Y", "Comună"));
        when(uatRepository.findAll()).thenReturn(list);

        List<Uat> result = uatService.getAllUats();

        assertThat(result).hasSize(2);
        verify(uatRepository).findAll();
        verify(uatRepository, never()).findByTenant_Id(any());
    }

    @Test
    void getAllUats_tenantContext_returnsByTenant() {
        TenantContext.setCurrentTenant("cluj");
        List<Uat> list = List.of(buildUat("54975", "Cluj-Napoca", "Cluj", "Municipiu"));
        when(uatRepository.findByTenant_Id("cluj")).thenReturn(list);

        List<Uat> result = uatService.getAllUats();

        assertThat(result).hasSize(1);
        verify(uatRepository).findByTenant_Id("cluj");
        verify(uatRepository, never()).findAll();
    }

    // getUatByCodSiruta
    @Test
    void getUatByCodSiruta_found_returnsUat() {
        Uat uat = buildUat("12345", "Cluj-Napoca", "Cluj", "Municipiu");
        when(uatRepository.findByCodSiruta("12345")).thenReturn(Optional.of(uat));

        Uat result = uatService.getUatByCodSiruta("12345");

        assertThat(result.getCodSiruta()).isEqualTo("12345");
    }

    @Test
    void getUatByCodSiruta_notFound_throwsNotFound() {
        when(uatRepository.findByCodSiruta("99999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> uatService.getUatByCodSiruta("99999"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void getUatByCodSiruta_nullCodSiruta_throwsBadRequest() {
        assertThatThrownBy(() -> uatService.getUatByCodSiruta(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("codSiruta must not be null");
    }

    // updateUat
    @Test
    void updateUat_success_updatesFields() {
        Uat existing = buildUat("12345", "Cluj-Napoca", "Cluj", "Municipiu");
        Uat request = new Uat();
        request.setDenumire("Cluj-Napoca Updated");
        request.setJudet("Cluj Updated");
        request.setTipUat("Oraș");
        request.setIsActive(false);

        when(uatRepository.findByCodSiruta("12345")).thenReturn(Optional.of(existing));
        when(uatRepository.save(existing)).thenReturn(existing);

        Uat result = uatService.updateUat("12345", request);

        assertThat(result.getDenumire()).isEqualTo("Cluj-Napoca Updated");
        assertThat(result.getJudet()).isEqualTo("Cluj Updated");
        assertThat(result.getTipUat()).isEqualTo("Oraș");
        assertThat(result.getIsActive()).isFalse();
    }

    @Test
    void updateUat_partialUpdate_onlyChangesProvidedFields() {
        Uat existing = buildUat("12345", "Cluj-Napoca", "Cluj", "Municipiu");
        Uat request = new Uat();
        request.setDenumire("Nou Nume");
        // judet, tipUat, isActive sunt null — nu trebuie modificate

        when(uatRepository.findByCodSiruta("12345")).thenReturn(Optional.of(existing));
        when(uatRepository.save(existing)).thenReturn(existing);

        Uat result = uatService.updateUat("12345", request);

        assertThat(result.getDenumire()).isEqualTo("Nou Nume");
        assertThat(result.getJudet()).isEqualTo("Cluj");       // neschimbat
        assertThat(result.getTipUat()).isEqualTo("Municipiu"); // neschimbat
    }

    @Test
    void updateUat_notFound_throwsNotFound() {
        when(uatRepository.findByCodSiruta("99999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> uatService.updateUat("99999", new Uat()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not found");
    }

    // deleteUat
    @Test
    void deleteUat_success() {
        Uat uat = buildUat("12345", "Cluj-Napoca", "Cluj", "Municipiu");
        when(uatRepository.findByCodSiruta("12345")).thenReturn(Optional.of(uat));

        uatService.deleteUat("12345");

        verify(uatRepository).delete(uat);
    }

    @Test
    void deleteUat_notFound_throwsNotFound() {
        when(uatRepository.findByCodSiruta("99999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> uatService.deleteUat("99999"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not found");
    }

    // helper
    private Uat buildUat(String codSiruta, String denumire, String judet, String tipUat) {
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
