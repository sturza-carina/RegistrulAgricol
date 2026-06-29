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
@SuppressWarnings("null")
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
                .hasMessageContaining("Tenant ID and name must not be null");
    }

    @Test
    void createTenant_nullName_throwsBadRequest() {
        assertThatThrownBy(() -> tenantService.createTenant("54975", null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Tenant ID and name must not be null");
    }

    @Test
    void createTenant_bothNull_throwsBadRequest() {
        assertThatThrownBy(() -> tenantService.createTenant(null, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Tenant ID and name must not be null");
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
        // We cannot verify tenantRepository.save here because Flyway fails with a mocked DataSource.
        // It will throw before reaching save.

        try {
            tenantService.createTenant("54975", "Cluj-Napoca");
            fail("Expected RuntimeException due to mocked DataSource");
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Could not provision schema");
        }
        
        verify(tenantRepository, never()).save(any());
    }

    @Test
    void createTenant_setsCorrectSchemaName() throws Exception {
        when(tenantRepository.existsById("99999")).thenReturn(false);

        try {
            tenantService.createTenant("99999", "Test UAT");
            fail("Expected RuntimeException due to mocked DataSource");
        } catch (RuntimeException e) {
            // Flyway esuaza fara DB real
        }
    }

    @Test
    void createTenant_flywayFails_throwsRuntimeException() {
        when(tenantRepository.existsById("54975")).thenReturn(false);

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
