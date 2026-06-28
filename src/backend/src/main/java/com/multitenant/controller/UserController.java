package com.multitenant.controller;

import com.multitenant.model.core.User;
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
import java.util.stream.Collectors;
import com.multitenant.dto.UserDTO;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private UserDetailsImpl getCurrentUser() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private boolean isSuperAdmin(UserDetailsImpl userDetails) {
        return "ROLE_SUPER_ADMIN".equals(userDetails.getRole());
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (user == null) {
            return ResponseEntity.badRequest().body("User data is missing");
        }
        UserDetailsImpl currentUser = getCurrentUser();

        if (!isSuperAdmin(currentUser)) {
            // Admin must create users in their own tenant
            user.setTenantId(currentUser.getTenantId());

            // Prevent tenant admins from assigning superior roles
            if ("ROLE_SUPER_ADMIN".equals(user.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot assign super admin role.");
            }
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return ResponseEntity.ok(UserDTO.fromEntity(userRepository.save(user)));
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        UserDetailsImpl currentUser = getCurrentUser();

        List<User> users;
        if (isSuperAdmin(currentUser)) {
            users = userRepository.findAll();
        } else {
            users = userRepository.findByTenantId(currentUser.getTenantId());
        }
        return ResponseEntity.ok(users.stream().map(UserDTO::fromEntity).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable long id) {
        UserDetailsImpl currentUser = getCurrentUser();
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        if (!isSuperAdmin(currentUser) && !user.getTenantId().equals(currentUser.getTenantId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to view this user.");
        }

        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable long id, @RequestBody User userDetails) {
        UserDetailsImpl currentUser = getCurrentUser();
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        if (!isSuperAdmin(currentUser) && !user.getTenantId().equals(currentUser.getTenantId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to modify this user.");
        }

        if (userDetails.getUsername() != null)
            user.setUsername(userDetails.getUsername());
        if (userDetails.getRole() != null) {
            // Prevent tenant admins from assigning superior roles
            if (!isSuperAdmin(currentUser) && "ROLE_SUPER_ADMIN".equals(userDetails.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot assign super admin role.");
            }
            user.setRole(userDetails.getRole());
        }

        if (userDetails.getNume() != null)
            user.setNume(userDetails.getNume());
        if (userDetails.getEmail() != null)
            user.setEmail(userDetails.getEmail());
        user.setActiv(userDetails.isActiv());

        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        // Store uatId as a plain Long — no cross-schema FK validation needed
        if (userDetails.getUatId() != null) {
            user.setUatId(userDetails.getUatId());
        }

        return ResponseEntity.ok(UserDTO.fromEntity(userRepository.save(user)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable long id) {
        UserDetailsImpl currentUser = getCurrentUser();
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        if (!isSuperAdmin(currentUser) && !user.getTenantId().equals(currentUser.getTenantId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to delete this user.");
        }

        userRepository.delete(user);
        return ResponseEntity.ok().build();
    }
}
