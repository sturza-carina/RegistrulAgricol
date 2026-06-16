package com.multitenant.dto;

import lombok.Data;

@Data
public class MachineryDTO {
    private Long id;
    private String tipUtilaj;
    private String marca;
    private String model;
    private Integer anFabricatie;
    private String numarInmatriculare;
    private Long gospodarieId;
}
