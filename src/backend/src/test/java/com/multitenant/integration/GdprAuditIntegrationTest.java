package com.multitenant.integration;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.model.audit.GdprActionType;
import com.multitenant.model.audit.GdprAuditLog;
import com.multitenant.repository.GdprAuditLogRepository;
import com.multitenant.security.UserDetailsImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GdprAuditIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GdprAuditLogRepository gdprAuditLogRepository;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant("cluj");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private UserDetailsImpl clujUser() {
        return new UserDetailsImpl(
                2L,
                "cluj_user",
                "password123",
                "ROLE_USER",
                "cluj", 1L, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    private UsernamePasswordAuthenticationToken authToken(UserDetailsImpl userDetails) {
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Test
    void testGetPersonById_GeneratesViewAuditLog() throws Exception {
        // Since database seeder runs on startup, person with ID 1 should exist in cluj tenant
        mockMvc.perform(get("/api/persons/1")
                        .with(authentication(authToken(clujUser()))))
                .andExpect(status().isOk());

        // Now query the repository inside "cluj" context
        TenantContext.setCurrentTenant("cluj");
        List<GdprAuditLog> logs = gdprAuditLogRepository.findAll();
        assertThat(logs).isNotEmpty();

        GdprAuditLog latestLog = logs.stream()
                .filter(log -> "/api/persons/1".equals(log.getEndpoint()))
                .findFirst()
                .orElse(null);

        assertThat(latestLog).isNotNull();
        assertThat(latestLog.getUtilizator()).isEqualTo("cluj_user");
        assertThat(latestLog.getTipActiune()).isEqualTo(GdprActionType.VIEW);
        assertThat(latestLog.getEntitateVizata()).isEqualTo("Persoana");
        assertThat(latestLog.getIdPersoanaVizata()).isEqualTo("1");
        assertThat(latestLog.getEndpoint()).isEqualTo("/api/persons/1");
        assertThat(latestLog.getTenantId()).isEqualTo("cluj");
    }

    @Test
    void testGetAllPersons_GeneratesListAuditLogWithIds() throws Exception {
        mockMvc.perform(get("/api/persons")
                        .with(authentication(authToken(clujUser()))))
                .andExpect(status().isOk());

        // Check if logs are written with formatted ids list like [1, 2, ...]
        TenantContext.setCurrentTenant("cluj");
        List<GdprAuditLog> logs = gdprAuditLogRepository.findAll();
        assertThat(logs).isNotEmpty();

        GdprAuditLog latestLog = logs.stream()
                .filter(log -> "/api/persons".equals(log.getEndpoint()))
                .findFirst()
                .orElse(null);

        assertThat(latestLog).isNotNull();
        assertThat(latestLog.getUtilizator()).isEqualTo("cluj_user");
        assertThat(latestLog.getTipActiune()).isEqualTo(GdprActionType.VIEW);
        assertThat(latestLog.getEntitateVizata()).isEqualTo("Persoana");
        // Check that the returned IDs are formatted as a list
        assertThat(latestLog.getIdPersoanaVizata()).startsWith("[").endsWith("]");
    }
}
