package com.multitenant.controller;

import com.multitenant.model.core.PublicUat;
import com.multitenant.service.UatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/uats")
@CrossOrigin(origins = "*")
public class PublicUatEndpointController {

    private final UatService uatService;

    public PublicUatEndpointController(UatService uatService) {
        this.uatService = uatService;
    }

    @GetMapping("/judete")
    public ResponseEntity<List<String>> getJudete() {
        return ResponseEntity.ok(uatService.getDistinctJudete());
    }

    @GetMapping("/localitati")
    public ResponseEntity<List<PublicUat>> getLocalitatiByJudet(@RequestParam String judet) {
        return ResponseEntity.ok(uatService.getLocalitatiByJudet(judet));
    }

    @GetMapping("/all")
    public ResponseEntity<List<PublicUat>> getAllLocalitati() {
        return ResponseEntity.ok(uatService.getAllLocalitati());
    }
}
