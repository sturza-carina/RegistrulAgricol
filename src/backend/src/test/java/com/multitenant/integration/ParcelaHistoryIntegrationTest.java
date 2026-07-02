package com.multitenant.integration;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.dto.ParcelaRevisionDto;
import com.multitenant.model.registru.Parcela;
import com.multitenant.security.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ParcelaHistoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
                "cluj",
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    private UsernamePasswordAuthenticationToken authToken(UserDetailsImpl userDetails) {
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Test
    void testParcelaModificationHistoryAndDiffCalculation() throws Exception {
        // Step 1: Query initial history of Parcela with ID 1
        MvcResult initialResult = mockMvc.perform(get("/api/parcele/1/istoric")
                        .with(authentication(authToken(clujUser()))))
                .andExpect(status().isOk())
                .andReturn();

        String initialContent = initialResult.getResponse().getContentAsString();
        List<ParcelaRevisionDto> initialHistory = objectMapper.readValue(
                initialContent,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ParcelaRevisionDto.class)
        );

        // Should have at least the ADD revision from the DatabaseSeeder run
        assertThat(initialHistory).isNotEmpty();
        int initialRevisionCount = initialHistory.size();

        // Step 2: Mutate Parcela 1 (Revision 1 of our modifications)
        Parcela updatedParcela1 = new Parcela();
        updatedParcela1.setDenumire("Parcela CJ 1 Modificata");
        updatedParcela1.setSuprafata(9.9);

        mockMvc.perform(put("/api/parcele/1")
                        .with(authentication(authToken(clujUser())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedParcela1)))
                .andExpect(status().isOk());

        // Step 3: Mutate Parcela 1 again (Revision 2 of our modifications)
        Parcela updatedParcela2 = new Parcela();
        updatedParcela2.setDenumire("Parcela CJ 1 Modificata");
        updatedParcela2.setSuprafata(9.9);
        updatedParcela2.setCategorieFolosinta("Faneata Modificata");

        mockMvc.perform(put("/api/parcele/1")
                        .with(authentication(authToken(clujUser())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedParcela2)))
                .andExpect(status().isOk());

        // Step 4: Get updated history
        MvcResult updatedResult = mockMvc.perform(get("/api/parcele/1/istoric")
                        .with(authentication(authToken(clujUser()))))
                .andExpect(status().isOk())
                .andReturn();

        String updatedContent = updatedResult.getResponse().getContentAsString();
        List<ParcelaRevisionDto> updatedHistory = objectMapper.readValue(
                updatedContent,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ParcelaRevisionDto.class)
        );

        // We expect exactly two new revisions
        assertThat(updatedHistory.size()).isEqualTo(initialRevisionCount + 2);

        // The second-to-last revision should contain denumire and suprafata changes
        ParcelaRevisionDto firstModRevision = updatedHistory.get(updatedHistory.size() - 2);
        assertThat(firstModRevision.getAuthor()).isEqualTo("cluj_user");
        assertThat(firstModRevision.getActionType()).isEqualTo("MOD");
        assertThat(firstModRevision.getDiffs()).containsKey("denumire");
        assertThat(firstModRevision.getDiffs().get("denumire").getNewValue()).isEqualTo("Parcela CJ 1 Modificata");
        assertThat(firstModRevision.getDiffs()).containsKey("suprafata");
        assertThat(firstModRevision.getDiffs().get("suprafata").getNewValue()).isEqualTo(9.9);

        // The latest revision should contain the categorieFolosinta change
        ParcelaRevisionDto secondModRevision = updatedHistory.get(updatedHistory.size() - 1);
        assertThat(secondModRevision.getAuthor()).isEqualTo("cluj_user");
        assertThat(secondModRevision.getActionType()).isEqualTo("MOD");
        assertThat(secondModRevision.getDiffs()).containsKey("categorieFolosinta");
        assertThat(secondModRevision.getDiffs().get("categorieFolosinta").getNewValue()).isEqualTo("Faneata Modificata");
        // And denumire/suprafata shouldn't be in the diff of the second modification since they didn't change relative to the first modification
        assertThat(secondModRevision.getDiffs()).doesNotContainKey("denumire");
        assertThat(secondModRevision.getDiffs()).doesNotContainKey("suprafata");
    }
}
