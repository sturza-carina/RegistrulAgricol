package com.multitenant.dto;

import lombok.Data;

@Data
public class CatalogIngrasaminteDTO {
    private Long id;
    private String denumire;
    private String tip; // Organic / Chimic
    private Double procentAzot;
    private Double procentFosfor;
    private Double procentPotasiu;
}
