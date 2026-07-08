package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.dto.CicluProductieDTO;
import com.multitenant.service.CicluProductieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cicluri-productie")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@TenantRequired
@RequiredArgsConstructor
public class CicluProductieController {

    private final CicluProductieService cicluProductieService;

    @GetMapping("/parcela/{parcelaId}")
    public ResponseEntity<Page<CicluProductieDTO>> getCicluriByParcela(@PathVariable Long parcelaId, Pageable pageable) {
        return ResponseEntity.ok(cicluProductieService.getCicluriByParcela(parcelaId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CicluProductieDTO> getCicluById(@PathVariable Long id) {
        return ResponseEntity.ok(cicluProductieService.getCicluById(id));
    }

    @PostMapping
    public ResponseEntity<CicluProductieDTO> createCiclu(@Valid @RequestBody CicluProductieDTO dto) {
        return ResponseEntity.ok(cicluProductieService.createCiclu(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CicluProductieDTO> updateCiclu(@PathVariable Long id, @Valid @RequestBody CicluProductieDTO dto) {
        return ResponseEntity.ok(cicluProductieService.updateCiclu(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCiclu(@PathVariable Long id) {
        cicluProductieService.deleteCiclu(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@RequestParam(required = false) Integer anAgricol) {
        byte[] pdfBytes = cicluProductieService.exportPdf(anAgricol);
        
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "registru_spatii_protejate.pdf");
        
        return new ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
    }
}
