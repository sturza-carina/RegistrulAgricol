package com.multitenant.service;

import com.multitenant.dto.DocumentDTO;
import com.multitenant.model.registru.Document;
import com.multitenant.model.registru.Gospodarie;
import com.multitenant.repository.DocumentRepository;
import com.multitenant.repository.GospodarieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final GospodarieRepository gospodarieRepository;

    @Value("${app.documents.storage-path:/data/documents}")
    private String storageBasePath;

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024; // 10 MB

    public DocumentService(DocumentRepository documentRepository, GospodarieRepository gospodarieRepository) {
        this.documentRepository = documentRepository;
        this.gospodarieRepository = gospodarieRepository;
    }

    public Page<Document> getAllDocuments(String uatCode, Pageable pageable) {
        if (uatCode != null && !uatCode.isBlank()) {
            return documentRepository.findByUatCode(uatCode, pageable);
        }
        return documentRepository.findAll(pageable);
    }

    public Page<Document> getDocumentsByGospodarie(Long gospodarieId, Pageable pageable) {
        if (gospodarieId == null) {
            throw new IllegalArgumentException("ID gospodărie nu poate fi null");
        }
        return documentRepository.findByGospodarieId(gospodarieId, pageable);
    }

    public Document getDocumentById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID nu poate fi null");
        }
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documentul nu a fost găsit"));
    }

    @Transactional
    public Document uploadDocument(DocumentDTO dto, MultipartFile file, Long currentUserId) {
        if (dto == null || file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Fișierul și datele documentului sunt obligatorii");
        }
        if (dto.getGospodarieId() == null) {
            throw new IllegalArgumentException("Gospodăria asociată este obligatorie");
        }
        if (dto.getTipDocumentId() == null) {
            throw new IllegalArgumentException("Tipul documentului este obligatoriu");
        }

        validateFile(file);

        Gospodarie gospodarie = gospodarieRepository.findById(dto.getGospodarieId())
                .orElseThrow(() -> new RuntimeException("Gospodăria nu a fost găsită"));

        String storedFileName = UUID.randomUUID() + "_" + sanitizeFileName(file.getOriginalFilename());
        Path targetDir = Paths.get(storageBasePath, dto.getGospodarieId().toString());
        Path targetPath = targetDir.resolve(storedFileName).normalize();

        if (!targetPath.startsWith(targetDir)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nume de fișier invalid");
        }

        try {
            Files.createDirectories(targetDir);
            try (InputStream is = file.getInputStream()) {
                Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Eroare la salvarea fișierului", e);
        }

        Document document = new Document();
        document.setGospodarie(gospodarie);
        document.setTipDocumentId(dto.getTipDocumentId());
        document.setNumeFisier(file.getOriginalFilename());
        document.setCaleStocare(targetPath.toString());
        document.setMimeType(file.getContentType());
        document.setDimensiuneKb((int) (file.getSize() / 1024));
        document.setDataEmitere(dto.getDataEmitere());
        document.setDataExpirare(dto.getDataExpirare());
        document.setObservatii(dto.getObservatii());
        document.setUploadedById(currentUserId);
        document.setEsteActiv(true);

        return documentRepository.save(document);
    }

    public org.springframework.core.io.Resource downloadDocument(Long id, Long gospodarieId) {
        Document document = documentRepository.findByIdAndGospodarieId(id, gospodarieId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Documentul nu a fost găsit"));

        Path path = Paths.get(document.getCaleStocare()).normalize();
        if (!Files.exists(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fișierul nu mai există pe disc");
        }
        return new org.springframework.core.io.FileSystemResource(path);
    }

    @Transactional
    public void deleteDocument(Long id, Long gospodarieId) {
        Document document = documentRepository.findByIdAndGospodarieId(id, gospodarieId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Documentul nu a fost găsit"));

        try {
            Files.deleteIfExists(Paths.get(document.getCaleStocare()));
        } catch (IOException e) {
            // log, dar nu blocăm ștergerea înregistrării din DB dacă fișierul de pe disc lipsește deja
        }
        documentRepository.delete(document);
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fișierul depășește dimensiunea maximă admisă (10MB)");
        }
        if (file.getContentType() == null || !ALLOWED_MIME_TYPES.contains(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tip de fișier nepermis");
        }
    }

    private String sanitizeFileName(String original) {
        if (original == null) {
            return "fisier";
        }
        String name = Paths.get(original).getFileName().toString();
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
