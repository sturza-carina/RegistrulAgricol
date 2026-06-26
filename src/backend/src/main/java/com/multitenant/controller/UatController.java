package com.multitenant.controller;

import com.multitenant.model.core.Uat;
import com.multitenant.repository.UatRepository;
import com.multitenant.service.UatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/uats")
@CrossOrigin(origins = "*")
public class UatController {

    private final UatRepository uatRepository;
    private final UatService uatService;

    public UatController(UatRepository uatRepository, UatService uatService) {
        this.uatRepository = uatRepository;
        this.uatService = uatService;
    }

    @GetMapping
    public ResponseEntity<List<Uat>> getAllUats() {
        return ResponseEntity.ok(uatService.getAllUats());
    }

    @PostMapping
    public ResponseEntity<Uat> createUat(@RequestBody Uat uat) {
        return ResponseEntity.ok(uatService.createUat(uat));
    }

    @PutMapping("/{codSiruta}")
    public ResponseEntity<Uat> updateUat(@PathVariable String codSiruta, @RequestBody Uat request) {
        return ResponseEntity.ok(uatService.updateUat(codSiruta, request));
    }

    @DeleteMapping("/{codSiruta}")
    public ResponseEntity<Void> deleteUat(@PathVariable String codSiruta) {
        uatService.deleteUat(codSiruta);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/judete")
    public ResponseEntity<List<String>> getJudete() {
        return ResponseEntity.ok(uatRepository.findDistinctJudeteOrderByJudetAsc());
    }

    @GetMapping("/localitati")
    public ResponseEntity<List<Uat>> getLocalitatiByJudet(@RequestParam String judet) {
        return ResponseEntity.ok(uatRepository.findByJudetOrderByDenumireAsc(judet));
    }
}


