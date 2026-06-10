package com.multitenant.controller;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.model.Uat;
import com.multitenant.model.User;
import com.multitenant.repository.UatRepository;
import com.multitenant.repository.UserRepository;
import com.multitenant.security.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final UatRepository uatRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, UatRepository uatRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.uatRepository = uatRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private UserDetailsImpl getCurrentUser() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private boolean isSuperAdmin(UserDetailsImpl userDetails) {
        return "ROLE_SUPER_ADMIN".equals(userDetails.getRole());
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (user == null) {
            return ResponseEntity.badRequest().body("User data is missing");
        }
        UserDetailsImpl currentUser = getCurrentUser();

        if (!isSuperAdmin(currentUser)) {
            // Admin must create users in their own tenant
            user.setTenantId(currentUser.getTenantId());
            
            Uat requestUat = user.getUat();
            if (requestUat != null) {
                Long uatId = requestUat.getId();
                if (uatId != null) {
                    Optional<Uat> uatOpt = uatRepository.findById(uatId);
                    if (uatOpt.isEmpty() || !uatOpt.get().getTenant().getId().equals(currentUser.getTenantId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("UAT does not belong to your tenant.");
                    }
                }
            }
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return ResponseEntity.ok(userRepository.save(user));
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        UserDetailsImpl currentUser = getCurrentUser();

        if (isSuperAdmin(currentUser)) {
            return ResponseEntity.ok(userRepository.findAll());
        } else {
            // Return only users belonging to the admin's tenant
            return ResponseEntity.ok(userRepository.findByTenantId(currentUser.getTenantId()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        UserDetailsImpl currentUser = getCurrentUser();
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        if (!isSuperAdmin(currentUser) && !user.getTenantId().equals(currentUser.getTenantId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to view this user.");
        }

        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        UserDetailsImpl currentUser = getCurrentUser();
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        if (!isSuperAdmin(currentUser) && !user.getTenantId().equals(currentUser.getTenantId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to modify this user.");
        }

        if (userDetails.getUsername() != null) user.setUsername(userDetails.getUsername());
        if (userDetails.getRole() != null) user.setRole(userDetails.getRole());
        if (userDetails.getNume() != null) user.setNume(userDetails.getNume());
        if (userDetails.getEmail() != null) user.setEmail(userDetails.getEmail());
        user.setActiv(userDetails.isActiv());

        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        Uat requestUat = userDetails.getUat();
        if (requestUat != null) {
            if (!isSuperAdmin(currentUser)) {
                Long uatId = requestUat.getId();
                if (uatId != null) {
                    Optional<Uat> uatOpt = uatRepository.findById(uatId);
                    if (uatOpt.isEmpty() || !uatOpt.get().getTenant().getId().equals(currentUser.getTenantId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("UAT does not belong to your tenant.");
                    }
                }
            }
            user.setUat(requestUat);
        }

        return ResponseEntity.ok(userRepository.save(user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        UserDetailsImpl currentUser = getCurrentUser();
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        if (!isSuperAdmin(currentUser) && !user.getTenantId().equals(currentUser.getTenantId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to delete this user.");
        }

        userRepository.delete(user);
        return ResponseEntity.ok().build();
    }
}
