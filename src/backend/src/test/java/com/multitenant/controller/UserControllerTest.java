package com.multitenant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multitenant.config.tenant.TenantFilter;
import com.multitenant.model.core.User;
import com.multitenant.repository.UserRepository;
import com.multitenant.security.JwtAuthenticationFilter;
import com.multitenant.security.SecurityConfig;
import com.multitenant.security.UserDetailsImpl;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@SuppressWarnings("null")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private TenantFilter tenantFilter;

    @MockBean
    private com.multitenant.security.UserDetailsServiceImpl userDetailsService;

    @MockBean(name = "userSecurity")
    private com.multitenant.security.UserSecurity userSecurity;

    @BeforeEach
    void setUp() throws Exception {
        // Setup UserSecurity mock
        when(userSecurity.canAccessUser(any(), anyLong())).thenAnswer(invocation -> {
            UserDetailsImpl principal = invocation.getArgument(0);
            Long targetUserId = invocation.getArgument(1);

            if ("ROLE_SUPER_ADMIN".equals(principal.getRole())) return true;
            
            Optional<User> userOpt = userRepository.findById(targetUserId);
            if (userOpt.isEmpty()) return false; 
            
            String userTenantId = userOpt.get().getTenantId();
            return principal.getTenantId() != null && principal.getTenantId().equals(userTenantId);
        });
        // Filtrele custom nu fac nimic — lasa request-ul sa treaca
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
        return new UserDetailsImpl(1L, "superadmin", "pass", "ROLE_SUPER_ADMIN", null, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
    }

    private UserDetailsImpl adminCluj() {
        return new UserDetailsImpl(2L, "cluj_admin", "pass", "ROLE_ADMIN", "cluj", true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private UserDetailsImpl regularUser() {
        return new UserDetailsImpl(3L, "cluj_user", "pass", "ROLE_USER", "cluj", true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private UsernamePasswordAuthenticationToken authToken(UserDetailsImpl userDetails) {
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private User buildUser(Long id, String username, String tenantId) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword("encoded_pass");
        user.setRole("ROLE_USER");
        user.setTenantId(tenantId);
        user.setNume("Test User");
        user.setEmail("test@test.com");
        user.setActiv(true);
        return user;
    }

    // GET /api/users
    @Test
    void getAllUsers_superAdmin_returnsAllUsers() throws Exception {
        Page<User> allUsers = new PageImpl<>(List.of(
                buildUser(1L, "user1", "cluj"),
                buildUser(2L, "user2", "bucuresti")
        ));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(allUsers);

        mockMvc.perform(get("/api/users")
                        .with(authentication(authToken(superAdmin()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        verify(userRepository).findAll(any(Pageable.class));
        verify(userRepository, never()).findByTenantId(any(), any());
    }

    @Test
    void getAllUsers_admin_returnsOnlyOwnTenantUsers() throws Exception {
        Page<User> clujUsers = new PageImpl<>(List.of(buildUser(1L, "user1", "cluj")));
        when(userRepository.findByTenantId(eq("cluj"), any(Pageable.class))).thenReturn(clujUsers);

        mockMvc.perform(get("/api/users")
                        .with(authentication(authToken(adminCluj()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        verify(userRepository).findByTenantId(eq("cluj"), any(Pageable.class));
        verify(userRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getAllUsers_userRole_forbidden() throws Exception {
        mockMvc.perform(get("/api/users")
                        .with(authentication(authToken(regularUser()))))
                .andExpect(status().isForbidden());
    }

    // GET /api/users/{id}
    @Test
    void getUser_superAdmin_canGetAnyUser() throws Exception {
        User user = buildUser(5L, "someuser", "bucuresti");
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/5")
                        .with(authentication(authToken(superAdmin()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("someuser"));
    }

    @Test
    void getUser_admin_canGetUserFromOwnTenant() throws Exception {
        User user = buildUser(5L, "someuser", "cluj");
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/5")
                        .with(authentication(authToken(adminCluj()))))
                .andExpect(status().isOk());
    }

    @Test
    void getUser_admin_cannotGetUserFromOtherTenant() throws Exception {
        User user = buildUser(5L, "someuser", "bucuresti"); // alt tenant
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/5")
                        .with(authentication(authToken(adminCluj()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUser_notFound_returns404() throws Exception {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99")
                        .with(authentication(authToken(superAdmin()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUser_userRole_forbidden() throws Exception {
        mockMvc.perform(get("/api/users/5")
                        .with(authentication(authToken(regularUser()))))
                .andExpect(status().isForbidden());
    }

    // POST /api/users
    @Test
    void createUser_superAdmin_success() throws Exception {
        User newUser = buildUser(null, "newuser", "cluj");
        User savedUser = buildUser(10L, "newuser", "cluj");

        when(passwordEncoder.encode(any())).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/users")
                        .with(authentication(authToken(superAdmin())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    void createUser_admin_success_setsOwnTenant() throws Exception {
        User newUser = buildUser(null, "newuser", null);
        User savedUser = buildUser(10L, "newuser", "cluj");

        when(passwordEncoder.encode(any())).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/users")
                        .with(authentication(authToken(adminCluj())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk());
    }

    @Test
    void createUser_admin_withUatId_fromOtherTenant_isIgnoredNow() throws Exception {
        // uatId is now a plain Long — no cross-tenant FK validation on create.
        // The admin's tenantId is enforced instead.
        User newUser = buildUser(null, "newuser", null);
        newUser.setUatId(99L);
        User savedUser = buildUser(10L, "newuser", "cluj");

        when(passwordEncoder.encode(any())).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/users")
                        .with(authentication(authToken(adminCluj())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk());
    }

    @Test
    void createUser_userRole_forbidden() throws Exception {
        mockMvc.perform(post("/api/users")
                        .with(authentication(authToken(regularUser())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new User())))
                .andExpect(status().isForbidden());
    }

    // PUT /api/users/{id}
    @Test
    void updateUser_superAdmin_success() throws Exception {
        User existing = buildUser(5L, "oldname", "cluj");
        User updated = buildUser(5L, "newname", "cluj");

        when(userRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode(any())).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(updated);

        mockMvc.perform(put("/api/users/5")
                        .with(authentication(authToken(superAdmin())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_admin_cannotUpdateUserFromOtherTenant() throws Exception {
        User existing = buildUser(5L, "someuser", "bucuresti"); // alt tenant
        when(userRepository.findById(5L)).thenReturn(Optional.of(existing));

        mockMvc.perform(put("/api/users/5")
                        .with(authentication(authToken(adminCluj())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existing)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUser_notFound_returns404() throws Exception {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/99")
                        .with(authentication(authToken(superAdmin())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new User())))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_userRole_forbidden() throws Exception {
        mockMvc.perform(put("/api/users/5")
                        .with(authentication(authToken(regularUser())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new User())))
                .andExpect(status().isForbidden());
    }

    // DELETE /api/users/{id}
    @Test
    void deleteUser_superAdmin_success() throws Exception {
        User user = buildUser(5L, "someuser", "cluj");
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        mockMvc.perform(delete("/api/users/5")
                        .with(authentication(authToken(superAdmin())))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_admin_cannotDeleteUserFromOtherTenant() throws Exception {
        User user = buildUser(5L, "someuser", "bucuresti"); // alt tenant
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        mockMvc.perform(delete("/api/users/5")
                        .with(authentication(authToken(adminCluj())))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_notFound_returns404() throws Exception {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/users/99")
                        .with(authentication(authToken(superAdmin())))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_userRole_forbidden() throws Exception {
        mockMvc.perform(delete("/api/users/5")
                        .with(authentication(authToken(regularUser())))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}