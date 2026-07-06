package com.multitenant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multitenant.config.tenant.TenantFilter;
import com.multitenant.model.core.Tenant;
import com.multitenant.payload.TenantCreateRequest;
import com.multitenant.repository.TenantRepository;
import com.multitenant.security.JwtAuthenticationFilter;
import com.multitenant.security.SecurityConfig;
import com.multitenant.security.UserDetailsImpl;
import com.multitenant.service.TenantService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TenantController.class)
@Import(SecurityConfig.class)
@SuppressWarnings("null")
class TenantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TenantService tenantService;

    @MockBean
    private TenantRepository tenantRepository;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private TenantFilter tenantFilter;

    @MockBean
    private com.multitenant.security.UserDetailsServiceImpl userDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> {
            inv.<FilterChain>getArgument(2).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        doAnswer(inv -> {
            inv.<FilterChain>getArgument(2).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(tenantFilter).doFilter(any(), any(), any());
    }

    // helpers
    private UserDetailsImpl superAdmin() {
        return new UserDetailsImpl(1L, "superadmin", "pass", "ROLE_SUPER_ADMIN", null, 1L, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
    }

    private UserDetailsImpl adminCluj() {
        return new UserDetailsImpl(2L, "cluj_admin", "pass", "ROLE_ADMIN", "cluj", 1L, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private UserDetailsImpl regularUser() {
        return new UserDetailsImpl(3L, "cluj_user", "pass", "ROLE_USER", "cluj", 1L, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private UsernamePasswordAuthenticationToken authToken(UserDetailsImpl userDetails) {
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private Tenant buildTenant(String id, String name, String schemaName) {
        Tenant tenant = new Tenant();
        tenant.setId(id);
        tenant.setName(name);
        tenant.setSchemaName(schemaName);
        tenant.setActive(true);
        return tenant;
    }

    private TenantCreateRequest buildRequest(String tenantId, String name) {
        TenantCreateRequest req = new TenantCreateRequest();
        req.setTenantId(tenantId);
        req.setName(name);
        return req;
    }

    // POST /api/tenants
    @Test
    void createTenant_superAdmin_success() throws Exception {
        Tenant tenant = buildTenant("54975", "Cluj-Napoca", "uat_54975");
        when(tenantService.createTenant("54975", "Cluj-Napoca")).thenReturn(tenant);

        mockMvc.perform(post("/api/tenants")
                        .with(authentication(authToken(superAdmin())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("54975", "Cluj-Napoca"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("54975"))
                .andExpect(jsonPath("$.name").value("Cluj-Napoca"))
                .andExpect(jsonPath("$.schemaName").value("uat_54975"));
    }

    @Test
    void createTenant_adminRole_forbidden() throws Exception {
        mockMvc.perform(post("/api/tenants")
                        .with(authentication(authToken(adminCluj())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("54975", "Cluj-Napoca"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void createTenant_userRole_forbidden() throws Exception {
        mockMvc.perform(post("/api/tenants")
                        .with(authentication(authToken(regularUser())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("54975", "Cluj-Napoca"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void createTenant_duplicateSirutaCode_returns409() throws Exception {
        when(tenantService.createTenant("54975", "Cluj-Napoca"))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT,
                        "A UAT with SIRUTA code 54975 already exists."));

        mockMvc.perform(post("/api/tenants")
                        .with(authentication(authToken(superAdmin())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("54975", "Cluj-Napoca"))))
                .andExpect(status().isConflict());
    }

    @Test
    void createTenant_nullSirutaCode_returns400() throws Exception {
        when(tenantService.createTenant(null, "Cluj-Napoca"))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "SIRUTA code and name must not be null"));

        mockMvc.perform(post("/api/tenants")
                        .with(authentication(authToken(superAdmin())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(null, "Cluj-Napoca"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTenant_nullName_returns400() throws Exception {
        when(tenantService.createTenant("54975", null))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "SIRUTA code and name must not be null"));

        mockMvc.perform(post("/api/tenants")
                        .with(authentication(authToken(superAdmin())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("54975", null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTenant_unauthenticated_returns403() throws Exception {
        mockMvc.perform(post("/api/tenants")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("54975", "Cluj-Napoca"))))
                .andExpect(status().isForbidden()); // 403, nu 401
    }

    // GET /api/tenants
    @Test
    void getAllTenants_superAdmin_returnsAll() throws Exception {
        List<Tenant> tenants = List.of(
                buildTenant("54975", "Cluj-Napoca", "uat_54975"),
                buildTenant("1017", "Bucuresti", "uat_1017")
        );
        when(tenantRepository.findAll()).thenReturn(tenants);

        mockMvc.perform(get("/api/tenants")
                        .with(authentication(authToken(superAdmin()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("54975"))
                .andExpect(jsonPath("$[1].id").value("1017"));
    }

    @Test
    void getAllTenants_emptyList_returns200() throws Exception {
        when(tenantRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/tenants")
                        .with(authentication(authToken(superAdmin()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllTenants_adminRole_forbidden() throws Exception {
        mockMvc.perform(get("/api/tenants")
                        .with(authentication(authToken(adminCluj()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllTenants_userRole_forbidden() throws Exception {
        mockMvc.perform(get("/api/tenants")
                        .with(authentication(authToken(regularUser()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllTenants_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/tenants"))
                .andExpect(status().isForbidden()); // 403, nu 401
    }
}
