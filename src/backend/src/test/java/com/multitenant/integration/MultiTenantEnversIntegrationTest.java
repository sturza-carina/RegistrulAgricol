package com.multitenant.integration;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.model.registru.Parcela;
import com.multitenant.security.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MultiTenantEnversIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    private UserDetailsImpl bucurestiUser() {
        return new UserDetailsImpl(
                4L,
                "buc_admin",
                "password123",
                "ROLE_USER",
                "bucuresti", 1L, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    private UsernamePasswordAuthenticationToken authToken(UserDetailsImpl userDetails) {
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Test
    void testStrictMultiTenantIsolationOfEnversAuditing() throws Exception {
        // Step 1: Update Parcela 1 inside "cluj" context
        Parcela updatedParcela = new Parcela();
        updatedParcela.setDenumire("Cluj Unique Parcela Name");
        updatedParcela.setSuprafata(5.5);

        mockMvc.perform(put("/api/parcele/1")
                        .with(authentication(authToken(clujUser())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedParcela)))
                .andExpect(status().isOk());

        // Step 2: Get history of Parcela 1 inside "cluj" context
        String clujHistoryJson = mockMvc.perform(get("/api/parcele/1/istoric")
                        .with(authentication(authToken(clujUser()))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(clujHistoryJson).contains("Cluj Unique Parcela Name");

        // Step 3: Attempt to fetch history of ID 1 inside "bucuresti" context
        // In "bucuresti" schema, ID 1 is a different Parcela (with its own separate history)
        String bucurestiHistoryJson = mockMvc.perform(get("/api/parcele/1/istoric")
                        .with(authentication(authToken(bucurestiUser()))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // The "bucuresti" history for Parcela 1 should NOT contain the unique Cluj name
        assertThat(bucurestiHistoryJson).doesNotContain("Cluj Unique Parcela Name");
    }
}
