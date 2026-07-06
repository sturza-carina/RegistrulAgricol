package com.multitenant.dto;

import lombok.Data;

@Data
public class CatalogPppDTO {
    private Long id;
    private String denumireComerciala;
    private String tip; // Fungicid, Erbicid, Insecticid
    private String daunatorVizat;
    private Double dozaOmologata;
    private Integer timpPauza;
}
