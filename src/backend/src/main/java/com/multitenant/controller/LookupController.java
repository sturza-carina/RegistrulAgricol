package com.multitenant.controller;

import com.multitenant.service.LookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lookup")
@RequiredArgsConstructor
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
}