package com.multitenant.controller;

import com.multitenant.payload.JwtResponse;
import com.multitenant.payload.LoginRequest;
import com.multitenant.payload.ImpersonateRequest;
import com.multitenant.security.JwtUtils;
import com.multitenant.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return ResponseEntity.status(401).build();
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(userDetails);
    }

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    @PostMapping("/impersonate")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> impersonateTenant(@RequestBody ImpersonateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return ResponseEntity.status(401).build();
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        System.out.println("AUDIT: SuperAdmin " + userDetails.getUsername() + " impersonating tenant " + request.getTenantId());
        
        UserDetailsImpl impersonatedDetails = new UserDetailsImpl(
                userDetails.getId(),
                userDetails.getUsername(),
                "",
                userDetails.getRole(),
                request.getTenantId(),
                true,
                userDetails.getAuthorities()
        );
        
        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                impersonatedDetails, null, impersonatedDetails.getAuthorities());
                
        String jwt = jwtUtils.generateJwtToken(newAuth);
        
        return ResponseEntity.ok(new JwtResponse(jwt));
    }
}

