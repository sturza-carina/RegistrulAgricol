package com.multitenant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multitenant.config.tenant.TenantFilter;
import com.multitenant.model.core.Uat;
import com.multitenant.security.JwtAuthenticationFilter;
import com.multitenant.security.SecurityConfig;
import com.multitenant.service.UatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.BeforeEach;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

@WebMvcTest(UatController.class)
@Import(SecurityConfig.class)
@SuppressWarnings("null")
class UatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UatService uatService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private TenantFilter tenantFilter;

    @BeforeEach
    void setUpFilters() throws Exception {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(tenantFilter).doFilter(any(), any(), any());

        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    // POST /api/uats
    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void createUat_returns200_withValidBody() throws Exception {
        Uat uat = buildUat("12345", "Cluj-Napoca", "Cluj", "Municipiu");
        when(uatService.createUat(any(Uat.class))).thenReturn(uat);

        mockMvc.perform(post("/api/uats")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(uat)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codSiruta").value("12345"))
                .andExpect(jsonPath("$.denumire").value("Cluj-Napoca"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUat_adminRole_allowed() throws Exception {
        Uat uat = buildUat("12345", "Cluj-Napoca", "Cluj", "Municipiu");
        when(uatService.createUat(any(Uat.class))).thenReturn(uat);

        mockMvc.perform(post("/api/uats")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(uat)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createUat_userRole_forbidden() throws Exception {
        Uat uat = buildUat("12345", "Cluj-Napoca", "Cluj", "Municipiu");

        mockMvc.perform(post("/api/uats")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(uat)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void createUat_serviceThrowsConflict_returns409() throws Exception {
        Uat uat = buildUat("12345", "Cluj-Napoca", "Cluj", "Municipiu");
        when(uatService.createUat(any(Uat.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "already exists"));

        mockMvc.perform(post("/api/uats")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(uat)))
                .andExpect(status().isConflict());
    }

    // GET /api/uats
    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void getAllUats_returnsListOf200() throws Exception {
        List<Uat> list = List.of(
                buildUat("12345", "Cluj-Napoca", "Cluj", "Municipiu"),
                buildUat("55311", "Florești", "Cluj", "Comună")
        );
        when(uatService.getAllUats()).thenReturn(list);

        mockMvc.perform(get("/api/uats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].codSiruta").value("12345"))
                .andExpect(jsonPath("$[1].codSiruta").value("55311"));
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void getAllUats_emptyList_returns200() throws Exception {
        when(uatService.getAllUats()).thenReturn(List.of());

        mockMvc.perform(get("/api/uats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // GET /api/uats/{codSiruta}
    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void getUat_found_returns200() throws Exception {
        Uat uat = buildUat("12345", "Cluj-Napoca", "Cluj", "Municipiu");
        when(uatService.getUatByCodSiruta("12345")).thenReturn(uat);

        mockMvc.perform(get("/api/uats/12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codSiruta").value("12345"))
                .andExpect(jsonPath("$.judet").value("Cluj"));
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void getUat_notFound_returns404() throws Exception {
        when(uatService.getUatByCodSiruta("99999"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "UAT not found"));

        mockMvc.perform(get("/api/uats/99999"))
                .andExpect(status().isNotFound());
    }

    // PUT /api/uats/{codSiruta}
    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void updateUat_success_returns200() throws Exception {
        Uat updated = buildUat("12345", "Cluj-Napoca Updated", "Cluj", "Municipiu");
        when(uatService.updateUat(eq("12345"), any(Uat.class))).thenReturn(updated);

        mockMvc.perform(put("/api/uats/12345")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.denumire").value("Cluj-Napoca Updated"));
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void updateUat_notFound_returns404() throws Exception {
        when(uatService.updateUat(eq("99999"), any(Uat.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "UAT not found"));

        mockMvc.perform(put("/api/uats/99999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Uat())))
                .andExpect(status().isNotFound());
    }

    // DELETE /api/uats/{codSiruta}
    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void deleteUat_success_returns200() throws Exception {
        doNothing().when(uatService).deleteUat("12345");

        mockMvc.perform(delete("/api/uats/12345").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void deleteUat_notFound_returns404() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "UAT not found"))
                .when(uatService).deleteUat("99999");

        mockMvc.perform(delete("/api/uats/99999").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteUat_userRole_forbidden() throws Exception {
        mockMvc.perform(delete("/api/uats/12345").with(csrf()))
                .andExpect(status().isForbidden());
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
