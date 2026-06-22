package com.multitenant.service;

import com.multitenant.model.core.Tenant;
import com.multitenant.repository.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private DataSource dataSource;

    @InjectMocks
    private TenantService tenantService;

    // createTenant
    @Test
    void createTenant_nullSirutaCode_throwsBadRequest() {
        assertThatThrownBy(() -> tenantService.createTenant(null, "Cluj-Napoca"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("SIRUTA code and name must not be null");
    }

    @Test
    void createTenant_nullName_throwsBadRequest() {
        assertThatThrownBy(() -> tenantService.createTenant("54975", null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("SIRUTA code and name must not be null");
    }

    @Test
    void createTenant_bothNull_throwsBadRequest() {
        assertThatThrownBy(() -> tenantService.createTenant(null, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("SIRUTA code and name must not be null");
    }

    @Test
    void createTenant_duplicateSirutaCode_throwsConflict() {
        when(tenantRepository.existsById("54975")).thenReturn(true);

        assertThatThrownBy(() -> tenantService.createTenant("54975", "Cluj-Napoca"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createTenant_success_savesCorrectTenant() {
        when(tenantRepository.existsById("54975")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));

        try {
            tenantService.createTenant("54975", "Cluj-Napoca");
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Could not provision schema");
        }

        verify(tenantRepository).save(argThat(tenant ->
                "54975".equals(tenant.getId()) &&
                        "Cluj-Napoca".equals(tenant.getName()) &&
                        "uat_54975".equals(tenant.getSchemaName())
        ));
    }

    @Test
    void createTenant_setsCorrectSchemaName() throws Exception {
        when(tenantRepository.existsById("99999")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));

        try {
            tenantService.createTenant("99999", "Test UAT");
        } catch (RuntimeException e) {
            // Flyway esuaza fara DB real
        }

        verify(tenantRepository).save(argThat(tenant ->
                "uat_99999".equals(tenant.getSchemaName())
        ));
    }

    @Test
    void createTenant_flywayFails_throwsRuntimeException() {
        when(tenantRepository.existsById("54975")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));

        // DataSource arunca exceptie -> Flyway esueaza
        try {
            when(dataSource.getConnection()).thenThrow(new java.sql.SQLException("Connection failed"));
        } catch (Exception e) {
            fail("Mock setup failed");
        }

        assertThatThrownBy(() -> tenantService.createTenant("54975", "Cluj-Napoca"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Could not provision schema");
    }

    // migrateAllTenants
    @Test
    void migrateAllTenants_noTenants_doesNothing() {
        when(tenantRepository.findAll()).thenReturn(List.of());

        // Nu arunca exceptie, nu apeleaza DataSource
        tenantService.migrateAllTenants();

        verify(tenantRepository).findAll();
        verifyNoInteractions(dataSource);
    }

    @Test
    void migrateAllTenants_flywayFailsForOneTenant_continuesForRest() throws Exception {
        Tenant t1 = buildTenant("54975", "Cluj-Napoca", "uat_54975");
        Tenant t2 = buildTenant("1017", "Bucuresti", "uat_1017");
        when(tenantRepository.findAll()).thenReturn(List.of(t1, t2));

        // DataSource arunca exceptie pentru orice tenant
        when(dataSource.getConnection()).thenThrow(new java.sql.SQLException("Connection failed"));

        // Nu arunca exceptie — eroarea e prinsa intern si logged
        assertThatNoException().isThrownBy(() -> tenantService.migrateAllTenants());
    }

    // helper
    private Tenant buildTenant(String id, String name, String schemaName) {
        Tenant tenant = new Tenant();
        tenant.setId(id);
        tenant.setName(name);
        tenant.setSchemaName(schemaName);
        return tenant;
    }
}
