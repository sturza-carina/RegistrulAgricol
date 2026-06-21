package com.multitenant.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CulturaParcelaDto {

    private Long id;
    private Integer anAgricol;
    private String specieCultura;
    private Double suprafataCultivataHa;
    private LocalDate dataInsamantare;
    private LocalDate dataRecoltare;
    private Double productieHectarKg;
    private Double productieTotalaTone;
    private String sistemIrigare;
    private String tipSol;
    private String culturaPrecedenta;
    private Long parcelaId;

}
