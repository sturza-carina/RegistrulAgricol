package com.multitenant.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TratamentFitosanitarDTO {
    private Long id;
    private LocalDateTime dataEfectuarii;
    private String fenofaza;
    private Long parcelaId;
    private String parcelaDenumire;
    private String agentDaunator;
    private Long catalogPppId;
    private String catalogPppDenumire;
    private Double catalogPppDozaOmologata;
    private Integer catalogPppTimpPauza;
    private Double dozaUtilizata;
    private Double suprafataTratata;
    private Double cantitateTotala;
    private String responsabil;
    private String semnaturaElectronica;
    private LocalDate dataIncepereRecoltare;
    private String documentDareConsum;
    private Boolean dozaDepasita;
    private String justificareSupradozaj;
}
