package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.annotation.GdprAudited;
import com.multitenant.dto.DocumentDTO;
import com.multitenant.model.registru.Document;
import com.multitenant.security.UserDetailsImpl;
import com.multitenant.service.DocumentService;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documente")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@TenantRequired
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    @GdprAudited(entity = "Document")
    public ResponseEntity<?> getAllDocuments(@RequestParam(required = false) String uatCode, Pageable pageable) {
        return ResponseEntity.ok(documentService.getAllDocuments(uatCode, pageable));
    }

    @GetMapping("/gospodarie/{gospodarieId}")
    @GdprAudited(entity = "Document")
    public ResponseEntity<?> getDocumentsByGospodarie(@PathVariable Long gospodarieId, Pageable pageable) {
        return ResponseEntity.ok(documentService.getDocumentsByGospodarie(gospodarieId, pageable));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @GdprAudited(entity = "Document")
    public ResponseEntity<?> uploadDocument(@RequestPart("document") DocumentDTO dto,
                                            @RequestPart("file") MultipartFile file) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Document saved = documentService.uploadDocument(dto, file, userDetails.getId());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Eroare la încărcarea documentului: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/download")
    @GdprAudited(entity = "Document")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id,
                                                     @RequestParam Long gospodarieId) {
        Document document = documentService.getDocumentById(id);
        Resource resource = documentService.downloadDocument(id, gospodarieId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getNumeFisier() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @GdprAudited(entity = "Document")
    public ResponseEntity<?> deleteDocument(@PathVariable Long id, @RequestParam Long gospodarieId) {
        try {
            documentService.deleteDocument(id, gospodarieId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Eroare la ștergerea documentului: " + e.getMessage());
        }
    }
}