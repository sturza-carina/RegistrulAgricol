package com.multitenant.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DocumentDTO {
    private Long id;
    private Long gospodarieId;
    private Integer tipDocumentId;
    private String numeFisier;
    private LocalDate dataEmitere;
    private LocalDate dataExpirare;
    private String observatii;
    private boolean esteActiv = true;
}