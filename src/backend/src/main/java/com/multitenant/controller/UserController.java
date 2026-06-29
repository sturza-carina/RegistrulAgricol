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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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



    @PostMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or (hasRole('ROLE_ADMIN') and #user.role != 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (user == null) {
            return ResponseEntity.badRequest().body("User data is missing");
        }
        UserDetailsImpl currentUser = getCurrentUser();

        if (!"ROLE_SUPER_ADMIN".equals(currentUser.getRole())) {
            user.setTenantId(currentUser.getTenantId());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return ResponseEntity.ok(UserDTO.fromEntity(userRepository.save(user)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable) {
        UserDetailsImpl currentUser = getCurrentUser();

        Page<User> users;
        if ("ROLE_SUPER_ADMIN".equals(currentUser.getRole())) {
            users = userRepository.findAll(pageable);
        } else {
            users = userRepository.findByTenantId(currentUser.getTenantId(), pageable);
        }
        return ResponseEntity.ok(users.map(UserDTO::fromEntity));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@userSecurity.canAccessUser(principal, #id)")
    public ResponseEntity<?> getUser(@PathVariable long id) {
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(UserDTO.fromEntity(userOpt.get()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@userSecurity.canAccessUser(principal, #id) and (hasRole('ROLE_SUPER_ADMIN') or (hasRole('ROLE_ADMIN') and #userDetails.role != 'ROLE_SUPER_ADMIN') or (hasRole('ROLE_USER') and #id == principal.id and #userDetails.role == null))")
    public ResponseEntity<?> updateUser(@PathVariable long id, @RequestBody User userDetails) {
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();

        if (userDetails.getUsername() != null)
            user.setUsername(userDetails.getUsername());
        if (userDetails.getRole() != null) {
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

        if (userDetails.getUatId() != null) {
            user.setUatId(userDetails.getUatId());
        }

        return ResponseEntity.ok(UserDTO.fromEntity(userRepository.save(user)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@userSecurity.canAccessUser(principal, #id) and (hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN'))")
    public ResponseEntity<?> deleteUser(@PathVariable long id) {
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        userRepository.delete(userOpt.get());
        return ResponseEntity.ok().build();
    }
}
