package com.multitenant.controller;

import com.multitenant.dto.SpecieRefDTO;
import com.multitenant.dto.TipDocumentDTO;
import com.multitenant.repository.LookupRepository;
import com.multitenant.service.LookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lookup")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
public class LookupController {

    private final LookupService lookupService;

    @GetMapping("/tip-sol")
    public ResponseEntity<List<String>> getTipuriSol() {
        return ResponseEntity.ok(lookupService.getTipuriSol());
    }

    @GetMapping("/categorie-folosinta")
    public ResponseEntity<List<String>> getCategoriiFolosinta() {
        return ResponseEntity.ok(lookupService.getCategoriiFolosinta());
    }

    @GetMapping("/tip-sursa-apa")
    public ResponseEntity<List<String>> getTipuriSursaApa() {
        return ResponseEntity.ok(lookupService.getTipuriSursaApa());
    }

    @GetMapping("/tip-document")
    public ResponseEntity<List<TipDocumentDTO>> getTipuriDocument() {
        return ResponseEntity.ok(lookupService.getTipuriDocument());
    }

    @GetMapping("/specii-pomi")
    public ResponseEntity<List<SpecieRefDTO>> getSpeciiPomi() {
        return ResponseEntity.ok(lookupService.getSpeciiPomi());
    }
}