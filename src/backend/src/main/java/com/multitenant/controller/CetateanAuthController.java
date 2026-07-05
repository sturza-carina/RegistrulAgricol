package com.multitenant.controller;

import com.multitenant.model.core.Cetatean;
import com.multitenant.payload.CetateanLoginRequest;
import com.multitenant.payload.CetateanSignupRequest;
import com.multitenant.payload.UserInfoResponse;
import com.multitenant.repository.core.CetateanRepository;
import com.multitenant.payload.CetateanUpdateRequest;
import com.multitenant.security.JwtUtils;
import com.multitenant.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/api/public/cetatean")
public class CetateanAuthController {

    @Autowired
    private CetateanRepository cetateanRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> registerCetatean(@Valid @RequestBody CetateanSignupRequest request) {
        if (cetateanRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        if (cetateanRepository.findByCnpClar(request.getCnp()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: CNP is already in use!");
        }

        Cetatean cetatean = new Cetatean();
        cetatean.setNume(request.getNume());
        cetatean.setPrenume(request.getPrenume());
        cetatean.setCnp(request.getCnp());
        cetatean.setEmail(request.getEmail());
        cetatean.setParola(passwordEncoder.encode(request.getParola()));
        cetatean.setTelefon(request.getTelefon());
        cetatean.setJudet(request.getJudet());
        cetatean.setLocalitate(request.getLocalitate());
        cetatean.setStrada(request.getStrada());
        cetatean.setNumar(request.getNumar());
        cetatean.setBloc(request.getBloc());
        cetatean.setScara(request.getScara());
        cetatean.setEtaj(request.getEtaj());
        cetatean.setApartament(request.getApartament());

        cetateanRepository.save(cetatean);

        return ResponseEntity.ok("Cetatean registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginCetatean(@Valid @RequestBody CetateanLoginRequest request) {
        Optional<Cetatean> cetateanOpt = cetateanRepository.findByEmail(request.getEmail());

        if (cetateanOpt.isEmpty() || !passwordEncoder.matches(request.getParola(), cetateanOpt.get().getParola())) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        Cetatean cetatean = cetateanOpt.get();

        String jwt = jwtUtils.generateCetateanJwtToken(cetatean);

        ResponseCookie jwtCookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(false) // Set to true in production if using HTTPS
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new UserInfoResponse(
                        cetatean.getId(),
                        cetatean.getEmail(),
                        "CETATEAN",
                        null,
                        null
                ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentCetatean() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Cetatean)) {
            return ResponseEntity.status(401).build();
        }
        Cetatean principal = (Cetatean) authentication.getPrincipal();
        Optional<Cetatean> cetateanOpt = cetateanRepository.findById(principal.getId());
        if (cetateanOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Cetatean not found");
        }
        Cetatean cetatean = cetateanOpt.get();
        cetatean.setParola(null); // Do not expose password
        return ResponseEntity.ok(cetatean);
    }
    
    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentCetatean(@Valid @RequestBody CetateanUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Cetatean)) {
            return ResponseEntity.status(401).build();
        }
        Cetatean principal = (Cetatean) authentication.getPrincipal();
        Optional<Cetatean> cetateanOpt = cetateanRepository.findById(principal.getId());
        if (cetateanOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Cetatean not found");
        }
        Cetatean cetatean = cetateanOpt.get();

        cetatean.setNume(request.getNume());
        cetatean.setPrenume(request.getPrenume());
        cetatean.setTelefon(request.getTelefon());
        cetatean.setJudet(request.getJudet());
        cetatean.setLocalitate(request.getLocalitate());
        cetatean.setStrada(request.getStrada());
        cetatean.setNumar(request.getNumar());
        cetatean.setBloc(request.getBloc());
        cetatean.setScara(request.getScara());
        cetatean.setEtaj(request.getEtaj());
        cetatean.setApartament(request.getApartament());

        cetateanRepository.save(cetatean);
        cetatean.setParola(null);
        return ResponseEntity.ok(cetatean);
    }
}
