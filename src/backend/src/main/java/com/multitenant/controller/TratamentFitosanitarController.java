package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.dto.TratamentFitosanitarDTO;
import com.multitenant.service.TratamentFitosanitarService;
import com.multitenant.service.PdfExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tratamente")
@RequiredArgsConstructor
@TenantRequired
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
public class TratamentFitosanitarController {

    private final TratamentFitosanitarService tratamentFitosanitarService;
    private final PdfExportService pdfExportService;

    @GetMapping
    public ResponseEntity<Page<TratamentFitosanitarDTO>> getTratamente(
            @RequestParam(value = "parcelaId", required = false) Long parcelaId,
            Pageable pageable) {
        return ResponseEntity.ok(tratamentFitosanitarService.getTratamente(parcelaId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TratamentFitosanitarDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tratamentFitosanitarService.getTratamentById(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TratamentFitosanitarDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(tratamentFitosanitarService.createTratament(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Eroare la salvarea tratamentului: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody TratamentFitosanitarDTO dto) {
        try {
            return ResponseEntity.ok(tratamentFitosanitarService.updateTratament(id, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Eroare la modificarea tratamentului: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tratamentFitosanitarService.deleteTratament(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> downloadRegistruPdf() {
        try {
            byte[] pdfBytes = pdfExportService.generateRegistruPesticidePdf();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"registru_tratamente_ppp.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
